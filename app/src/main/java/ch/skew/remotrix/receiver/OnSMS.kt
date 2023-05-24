package ch.skew.remotrix.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class OnSMS : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals("android.provider.Telephony.SMS_RECEIVED")){
            if(context == null) return
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            msgs.forEach { msg ->
                // Request work here somehow??
            }
        }
    }
}