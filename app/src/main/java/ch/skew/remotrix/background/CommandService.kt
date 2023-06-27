package ch.skew.remotrix.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import net.folivo.trixnity.core.model.RoomId
import okio.Path.Companion.toPath

class CommandService: Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    // Null indicates that service has not been set up yet
    private var clients: MutableMap<Int, MatrixClient>? = null
    private val settings = RemotrixSettings(applicationContext)
    private val db = RemotrixDB.getInstance(applicationContext)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            START_ALL -> startAll()
            SEND_MSG -> {
                val sender = intent.getStringExtra(SENDER)
                val payload = intent.getStringExtra(PAYLOAD)
                if (sender === null || payload === null) {
                    // TODO
                } else {
                    sendMsg(sender, payload)
                }
            }
            SEND_TEST_MSG -> {
                val id = intent.getIntExtra(FORWARDER_ID, -1)
                val to = intent.getStringExtra(ROOM_ID)
                val payload = intent.getStringExtra(PAYLOAD)
                if (id == -1 || to === null || payload === null) {
                    // TODO
                } else {
                    sendTestMsg(id, RoomId(to), payload)
                }
            }
            STOP_ALL -> stopAll()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendTestMsg(id: Int, to: RoomId, payload: String) {
        TODO("Not yet implemented")
    }

    private fun sendMsg(sender: String, payload: String) {
        TODO("Not yet implemented")
    }

    private fun stopAll() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        scope.cancel()
        stopSelf()
    }

    private fun startAll() {
        if(this.clients !== null) return
        CoroutineScope(Dispatchers.IO).launch {
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
                if (client !== null) clients?.put(a.id, client)
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