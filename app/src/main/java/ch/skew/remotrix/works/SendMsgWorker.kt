package ch.skew.remotrix.works

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ch.skew.remotrix.classes.MsgToSend
import ch.skew.remotrix.classes.SMSMsg
import ch.skew.remotrix.classes.TestMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import okio.Path.Companion.toPath

class SendMsgWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters){
    override suspend fun doWork(): Result {
        val result = withContext(Dispatchers.IO) {
            Log.i("Remotrix", "Running task")
            val msg = MsgToSend.from(inputData.getInt("msgType", -1), inputData.getInt("senderId", 0), inputData.getStringArray("payload"))
            if(msg === null) return@withContext Result.failure(
                workDataOf(
                    "error" to "Invalid message."
                )
            )
            val sendTo = when (msg) {
                is SMSMsg -> {
                    2 //TEMP
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
            val clientDir = context.filesDir.resolve("clients/${sendTo}")
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
                Log.i("Remotrix", msg.payload)
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