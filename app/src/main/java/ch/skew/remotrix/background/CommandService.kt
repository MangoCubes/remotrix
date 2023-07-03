package ch.skew.remotrix.background

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.provider.ContactsContract
import androidx.core.app.NotificationCompat
import ch.skew.remotrix.R
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.classes.CommandAction
import ch.skew.remotrix.classes.SMSMsg
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
import ch.skew.remotrix.data.logDB.MsgStatus
import ch.skew.remotrix.data.roomIdDB.RoomIdData
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.message.reply
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.client.store.TimelineEvent
import net.folivo.trixnity.client.store.isEncrypted
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import net.folivo.trixnity.clientserverapi.model.media.Media
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.room.AvatarEventContent
import net.folivo.trixnity.core.model.events.m.room.HistoryVisibilityEventContent
import net.folivo.trixnity.core.model.events.m.room.JoinRulesEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import net.folivo.trixnity.core.model.events.m.space.ChildEventContent
import net.folivo.trixnity.core.model.events.m.space.ParentEventContent
import okio.Path.Companion.toPath

class CommandService: Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    // Null indicates that service has not been set up yet
    private var clients: MutableMap<Int, Pair<MatrixClient, Account>>? = null
    private lateinit var settings: RemotrixSettings
    private lateinit var db: RemotrixDB

    override fun onCreate() {
        super.onCreate()
        this.settings = RemotrixSettings(applicationContext)
        this.db = RemotrixDB.getInstance(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            RELOAD -> {
                println("Service reloading...")
                CoroutineScope(Dispatchers.IO).launch {
                    reload()
                }
            }
            START_ALL -> {
                println("Service starting...")
                CoroutineScope(Dispatchers.IO).launch {
                    startAll()
                }
            }
            SEND_MSG -> {
                val sender = intent.getStringExtra(SENDER)
                val payload = intent.getStringExtra(PAYLOAD)
                if (sender === null || payload === null) {
                    // TODO
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val log = settings.getLogging.first()
                        sendMsg(SMSMsg(sender, payload), log)
                    }
                }
            }
            SEND_TEST_MSG -> {
                val id = intent.getIntExtra(FORWARDER_ID, -1)
                val to = intent.getStringExtra(ROOM_ID)
                val payload = intent.getStringExtra(PAYLOAD)
                if (id == -1 || to === null || payload === null) {
                    // TODO
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val log = settings.getLogging.first()
                        sendTestMsg(id, RoomId(to), payload, log)
                    }
                }
            }
            STOP_ALL -> stopAll()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun sendTestMsg(id: Int, to: RoomId, payload: String, log: Boolean) {
        if(clients === null) {
            startAll()
        }
        val client = clients?.get(id)?.first
        val currentLog = if (log) db.logDao.writeAhead(1, payload) else -1
        if(client === null) {
            if (currentLog != -1L) db.logDao.setFailure(
                currentLog,
                MsgStatus.NO_SUITABLE_FORWARDER,
                null,
                null
            )
            return
        }
        val tid = client.room.sendMessage(to) {
            text(payload)
        }
        do {
            delay(5000)
            val outbox = client.room.getOutbox().first()
            val message = outbox.find { it.transactionId === tid }
            if(message === null) break
            else if(message.reachedMaxRetryCount) {
                client.room.abortSendMessage(tid)
                if (currentLog != -1L) db.logDao.setFailure(
                    currentLog,
                    MsgStatus.MESSAGE_MAX_ATTEMPTS_REACHED,
                    null,
                    id
                )
            }
        } while (true)
        if (currentLog != -1L) db.logDao.setSuccess(
            currentLog,
            MsgStatus.MESSAGE_SENT,
            id
        )
    }

    private suspend fun sendMsg(msg: SMSMsg, log: Boolean) {
        if(clients === null) {
            startAll()
        }
        val currentLog = if (log) db.logDao.writeAhead(2, msg.payload) else -1
        // Default account is loaded up from the settings.
        val defaultAccount = settings.getDefaultForwarder.first()
        // Forwarder rules are loaded here, and are immediately put through getSenderId to determine the forwarder
        val match = msg.getSenderId(db.forwardRuleDao.getAll())
        // Account ID of -1 is set to none.
        if(defaultAccount == -1 && match === null) {
            if(currentLog != -1L) db.logDao.setSuccess(
                currentLog,
                MsgStatus.MESSAGE_DROPPED,
                null
            )
            return
        }
        // If match is not found, default account is chosen.
        val sendAs = if (match !== null) match else defaultAccount
        val client = clients?.get(sendAs)?.first
        if(client === null) {
            if (currentLog != -1L) db.logDao.setFailure(
                currentLog,
                MsgStatus.NO_SUITABLE_FORWARDER,
                null,
                null
            )
            return
        }
        val msgSpace = db.accountDao.getMessageSpace(sendAs)
        val sendTo = db.roomIdDao.getDestRoom(msg.sender, sendAs)
        val managerId = settings.getManagerId.first()
        val roomId: RoomId
        var picUri: Uri? = null
        // If an appropriate room does not exist for a given forwarder-SMS sender is not found, a new room is created.
        if(sendTo === null) {
            // The "via" part of m.space.child/parent event.
            val via = setOf(client.userId.domain)
            var roomName = msg.sender
            val senderNumber = msg.sender.filter { it.isDigit() }
            val contacts = applicationContext.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
            if (contacts !== null) {
                while (contacts.moveToNext()){
                    val numberIndex = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (numberIndex < 0) continue
                    val current = contacts.getString(numberIndex).filter { it.isDigit() }
                    if(current == senderNumber) {
                        val nameIndex = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val pictureIndex = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                        if(pictureIndex >= 0) {
                            val uri = contacts.getString(pictureIndex)
                            if (uri !== null) picUri = Uri.parse(uri)
                        }
                        if(nameIndex >= 0){
                            val name = contacts.getString(nameIndex)
                            if (name !== null) roomName = name
                        }
                        break
                    }
                }
                contacts.close()
            }
            val initialStates = mutableListOf(
                // Initial event for making this room child of the message room
                Event.InitialStateEvent(
                    content = ParentEventContent(true, via),
                    stateKey = msgSpace
                ),
                // Initial event for making this room restricted to the message room members
                Event.InitialStateEvent(
                    content = JoinRulesEventContent(
                        joinRule = JoinRulesEventContent.JoinRule.Restricted,
                        allow = setOf(
                            JoinRulesEventContent.AllowCondition(RoomId(msgSpace), JoinRulesEventContent.AllowCondition.AllowConditionType.RoomMembership)
                        )
                    ),
                    stateKey = ""
                ),
                // History is in SHARED mode, which allows the manager to view messages sent after invite to users is sent.
                Event.InitialStateEvent(
                    content = HistoryVisibilityEventContent(
                        HistoryVisibilityEventContent.HistoryVisibility.INVITED),
                    stateKey = ""
                )
            )
            if(picUri !== null){
                val stream = applicationContext.contentResolver.openInputStream(picUri)
                if(stream !== null){
                    val length = withContext(Dispatchers.IO) {
                        stream.available()
                    }
                    client.api.media.upload(
                        Media(
                            ByteReadChannel(stream.readBytes()),
                            contentLength = length.toLong(),
                            filename = roomName,
                            contentType = ContentType("image", "jpeg")
                        )
                    ).fold(
                        {
                            initialStates.add(
                                Event.InitialStateEvent(
                                    content = AvatarEventContent(
                                        it.contentUri
                                    ),
                                    stateKey = ""
                                )
                            )
                        },
                        {

                        }
                    )
                    withContext(Dispatchers.IO) {
                        stream.close()
                    }
                }
            }
            roomId = client.api.rooms.createRoom(
                name = roomName,
                topic = applicationContext.getString(R.string.msg_room_desc).format(msg.sender),
                initialState = initialStates
            ).getOrElse {
                if(currentLog != -1L) db.logDao.setFailure(
                    currentLog,
                    MsgStatus.CANNOT_CREATE_ROOM,
                    it.message,
                    sendAs
                )
                return
            }
            // This state ensures that the parent room recognises the child room as its child.
            client.api.rooms.sendStateEvent(
                RoomId(msgSpace),
                ChildEventContent(suggested = false, via = via),
                roomId.full
            ).getOrElse {
                // Forwarder leaves the room to ensure it is removed in case state cannot be set.
                client.api.rooms.leaveRoom(roomId)
                if(currentLog != -1L) db.logDao.setFailure(
                    currentLog,
                    MsgStatus.CANNOT_CREATE_CHILD_ROOM,
                    it.message,
                    sendAs
                )
                return
            }
        } else roomId = RoomId(sendTo)
        // If this was null, it means that this entry was not present in the database. It is entered here.
        if (sendTo === null) {
            db.roomIdDao.insert(RoomIdData(msg.sender, sendAs, roomId.full))
            client.api.rooms.inviteUser(roomId, UserId(managerId))
        }
        client.room.sendMessage(roomId) {
            text(getString(R.string.startup_1).format(client.userId.full, client.deviceId))
            text(getString(R.string.startup_2).format(msg.sender))
        }
        val tid = client.room.sendMessage(roomId) {
            text(msg.payload)
        }
        do {
            delay(5000)
            val outbox = client.room.getOutbox().first()
            val message = outbox.find { it.transactionId === tid }
            if (message === null) break
            else if(message.reachedMaxRetryCount) {
                client.room.abortSendMessage(tid)
                if (currentLog != -1L) db.logDao.setFailure(
                    currentLog,
                    MsgStatus.MESSAGE_MAX_ATTEMPTS_REACHED,
                    null,
                    sendAs
                )
            }
        } while (true)
        if(currentLog != -1L) db.logDao.setSuccess(
            currentLog,
            MsgStatus.MESSAGE_SENT,
            sendAs
        )
    }

    private fun stopAll() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        scope.cancel()
        stopSelf()
    }

    private suspend fun startAll() {
        if(this.clients !== null) return
        else reload()
    }
    private suspend fun reload() {
        clients = mutableMapOf()
        val accounts = Account.from(db.accountDao.getAllAccounts().first())
        for(a in accounts){
            val clientDir = applicationContext.filesDir.resolve("clients/${a.id}")
            val repo = createRealmRepositoriesModule {
                this.directory(clientDir.toString())
            }
            val scope = CoroutineScope(Dispatchers.Default)
            val media = OkioMediaStore(applicationContext.filesDir.resolve("clients/media").absolutePath.toPath())
            val client = MatrixClient.fromStore(
                repositoriesModule = repo,
                mediaStore = media,
                scope = scope
            ).getOrNull()
            if (client !== null) {
                clients?.put(a.id, Pair(client, a))
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            clients?.forEach {
                val client = it.value.first
                client.startSync()
                if (settings.getEnableOnBootMessage.first()){
                    client.room.sendMessage(RoomId(it.value.second.managementRoom)) {
                        text(getString(R.string.ready_to_accept_commands).format(client.userId.full))
                    }
                }
                client.room.getTimelineEventsFromNowOn().collect { ev ->
                    if(ev.event.sender.full == client.userId.full) return@collect
                    val content = ev.content?.getOrNull()
                    if (content is RoomMessageEventContent.TextMessageEventContent && ev.isEncrypted) {
                        val reply = handleMessage(it.value, content.body, ev)
                        if(reply === null) return@collect
                        client.room.sendMessage(ev.roomId) {
                            when(reply) {
                                is CommandAction.Reaction -> {
                                    text(reply.reaction)
                                    // TODO: Use reaction to let user know message has been sent successfully
                                }
                                is CommandAction.Reply -> {
                                    text(reply.msg)
                                    reply(ev)
                                }
                            }
                        }
                    }
                }
            }
        }
        if(clients !== null){
            val notification = NotificationCompat.Builder(this, "command_listener")
                .setContentTitle(getString(R.string.remotrix_service))
                .setContentText(getString(R.string.remotrix_service_desc).format(clients?.size))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
            startForeground(1, notification.build())
        }

    }

    private suspend fun handleMessage(account: Pair<MatrixClient, Account>, body: String, event: TimelineEvent): CommandAction? {
        if(body.startsWith("!")){
            val args = body.split(' ')
            if(args[0] == "!say") {
                return null//CommandAction.Reaction("✅")
            } else if(args[0] == "!close") {
                if (event.roomId.full == account.second.managementRoom)
                    return CommandAction.Reply(getString(R.string.cannot_delete_management_room))
                val client = account.first
                client.api.rooms.getMembers(event.roomId).fold(
                    {
                        it.forEach { user ->
                            if(user.stateKey != client.userId.full){
                                client.api.rooms.kickUser(event.roomId, UserId(user.stateKey)).onFailure {
                                    return CommandAction.Reply(getString(R.string.error_cannot_kick_user).format(user.stateKey))
                                }
                            }
                        }
                        client.api.rooms.leaveRoom(event.roomId)
                        db.roomIdDao.delRoomById(event.roomId.full)
                        client.api.rooms.forgetRoom(event.roomId)
                    },
                    {
                        return CommandAction.Reply(getString(R.string.kick_error))
                    }
                )
            } else if(args[0] == "!ping") return CommandAction.Reply(getString(R.string.pong))
            else if (args[0] == "!help") return CommandAction.Reply(getString(R.string.command_help_output))
            else return CommandAction.Reply(getString(R.string.unknown_command))
        }
        return null//CommandAction.Reaction("✅")
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val START_ALL = "START_ALL"
        const val STOP_ALL = "STOP_ALL"
        const val SEND_MSG = "SEND_MSG"
        const val SEND_TEST_MSG = "SEND_TEST_MSG"
        const val RELOAD = "RELOAD"

        const val FORWARDER_ID = "FORWARDER_ID"
        const val ROOM_ID = "ROOM_ID"
        const val SENDER = "SENDER"
        const val PAYLOAD = "PAYLOAD"
    }
}