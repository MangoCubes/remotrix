package ch.skew.remotrix.works

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ch.skew.remotrix.R
import ch.skew.remotrix.classes.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import net.folivo.trixnity.core.model.RoomId
import okio.Path.Companion.toPath

class SendMsgWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters){
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val account = Account(1, "remotrix", "skew.ch", "https://matrix.skew.ch", "!BAZCUeakzJjfxPNjIB:skew.ch")
            val clientDir = context.filesDir.resolve("clients/${account.id}")
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
            client?.room?.sendMessage(RoomId(account.managementRoom)) {
                text(context.getString(R.string.test_msg))
            }
            return@withContext Result.success()
        }
    }
}