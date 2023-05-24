package ch.skew.remotrix.works

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ch.skew.remotrix.classes.MsgToSend
import ch.skew.remotrix.classes.TestMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
            val clientDir = context.filesDir.resolve("clients/${msg.senderId}")
            val repo = createRealmRepositoriesModule {
                this.directory(clientDir.toString())
            }
            val media = OkioMediaStore(context.filesDir.resolve("clients/media").absolutePath.toPath())
            val client = MatrixClient.fromStore(
                repositoriesModule = repo,
                mediaStore = media,
                scope = CoroutineScope(Dispatchers.IO)
            ).getOrElse {
                return@withContext Result.failure(
                    workDataOf(
                        "error" to it
                    )
                )
            }
            if(msg is TestMsg){
                client?.let {
                    client.syncOnce()
                    it.room.sendMessage(msg.to) {
                        text(msg.payload)
                    }
                    client.syncOnce()
                    return@withContext Result.success()
                }
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