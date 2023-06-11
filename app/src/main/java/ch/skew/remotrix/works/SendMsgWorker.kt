package ch.skew.remotrix.works

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ch.skew.remotrix.R
import ch.skew.remotrix.classes.MsgToSend
import ch.skew.remotrix.classes.SMSMsg
import ch.skew.remotrix.classes.TestMsg
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
import ch.skew.remotrix.data.logDB.MsgStatus
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
            val db = RemotrixDB.getInstance(applicationContext)
            val logging = settings.getLogging.first()
            val currentLog = if (logging) db.logDao.writeAhead(msgType, senderId, payload?.joinToString(", ") ?: "<Empty payload>") else -1
            if(msg === null) {
                if(logging) db.logDao.setFailure(
                    currentLog,
                    MsgStatus.UNRECOGNISED_MESSAGE_CODE,
                    null
                )
                return@withContext Result.failure(
                    workDataOf(
                        "error" to MsgStatus.UNRECOGNISED_MESSAGE_CODE,
                        "msgType" to msgType,
                        "errorMsg" to null,
                        "senderId" to senderId,
                        "payload" to payload
                    )
                )
            }

            // Matrix Account to send message with is chosen at this step.
            val sendAs = when (msg) {
                // If SMSMsg class is found, the forwarder is a function of SMS sender and its body.
                is SMSMsg -> {
                    // Default account is loaded up from the settings.
                    val defaultAccount = settings.getDefaultSend.first()
                    // Account ID of -1 is set to none.
                    if(defaultAccount == -1) {
                        if(logging) db.logDao.setSuccess(
                            currentLog,
                            MsgStatus.MESSAGE_DROPPED
                        )
                        return@withContext Result.success()
                    }
                    // Forwarder rules are loaded here, and are immediately put through getSenderId to determine the forwarder
                    val match = msg.getSenderId(db.forwardRuleDao.getAll())
                    // If match is not found, default account is chosen.
                    if (match !== null) match else defaultAccount
                }

                is TestMsg -> {
                    // Forwarder is the one user selected.
                    msg.senderId
                }

                else -> {
                    if(logging) db.logDao.setFailure(
                        currentLog,
                        MsgStatus.NO_SUITABLE_FORWARDER,
                        null
                    )
                    // This shouldn't execute but still
                    return@withContext Result.failure(
                        workDataOf(
                            "error" to MsgStatus.NO_SUITABLE_FORWARDER,
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
                if(logging) db.logDao.setFailure(
                    currentLog,
                    MsgStatus.CANNOT_LOAD_MATRIX_CLIENT,
                    it.message
                )
                return@withContext Result.failure(
                    workDataOf(
                        "error" to MsgStatus.CANNOT_LOAD_MATRIX_CLIENT,
                        "errorMsg" to it,
                        "msgType" to msgType,
                        "senderId" to senderId,
                        "payload" to payload
                    )
                )
            }
            
            // Not sure why Result may be success but have null client, but still
            if(client === null) {
                if(logging) db.logDao.setFailure(
                    currentLog,
                    MsgStatus.CANNOT_LOAD_MATRIX_CLIENT,
                    null
                )
                return@withContext Result.failure(
                    workDataOf(
                        "error" to MsgStatus.CANNOT_LOAD_MATRIX_CLIENT,
                        "errorMsg" to null,
                        "msgType" to msgType,
                        "senderId" to senderId,
                        "payload" to payload
                    )
                )
            }

            if(msg is TestMsg){
                client.room.sendMessage(msg.to) {
                    text(msg.payload)
                }
                client.startSync()
                while(client.room.getOutbox().value.isNotEmpty()) {
                    delay(1000) //Temporary fix, TODO: Figure out how to stop the code until a message is confirmed to be sent
                }
                client.stopSync()
                scope.cancel()
                if(logging) db.logDao.setSuccess(
                    currentLog,
                    MsgStatus.MESSAGE_SENT
                )
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
                        if(logging) db.logDao.setFailure(
                            currentLog,
                            MsgStatus.CANNOT_CREATE_ROOM,
                            it.message
                        )
                        return@withContext Result.failure(
                            workDataOf(
                                "error" to MsgStatus.CANNOT_CREATE_ROOM,
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
                        if(logging) db.logDao.setFailure(
                            currentLog,
                            MsgStatus.CANNOT_CREATE_CHILD_ROOM,
                            it.message
                        )
                        return@withContext Result.failure(
                            workDataOf(
                                "error" to MsgStatus.CANNOT_CREATE_CHILD_ROOM,
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
                client.startSync()
                client.room.sendMessage(roomId) {
                    text(msg.payload)
                }
                while(client.room.getOutbox().value.isNotEmpty()) {
                    delay(1000) //Temporary fix, TODO: Figure out how to stop the code until a message is confirmed to be sent
                }
                client.stopSync()
                scope.cancel()
                if(logging) db.logDao.setSuccess(
                    currentLog,
                    MsgStatus.MESSAGE_SENT,
                )
                return@withContext Result.success()
            }
            // Again, this should not execute, but oh well.
            if(logging) db.logDao.setFailure(
                currentLog,
                MsgStatus.UNRECOGNISED_MESSAGE_CLASS,
                null
            )
            return@withContext Result.failure(
                workDataOf(
                    "error" to MsgStatus.UNRECOGNISED_MESSAGE_CLASS,
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