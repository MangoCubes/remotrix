package ch.skew.remotrix.works

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ch.skew.remotrix.classes.MsgToSend
import ch.skew.remotrix.classes.TestMsg
import kotlinx.coroutines.Dispatchers
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
        return withContext(Dispatchers.IO) {
            val msg = MsgToSend.from(inputData.getInt("msgType", -1), inputData.getInt("senderId", 0), inputData.getStringArray("payload"))
            if(msg === null) return@withContext Result.failure(
                workDataOf(
                    "error" to "Invalid message."
                )
            )
            val clientDir = context.filesDir.resolve("clients/${msg.senderId}")
            val client = MatrixClient.fromStore(
                repositoriesModule = createRealmRepositoriesModule {
                    this.directory(clientDir.toString())
                },
                mediaStore = OkioMediaStore(context.filesDir.resolve("clients/media").absolutePath.toPath()),
                scope = this
            ).getOrElse {
                return@withContext Result.failure(
                    workDataOf(
                        "error" to it
                    )
                )
            }
            if(msg is TestMsg){
                client?.let {
                    it.room.sendMessage(msg.to) {
                        text(msg.payload)
                    }
                    client.startSync()
                    delay(1000)
                }
            }
            return@withContext Result.success()
        }
    }
}