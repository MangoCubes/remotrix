package ch.skew.remotrix.setup

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ch.skew.remotrix.data.RemotrixSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetManagerAccount(
    nextPage: () -> Unit
){
    Scaffold(
        topBar = {
            TopAppBar({
                Text("Set Manager Account")
            })
        }
    ) { padding ->
        val mgrId = remember { mutableStateOf("") }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val settings = RemotrixSettings(context)
        val currentMgrId = settings.getManagerId.collectAsState(initial = "")
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ListItem(
                headlineText = { Text("Manager Account") },
                supportingText = {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Manager account")
                            }
                            append(" is the account that gets automatically invited into all rooms registered bots generate, which includes all message forwarding rooms and management rooms.")
                        }
                    )
                    Text(
                        buildAnnotatedString {
                            append("Essentially, ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("manager account")
                            }
                            append(" is the first to receive any forwarded messages.")
                        }
                    )
                    Text(
                        buildAnnotatedString {
                            append("Please enter the username of the ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("manager account")
                            }
                            append(" here.")
                        }
                    )
                    if (currentMgrId.value !== "") Text(
                        buildAnnotatedString {
                            append("You already have set a ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("manager account")
                            }
                            append(". Press continue to keep current settings.")
                        }
                    )
                }
            )
            TextField(
                value = mgrId.value,
                onValueChange = { mgrId.value = it },
                label = { Text("Manager ID") },
                singleLine = true,
                placeholder = { Text(if(currentMgrId.value === "") "@username:example.com" else currentMgrId.value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                enabled = currentMgrId.value !== "" || mgrId.value !== "",
                onClick = {
                    if (mgrId.value === "" && currentMgrId.value !== "") nextPage()
                    else confirm(mgrId.value, context, scope, settings, nextPage)
                }
            ) {
                Text("Continue")
            }
        }
    }
}

@Suppress("RegExpRedundantEscape")
fun confirm(userId: String, context: Context, scope: CoroutineScope, settings: RemotrixSettings, done: () -> Unit){
    val idPattern = Regex("@.+:[A-z0-9\\.-]+")
    if(idPattern.matchEntire(userId) === null){
        Toast.makeText(context, "The ID you have entered is invalid.", Toast.LENGTH_SHORT).show()
        return
    }
    scope.launch {
        settings.saveManagerId(userId)
        Toast.makeText(context, "Manager set.", Toast.LENGTH_SHORT).show()
        done()
    }
}