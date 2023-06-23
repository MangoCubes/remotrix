package ch.skew.remotrix.setup

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

enum class CurrentlyGranting {
    SMS,
    CONTACT
}
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
        val allowNext = remember { mutableStateOf(smsGranted) }
        val smsAlreadyGranted = remember { mutableStateOf(smsGranted) }
        val contactAlreadyGranted = remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) }
        val currentlyGranting = remember { mutableStateOf<CurrentlyGranting?>(null) }
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                if(currentlyGranting.value === CurrentlyGranting.CONTACT) {
                    contactAlreadyGranted.value = true
                    Toast.makeText(
                        context,
                        "Contact permission granted.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if(currentlyGranting.value === CurrentlyGranting.SMS) {
                    allowNext.value = true
                    smsAlreadyGranted.value = true
                    Toast.makeText(
                        context,
                        "SMS permission granted.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if(currentlyGranting.value === CurrentlyGranting.CONTACT) {
                    Toast.makeText(
                        context,
                        "Contact permission denied.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if(currentlyGranting.value === CurrentlyGranting.SMS) {
                    Toast.makeText(
                        context,
                        "SMS permission denied.",
                        Toast.LENGTH_SHORT
                    ).show()
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

                            append(".")
                        }
                    )
                    Text("This is obviously needed because this app is supposed to forward incoming SMS via Matrix.")
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
            ListItem(
                headlineText = { Text("Optional Permissions") },
                supportingText = {
                    Text("This app does not need contact access, but it is recommended. If granted, this app will read your contact to get the name of the sender, and it will be used for room names instead. You may skip this, but this will make all room names to show up as phone numbers only.")
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
                onClick = nextPage,
                enabled = allowNext.value
            ) {
                Text("Done")
            }
        }
    }
}