package ch.skew.remotrix.background

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ServiceWatcher(
    private val context: Context,
    workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters){
    override suspend fun doWork(): Result {
        Intent(context, CommandService::class.java)
            .apply {
                action = CommandService.START_ALL
                context.startService(this)
            }
        println("Triggering service...")
        return Result.success()
    }
}