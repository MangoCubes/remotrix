package ch.skew.remotrix.setup

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

enum class CurrentlyGranting {
    SMS,
    CONTACT,
    NOTIFICATION
}
@SuppressLint("BatteryLife")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AskPermissions(
    nextPage: () -> Unit = {}
){
    Scaffold(
        topBar = {
            TopAppBar({
                Text("Permissions")
            })
        }
    ) { padding ->
        val context = LocalContext.current
        val smsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val packageName = context.packageName
        val pm = context.getSystemService(POWER_SERVICE) as PowerManager
        val batteryGranted = pm.isIgnoringBatteryOptimizations(packageName)
        val notificationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        val contactGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val smsAlreadyGranted = remember { mutableStateOf(smsGranted) }
        val notificationAlreadyGranted = remember { mutableStateOf(notificationGranted) }
        val contactAlreadyGranted = remember { mutableStateOf(contactGranted) }
        val currentlyGranting = remember { mutableStateOf<CurrentlyGranting?>(null) }
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                when (currentlyGranting.value) {
                    CurrentlyGranting.SMS -> {
                        smsAlreadyGranted.value = true
                        Toast.makeText(
                            context,
                            "SMS permission granted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CurrentlyGranting.CONTACT -> {
                        contactAlreadyGranted.value = true
                        Toast.makeText(
                            context,
                            "Contact permission granted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CurrentlyGranting.NOTIFICATION -> {
                        notificationAlreadyGranted.value = true
                        Toast.makeText(
                            context,
                            "Notification permission granted.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    null -> {}

                }
            } else {
                when (currentlyGranting.value) {
                    CurrentlyGranting.SMS -> {
                        Toast.makeText(
                            context,
                            "SMS permission denied.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CurrentlyGranting.CONTACT -> {
                        Toast.makeText(
                            context,
                            "Contact permission denied.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    CurrentlyGranting.NOTIFICATION -> {
                        Toast.makeText(
                            context,
                            "Notification permission denied.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    null -> {}
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ListItem(
                headlineText = { Text("Required Permissions") },
                supportingText = {
                    Text(
                        buildAnnotatedString {
                            append("This app requires ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("SMS access")
                            }

                            append(" for both sending and receiving. This is obviously needed because this app is supposed to forward incoming SMS via Matrix and send back using SMS.")
                        }
                    )
                    Text(
                        buildAnnotatedString {
                            append("This app also needs to ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("bypass battery optimisation")
                            }
                            append(".")
                        }
                    )
                    Text("App needs to listen to messages sent into Matrix chatroom to act upon them.")
                }
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onClick = {
                    currentlyGranting.value = CurrentlyGranting.SMS
                    launcher.launch(Manifest.permission.RECEIVE_SMS)
                },
                enabled = !smsAlreadyGranted.value
            ) {
                Text("Grant SMS access")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onClick = {
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(context, intent, null)
                },
                enabled = !batteryGranted
            ) {
                Text("Bypass battery optimisation")
            }
            ListItem(
                headlineText = { Text("Optional Permissions") },
                supportingText = {
                    Text(
                        buildAnnotatedString {
                            append("This app does not need ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("contact access")
                            }
                            append(", but it is recommended. If granted, this app will read your contact to get the name of the sender, and it will be used for room names instead. You may skip this, but this will make all room names to show up as phone numbers only.")
                        }
                    )
                    Text(
                        buildAnnotatedString {
                            append("This app also uses ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("notification")
                            }
                            append(" not to alert you of some event, but to let you know that the core service is running.")
                        }
                    )
                }
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onClick = {
                    currentlyGranting.value = CurrentlyGranting.CONTACT
                    launcher.launch(Manifest.permission.READ_CONTACTS)
                },
                enabled = !contactAlreadyGranted.value
            ) {
                Text("Grant contact access")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onClick = {
                    currentlyGranting.value = CurrentlyGranting.NOTIFICATION
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                enabled = !notificationAlreadyGranted.value
            ) {
                Text("Grant notification")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onClick = nextPage,
                enabled = smsAlreadyGranted.value
            ) {
                Text("Done")
            }
        }
    }
}