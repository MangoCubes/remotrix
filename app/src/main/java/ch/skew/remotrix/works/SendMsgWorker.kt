package ch.skew.remotrix.works

import android.content.Context
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
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.room.JoinRulesEventContent
import net.folivo.trixnity.core.model.events.m.space.ChildEventContent
import net.folivo.trixnity.core.model.events.m.space.ParentEventContent
import okio.Path.Companion.toPath

/**
 * Work where message actually gets sent
 */
class SendMsgWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters){
    override suspend fun doWork(): Result {
        val result = withContext(Dispatchers.IO) {
            // Message is constructed from inputData
            val msgType = inputData.getInt("msgType", -1)
            val senderId = inputData.getInt("senderId", 0)
            val payload = inputData.getStringArray("payload")
            val msg = MsgToSend.from(msgType, senderId, payload)
            // Message that does not have valid type code will be dropped and TODO: logged.
            val settings = RemotrixSettings(applicationContext)
            val db = Room.databaseBuilder(
                applicationContext,
                RemotrixDB::class.java, "accounts.db"
            ).build()
            val logging = settings.getLogging.first()
            if(msg === null) {
                return@withContext Result.failure(
                    workDataOf(
                        "error" to context.getString(R.string.error_message_is_not_sent_because_its_type_code_is_not_recognised),
                        "msgType" to msgType,
                        "errorMsg" to null,
                        "senderId" to senderId,
                        "payload" to payload
                    )
                )
            }
            // Database is loaded after this initial check


            // Matrix Account to send message with is chosen at this step.
            val sendAs = when (msg) {
                // If SMSMsg class is found, the forwarder is a function of SMS sender and its body.
                is SMSMsg -> {
                    // Default account is loaded up from the settings.
                    val defaultAccount = settings.getDefaultSend.first()
                    // Account ID of -1 is set to none.
                    if(defaultAccount == -1) return@withContext Result.success()
                    // Forwarder rules are loaded here, and are immediately put through getSenderId to determine the forwarder
                    val match = msg.getSenderId(db.sendActionDao.getAll())
                    // If match is not found, default account is chosen.
                    if (match !== null) match else defaultAccount
                }

                is TestMsg -> {
                    // Forwarder is the one user selected.
                    msg.senderId
                }

                else -> {
                    // This shouldn't execute but still
                    return@withContext Result.failure(
                        workDataOf(
                            "error" to context.getString(R.string.error_message_is_not_sent_as_the_forwarder_could_not_be_decided),
                            "msgType" to msgType,
                            "errorMsg" to null,
                            "senderId" to senderId,
                            "payload" to payload
                        )
                    )
                }
            }
            // Matrix client is loaded here.
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
                        "error" to context.getString(R.string.error_matrix_client_could_not_be_loaded),
                        "errorMsg" to it,
                        "msgType" to msgType,
                        "senderId" to senderId,
                        "payload" to payload
                    )
                )
            }
            
            // Not sure why Result may be success but have null client, but still
            if(client === null) return@withContext Result.failure(
                workDataOf(
                    "error" to context.getString(R.string.error_matrix_client_could_not_be_loaded),
                    "errorMsg" to null,
                    "msgType" to msgType,
                    "senderId" to senderId,
                    "payload" to payload
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
                val msgSpace = db.accountDao.getMessageSpace(sendAs)
                val sendTo = db.roomIdDao.getDestRoom(msg.sender, sendAs)
                val managerId = settings.getManagerId.first()
                val roomId: RoomId
                // If an appropriate room does not exist for a given forwarder-SMS sender is not found, a new room is created.
                if(sendTo === null) {
                    // The "via" part of m.space.child/parent event.
                    val via = setOf(client.userId.domain)
                    roomId = client.api.rooms.createRoom(
                        name = msg.sender,
                        topic = context.getString(R.string.msg_room_desc).format(msg.sender),
                        initialState = listOf(
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
                            )
                        )
                    ).getOrElse {
                        return@withContext Result.failure(
                            workDataOf(
                                "error" to context.getString(R.string.error_matrix_client_failed_to_create_a_room),
                                "errorMsg" to it,
                                "msgType" to msgType,
                                "senderId" to senderId,
                                "payload" to payload
                            )
                        )
                    }
                    // This state ensures that the parent room recognises the child room as its child.
                    client.api.rooms.sendStateEvent(
                        RoomId(msgSpace),
                        ChildEventContent(suggested = false, via = via),
                        roomId.full
                    ).getOrElse {
                        // Forwarder leaves the room to ensure it is removed in case state cannot be set.
                        client.api.rooms.leaveRoom(roomId)
                        return@withContext Result.failure(
                            workDataOf(
                                "error" to "Error: Matrix client failed to set room parent-child relationship.",
                                "errorMsg" to it,
                                "msgType" to msgType,
                                "senderId" to senderId,
                                "payload" to payload
                            )
                        )
                    }
                } else roomId = RoomId(sendTo)
                // If this was null, it means that this entry was not present in the database. It is entered here.
                if (sendTo === null) {
                    db.roomIdDao.insert(RoomIdData(msg.sender, sendAs, roomId.full))
                    client.api.rooms.inviteUser(roomId, UserId(managerId))
                }
                client.room.sendMessage(roomId) {
                    text(msg.payload)
                }
                client.startSync()
                delay(10000) //Temporary fix, TODO: Figure out how to stop the code until a message is confirmed to be sent
                scope.cancel()
                return@withContext Result.success()
            }
            // Again, this should not execute, but oh well.
            return@withContext Result.failure(
                workDataOf(
                    "error" to context.getString(R.string.error_message_type_is_unrecognised),
                    "msgType" to msgType,
                    "errorMsg" to null,
                    "senderId" to senderId,
                    "payload" to payload
                )
            )
        }
        return result
    }
}