package ch.skew.remotrix.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.skew.remotrix.R
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.components.ListHeader
import ch.skew.remotrix.components.SelectAccountDialog
import ch.skew.remotrix.components.SelectOnSendActionDialog
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
import kotlinx.coroutines.launch

enum class CurrentDialog {
    None,
    SelectAccount,
    OnSendSuccess,
    OnSendFailure
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    accounts: List<Account> = listOf(),
    defaultForwarder: Int = -1,
    logging: Boolean = true,
    enableOnBootMessage: Boolean = true,
    goBack: () -> Unit = {},
    debugMenu: () -> Unit = {},
    onSendSuccess: RemotrixSettings.OnSend = RemotrixSettings.OnSend.None,
    onSendFailure: RemotrixSettings.OnSend = RemotrixSettings.OnSend.None,
) {
    val open = remember { mutableStateOf(CurrentDialog.None) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val settings = RemotrixSettings(context)
    Scaffold(
        topBar = {
            TopAppBar({
                Text(stringResource(R.string.settings))
            }, navigationIcon = {
                IconButton(goBack) {
                    Icon(Icons.Filled.ArrowBack, stringResource(R.string.go_back))
                }
            })
        },

        ) { padding ->
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scroll)
        ) {
            ListHeader(stringResource(R.string.accounts))
            ListItem(
                headlineContent = { Text(stringResource(R.string.choose_default_account)) },
                supportingContent = { Text(stringResource(R.string.choose_default_account_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = stringResource(R.string.choose_default_account)
                    )
                },
                modifier = Modifier.clickable { open.value = CurrentDialog.SelectAccount }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.enable_service_ready_message)) },
                supportingContent = { Text(stringResource(R.string.enable_service_ready_message_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.Start,
                        contentDescription = stringResource(R.string.enable_service_ready_message)
                    )
                },
                modifier = Modifier.clickable {
                    scope.launch {
                        settings.saveEnableOnBootMessage(!enableOnBootMessage)
                    }
                },
                trailingContent = {
                    Switch(checked = enableOnBootMessage, onCheckedChange = null)
                }
            )
            ListHeader(stringResource(R.string.behaviour))
            ListItem(
                headlineContent = { Text("Successful Message Transmission") },
                supportingContent = {
                    Text("Determine what happens if the SMS is sent successfully.")
                    Text("Commands are not affected by this.")
                                    },

                leadingContent = {
                    Icon(
                        Icons.Filled.ChatBubble,
                        contentDescription = "Successful Message Transmission"
                    )
                },
                modifier = Modifier.clickable { open.value = CurrentDialog.OnSendSuccess }
            )
            ListItem(
                headlineContent = { Text("Unsuccessful Message Transmission") },
                supportingContent = {
                    Text("Determine what happens if sending SMS fails")
                    Text("Commands are not affected by this.")
                                    },
                leadingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.Announcement,
                        contentDescription = "Unsuccessful Message Transmission"
                    )
                },
                modifier = Modifier.clickable { open.value = CurrentDialog.OnSendFailure }
            )
            ListHeader(stringResource(R.string.logging))
            ListItem(
                headlineContent = { Text(stringResource(R.string.enable_logging)) },
                supportingContent = { Text(stringResource(R.string.enable_logging_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.Storage,
                        contentDescription = stringResource(R.string.enable_logging)
                    )
                },
                modifier = Modifier.clickable {
                    scope.launch {
                        settings.saveLogging(!logging)
                    }
                },
                trailingContent = {
                    Switch(checked = logging, onCheckedChange = null)
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.delete_log)) },
                supportingContent = { Text(stringResource(R.string.delete_log_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.DeleteForever,
                        contentDescription = stringResource(R.string.delete_log)
                    )
                },
                modifier = Modifier.clickable {
                    Toast.makeText(context, context.getString(R.string.log_deleted), Toast.LENGTH_SHORT).show()
                    scope.launch { RemotrixDB.getInstance(context).logDao.deleteAll() }
                }
            )
            ListHeader(stringResource(R.string.debug))
            ListItem(
                headlineContent = { Text(stringResource(R.string.debug_menu)) },
                supportingContent = { Text(stringResource(R.string.debug_menu_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.BugReport,
                        contentDescription = stringResource(R.string.debug_menu)
                    )
                },
                modifier = Modifier.clickable { debugMenu() }
            )
        }
    }
    SelectAccountDialog(
        accounts = accounts,
        close = { open.value = CurrentDialog.None },
        confirm = {
            scope.launch {
                settings.saveDefaultForwarder(it)
            }
            open.value = CurrentDialog.None
        },
        title = stringResource(R.string.choose_default_account),
        noneChosenDesc = stringResource(R.string.none_option),
        show = open.value == CurrentDialog.SelectAccount,
        defaultSelected = if(defaultForwarder == -1) null else defaultForwarder
    )
    SelectOnSendActionDialog(
        close = { open.value = CurrentDialog.None },
        confirm = {
            scope.launch {
                if (open.value == CurrentDialog.OnSendSuccess) {
                    settings.saveOnSendSuccess(it)
                } else if(open.value == CurrentDialog.OnSendFailure) {
                    settings.saveOnSendFailure(it)
                }
            }
            open.value = CurrentDialog.None
        },
        title = TODO(),
        show = open.value == CurrentDialog.SelectAccount || open.value == CurrentDialog.OnSendFailure,
        defaultSelected = if (open.value == CurrentDialog.OnSendSuccess) {
            onSendSuccess
        } else {
            onSendFailure
        }
    )
}