package ch.skew.remotrix.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.skew.remotrix.R
import ch.skew.remotrix.data.RemotrixSettings
import kotlinx.coroutines.launch

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugSettings(
    goBack: () -> Unit = {},
    debugAlivePing: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val settings = RemotrixSettings(context)
    Scaffold(
        topBar = {
            TopAppBar({
                Text(stringResource(R.string.debug_settings))
            }, navigationIcon = {
                IconButton(goBack) {
                    Icon(Icons.Filled.ArrowBack, stringResource(R.string.go_back))
                }
            })
        },

        ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineText = { Text(stringResource(R.string.receive_message_on_service_check)) },
                supportingText = { Text(stringResource(R.string.receive_message_on_service_check_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = stringResource(R.string.receive_message_on_service_check)
                    )
                },
                modifier = Modifier.clickable {
                    scope.launch { settings.saveDebugAlivePing(!debugAlivePing) }
                },
                trailingContent = {
                    Switch(checked = debugAlivePing, onCheckedChange = null)
                }
            )
        }
    }
}