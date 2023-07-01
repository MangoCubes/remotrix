package ch.skew.remotrix.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ch.skew.remotrix.background.ServiceWatcher
import java.time.Duration

class OnBoot : BroadcastReceiver(){
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)){
            if(context == null) return
            val work = PeriodicWorkRequestBuilder<ServiceWatcher>(Duration.ofHours(1))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(
                            NetworkType.CONNECTED
                        ).build()
                )
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork("service_watcher", ExistingPeriodicWorkPolicy.KEEP, work)
        }
    }
}