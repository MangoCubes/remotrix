package ch.skew.remotrix.setup

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun SetManagementSpace(
    nextPage: () -> Unit
){
    Scaffold(
        topBar = {
            TopAppBar({
                Text("Set Management Space")
            })
        }
    ) { padding ->
        val managementSpace = remember { mutableStateOf("") }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val settings = RemotrixSettings(context)
        val currentManagementSpace = settings.getManagementSpaceId.collectAsState(initial = "")
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ListItem(
                headlineText = { Text("Management Space") },
                supportingText = {
                    Text(
                        buildAnnotatedString {
                            append("Upon adding a ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("bot account")
                            }
                            append(" to this app, it will automatically create a ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("management room")
                            }
                            append(" and invite the ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("manager account")
                            }
                            append(". If you are planning on adding multiple accounts, it may be desirable to have all the rooms under a single space.")
                        }
                    )
                    Text(
                        buildAnnotatedString {
                            append("If not, you may leave this field empty. Regardless of whether ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("management space")
                            }
                            append(" is set or not, management rooms will still be created for each account on this app.")
                        }
                    )
                    if (currentManagementSpace.value !== "") Text(
                        buildAnnotatedString {
                            append("You already have set a ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("management space")
                            }
                            append(". Press continue to keep current settings.")
                        }
                    )
                }
            )
            TextField(
                value = managementSpace.value,
                onValueChange = { managementSpace.value = it },
                label = { Text("Space ID") },
                singleLine = true,
                placeholder = { Text(
                    if (currentManagementSpace.value === null
                        || currentManagementSpace.value === "")
                        "!spaceId:example.com"
                    else currentManagementSpace.value!!
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                onClick = {
                    if (managementSpace.value === "" && currentManagementSpace.value !== "") nextPage()
                    else confirmSpace(managementSpace.value, context, scope, settings, nextPage)
                }
            ) {
                Text("Continue")
            }
        }
    }
}

fun confirmSpace(managementSpace: String, context: Context, scope: CoroutineScope, settings: RemotrixSettings, done: () -> Unit){
    val spacePattern = Regex("![A-z]+:[A-z0-9\\.]+")
    if(managementSpace !== "" && spacePattern.matchEntire(managementSpace) === null){
        Toast.makeText(context, "The space ID you have entered is invalid.", Toast.LENGTH_SHORT).show()
        return
    }
    scope.launch {
        settings.saveManagementSpaceId(if (managementSpace === "") null else managementSpace)
        Toast.makeText(context, "Management space ID set.", Toast.LENGTH_SHORT).show()
        done()
    }
}