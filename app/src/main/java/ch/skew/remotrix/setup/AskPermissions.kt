package ch.skew.remotrix.setup

import android.Manifest
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
        val allowNext = remember { mutableStateOf(false) }
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) allowNext.value = true
            else {
                Toast.makeText(
                    context,
                    "SMS permission has not been granted.",
                    Toast.LENGTH_SHORT
                ).show()
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                onClick = { launcher.launch(Manifest.permission.RECEIVE_SMS) },
                enabled = !allowNext.value
            ) {
                Text("Grant SMS access")
            }
            Button(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                onClick = nextPage,
                enabled = allowNext.value
            ) {
                Text("Done")
            }
        }
    }
}