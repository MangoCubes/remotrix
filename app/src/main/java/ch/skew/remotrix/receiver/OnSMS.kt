package ch.skew.remotrix.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import ch.skew.remotrix.works.SendMsgWorker

class OnSMS : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals("android.provider.Telephony.SMS_RECEIVED")){
            if(context == null) return
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val msgList = mutableListOf<String>()
            val sender = msgs[0].originatingAddress
            if (sender === null) return
            msgs.forEach {
                msgList.add(it.messageBody)
            }
            val work = OneTimeWorkRequestBuilder<SendMsgWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(
                            NetworkType.CONNECTED
                        ).build()
                )
                .setInputData(
                    Data.Builder()
                        .putInt("msgType", 2)
                        .putStringArray("payload", arrayOf(sender, msgList.joinToString("")))
                        .build()
                )
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(sender, ExistingWorkPolicy.APPEND_OR_REPLACE, work)
        }
    }
}