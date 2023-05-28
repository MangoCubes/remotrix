package ch.skew.remotrix.works

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ch.skew.remotrix.R
import ch.skew.remotrix.classes.MsgToSend
import ch.skew.remotrix.classes.SMSMsg
import ch.skew.remotrix.classes.TestMsg
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
import ch.skew.remotrix.data.roomIdDB.RoomIdData
import ch.skew.remotrix.data.sendActionDB.SendAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import net.folivo.trixnity.clientserverapi.model.rooms.DirectoryVisibility
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.space.ChildEventContent
import net.folivo.trixnity.core.model.events.m.space.ParentEventContent
import okio.Path.Companion.toPath

class SendMsgWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters){
    override suspend fun doWork(): Result {
        val result = withContext(Dispatchers.IO) {
            Log.i("Remotrix", "Running task")
            // Construct message
            val msg = MsgToSend.from(inputData.getInt("msgType", -1), inputData.getInt("senderId", 0), inputData.getStringArray("payload"))
            if(msg === null) return@withContext Result.failure(
                workDataOf(
                    "error" to "Invalid message."
                )
            )
            val db = Room.databaseBuilder(
                applicationContext,
                RemotrixDB::class.java, "accounts.db"
            ).build()

            val sendAs = when (msg) {
                is SMSMsg -> {
                    msg.getSenderId(db.sendActionDao.getAll() + SendAction(1, "", "", 0, 100))
                }

                is TestMsg -> {
                    msg.senderId
                }

                else -> {
                    return@withContext Result.failure(
                        workDataOf(
                            "error" to "Message type is invalid."
                        )
                    )
                }
            }
            // If sendAs is null, it means the message did not match any rule and should be dropped.
             if (sendAs === null) return@withContext Result.success()

            val clientDir = context.filesDir.resolve("clients/${sendAs}")
            val repo = createRealmRepositoriesModule {
                this.directory(clientDir.toString())
            }
            val scope = CoroutineScope(Dispatchers.Default)
            val media = OkioMediaStore(context.filesDir.resolve("clients/media").absolutePath.toPath())
            val client = MatrixClient.fromStore(
                repositoriesModule = repo,
                mediaStore = media,
                scope = scope
            ).getOrElse {
                return@withContext Result.failure(
                    workDataOf(
                        "error" to it
                    )
                )
            }

            if(client === null) return@withContext Result.failure(
                workDataOf(
                    "error" to "Client cannot be constructed."
                )
            )

            if(msg is TestMsg){
                client.room.sendMessage(msg.to) {
                    text(msg.payload)
                }
                client.startSync()
                delay(10000) //Temporary fix, TODO: Figure out how to stop the code until a message is confirmed to be sent
                scope.cancel()
                return@withContext Result.success()
            } else if(msg is SMSMsg){
                val via = setOf(client.userId.domain)
                val msgSpace = db.accountDao.getMessageSpace(sendAs)
                val sendTo = db.roomIdDao.getDestRoom(msg.sender, sendAs)
                val managerId = RemotrixSettings(applicationContext).getManagerId.first()
                val roomId: RoomId
                if(sendTo === null) {
                    roomId = client.api.rooms.createRoom(
                        visibility = DirectoryVisibility.PRIVATE,
                        name = msg.sender,
                        topic = context.getString(R.string.msg_room_desc).format(msg.sender),
                        initialState = listOf(
                            Event.InitialStateEvent(
                                content = ParentEventContent(true, via),
                                stateKey = msgSpace
                            )
                        )
                    ).getOrElse {
                        return@withContext Result.failure(
                            workDataOf(
                                "error" to "Cannot create room."
                            )
                        )
                    }
                } else roomId = RoomId(sendTo)
                client.api.rooms.sendStateEvent(RoomId(msgSpace), ChildEventContent(suggested = false, via = via), roomId.full).getOrElse {
                    client.api.rooms.leaveRoom(roomId)
                    return@withContext Result.failure(
                        workDataOf(
                            "error" to "Cannot make the new room child room."
                        )
                    )
                }

                client.api.rooms.inviteUser(roomId, UserId(managerId))
                if (sendTo === null) db.roomIdDao.insert(RoomIdData(msg.sender, sendAs, roomId.full))
                client.room.sendMessage(roomId) {
                    text(msg.payload)
                }
                client.startSync()
                delay(10000) //Temporary fix, TODO: Figure out how to stop the code until a message is confirmed to be sent
                scope.cancel()
                return@withContext Result.success()
            }
            return@withContext Result.failure(
                workDataOf(
                    "error" to "Message type is invalid."
                )
            )
        }
        return result
    }
}