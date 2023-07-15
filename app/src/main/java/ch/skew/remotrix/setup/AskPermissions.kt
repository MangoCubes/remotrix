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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    Notification,
    ReceiveSMS,
    SendSMS,
    Battery,
    Contacts
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
        val packageName = context.packageName
        val pm = context.getSystemService(POWER_SERVICE) as PowerManager
        val currentlyGranting = remember { mutableStateOf<CurrentlyGranting?>(null) }
        val notificationGranted = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
        val smsSendingGranted = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
        val smsReceivingGranted = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECEIVE_MMS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
        val contactsGranted = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
        val batteryGranted = remember {
            mutableStateOf(pm.isIgnoringBatteryOptimizations(packageName))
        }
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            println(isGranted)
            if (isGranted) {
                when(currentlyGranting.value) {
                    CurrentlyGranting.Contacts -> contactsGranted.value = true
                    CurrentlyGranting.Notification -> notificationGranted.value = true
                    CurrentlyGranting.ReceiveSMS -> smsReceivingGranted.value = true
                    CurrentlyGranting.SendSMS -> smsSendingGranted.value = true
                    CurrentlyGranting.Battery -> batteryGranted.value = true
                    null -> {}
                }
                Toast.makeText(
                    context,
                    "Permission granted.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Permission denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ListItem(
                headlineText = { Text("Required Permissions") },
                supportingText = {
                    Text(
                        buildAnnotatedString {
                            append("These permissions are ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("required")
                            }
                            append(", meaning the app will not be able to perform its function without them.")
                        }
                    )
                }
            )
            PermissionItem(
                name = "Notification",
                granted = notificationGranted.value,
                onClick = {
                    currentlyGranting.value = CurrentlyGranting.Notification
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                onInfoClick = {}
            )
            PermissionItem(
                name = "Receive SMS",
                granted = smsReceivingGranted.value,
                onClick = {
                    currentlyGranting.value = CurrentlyGranting.ReceiveSMS
                    launcher.launch(Manifest.permission.RECEIVE_SMS)
                },
                onInfoClick = {}
            )
            PermissionItem(
                name = "Send SMS",
                granted = smsSendingGranted.value,
                onClick = {
                    currentlyGranting.value = CurrentlyGranting.SendSMS
                    launcher.launch(Manifest.permission.SEND_SMS)
                },
                onInfoClick = {}
            )
            PermissionItem(
                name = "Bypass battery optimisation",
                granted = batteryGranted.value,
                onClick = {
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(context, intent, null)
                    batteryGranted.value = true
                },
                onInfoClick = {}
            )
            ListItem(
                headlineText = { Text("Optional Permissions") },
                supportingText = {
                    Text(
                        buildAnnotatedString {
                            append("These permissions are ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("not required")
                            }
                            append(", and this app can function without them.")
                        }
                    )
                }
            )
            PermissionItem(
                name = "Read contacts",
                granted = contactsGranted.value,
                onClick = {
                    currentlyGranting.value = CurrentlyGranting.Contacts
                    launcher.launch(Manifest.permission.READ_CONTACTS)
                },
                onInfoClick = {}
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onClick = nextPage,
                enabled = smsSendingGranted.value && smsReceivingGranted.value && notificationGranted.value && batteryGranted.value
            ) {
                Text("Next")
            }
        }
    }
}