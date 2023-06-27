package ch.skew.remotrix.background

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.provider.ContactsContract
import ch.skew.remotrix.R
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.classes.SMSMsg
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
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
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import net.folivo.trixnity.clientserverapi.model.media.Media
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.room.AvatarEventContent
import net.folivo.trixnity.core.model.events.m.room.HistoryVisibilityEventContent
import net.folivo.trixnity.core.model.events.m.room.JoinRulesEventContent
import net.folivo.trixnity.core.model.events.m.space.ChildEventContent
import net.folivo.trixnity.core.model.events.m.space.ParentEventContent
import okio.Path.Companion.toPath

class CommandService: Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    // Null indicates that service has not been set up yet
    private var clients: MutableMap<Int, MatrixClient>? = null
    private lateinit var settings: RemotrixSettings
    private lateinit var db: RemotrixDB

    override fun onCreate() {
        super.onCreate()
        this.settings = RemotrixSettings(applicationContext)
        this.db = RemotrixDB.getInstance(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            START_ALL -> {
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
                        sendMsg(SMSMsg(sender, payload))
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
                        sendTestMsg(id, RoomId(to), payload)
                    }
                }
            }
            STOP_ALL -> stopAll()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun sendTestMsg(id: Int, to: RoomId, payload: String) {
        if(clients === null) {
            startAll()
        }
        val client = clients?.get(id)
        if(client === null) {
            // TODO
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
        } while (true)
        // TODO: Add logging
    }

    private suspend fun sendMsg(msg: SMSMsg) {
        if(clients === null) {
            startAll()
        }
        // Default account is loaded up from the settings.
        val defaultAccount = settings.getDefaultForwarder.first()
        // Account ID of -1 is set to none.
        if(defaultAccount == -1) {
            // TODO: Logging (success)
            return
        }
        // Forwarder rules are loaded here, and are immediately put through getSenderId to determine the forwarder
        val match = msg.getSenderId(db.forwardRuleDao.getAll())
        // If match is not found, default account is chosen.
        val sendAs = if (match !== null) match else defaultAccount
        val client = clients?.get(sendAs)
        if(client === null) {
            // TODO
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
                // TODO: MsgStatus.CANNOT_CREATE_ROOM
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
                // TODO: MsgStatus.CANNOT_CREATE_CHILD_ROOM,
                return
            }
        } else roomId = RoomId(sendTo)
        // If this was null, it means that this entry was not present in the database. It is entered here.
        if (sendTo === null) {
            db.roomIdDao.insert(RoomIdData(msg.sender, sendAs, roomId.full))
            client.api.rooms.inviteUser(roomId, UserId(managerId))
        }
        val tid = client.room.sendMessage(roomId) {
            text(msg.payload)
        }
        do {
            delay(3000)
            val outbox = client.room.getOutbox().first()
            if(outbox.find { it.transactionId === tid } === null) break
        } while (true)
        // TODO: MsgStatus.MESSAGE_SENT
    }

    private fun stopAll() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        scope.cancel()
        stopSelf()
    }

    private suspend fun startAll() {
        if(this.clients !== null) return
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
                client.startSync()
                clients?.put(a.id, client)
            }
        }
    }
/*
    private fun addClient() {
        val notification = NotificationCompat.Builder(this, "command_listener")
            .setContentTitle("Listening to Matrix chat...")
            .setContentText("${this.clients.size} bots active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                client.room.getAll().flatten().collect { rooms ->
                    rooms.filter { it.membership == Membership.INVITE }.forEach {
                        client.api.rooms.joinRoom(it.roomId).getOrNull()
                    }
                }
            }
            launch {
                client.room.getTimelineEventsFromNowOn().collect {timelineEvent ->
                    val content = timelineEvent.content?.getOrNull()
                    if (content is RoomMessageEventContent.TextMessageEventContent) {

                        val body = content.body
                        println(body)
                        val answer = when {
                            body.lowercase().startsWith("ping") ->
                                "pong to ${content.body.removePrefix("ping").trimStart()}"

                            else -> null
                        }
                        if (answer != null) client.room.sendMessage(timelineEvent.roomId) {
                            text(answer)
                            reply(timelineEvent)
                        }
                    }
                }
            }
        }
        if(clients.isEmpty()) {
            startForeground(1, notification.build())
        } else {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notification.build())
        }
    }
*/
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

        const val FORWARDER_ID = "FORWARDER_ID"
        const val ROOM_ID = "ROOM_ID"
        const val SENDER = "SENDER"
        const val PAYLOAD = "PAYLOAD"
    }
}