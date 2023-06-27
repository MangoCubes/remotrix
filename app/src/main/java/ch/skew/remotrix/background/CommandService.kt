package ch.skew.remotrix.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import net.folivo.trixnity.client.MatrixClient

class CommandService: Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var clients: MutableList<MatrixClient> = mutableListOf()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            START_ALL -> startAll()
            SEND_MSG -> sendMsg()
            SEND_TEST_MSG -> sendTestMsg()
            STOP_ALL -> stopAll()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendTestMsg() {
        TODO("Not yet implemented")
    }

    private fun sendMsg() {
        TODO("Not yet implemented")
    }

    private fun stopAll() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopClient() {
        TODO("Not yet implemented")
    }

    private fun startAll() {

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
    }
}