package ch.skew.remotrix.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ch.skew.remotrix.background.CommandService

class OnBoot : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)){
            if(context == null) return
            Intent(context, CommandService::class.java)
                .apply {
                    action = CommandService.START_ALL
                    context.startService(this)
                }
        }
    }
}