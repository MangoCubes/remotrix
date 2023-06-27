package ch.skew.remotrix.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import ch.skew.remotrix.background.CommandService

class OnSMS : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals("android.provider.Telephony.SMS_RECEIVED")){
            if(context == null) return
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val msgList = mutableListOf<String>()
            val sender = msgs[0].originatingAddress
            if (sender === null) return
            // Apparently, there may be msgs[1], but this is only when the message is an MMS, content being longer than a single SMS.
            msgs.forEach {
                msgList.add(it.messageBody)
            }
            Intent(context, CommandService::class.java)
                .putExtra(CommandService.SENDER, sender)
                .putExtra(CommandService.PAYLOAD, msgList.joinToString(""))
                .apply {
                    action = CommandService.SEND_MSG
                    context.startService(this)
                }
        }
    }
}