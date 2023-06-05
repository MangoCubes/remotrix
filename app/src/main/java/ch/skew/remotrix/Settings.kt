package ch.skew.remotrix

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
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
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.components.SelectAccountDialog
import ch.skew.remotrix.data.RemotrixSettings
import kotlinx.coroutines.launch

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    accounts: List<Account> = listOf(),
    defaultSend: Int = -1,
    goBack: () -> Unit = {}
) {
    val open = remember { mutableStateOf(false) }
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
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineText = { Text(stringResource(R.string.choose_default_account)) },
                supportingText = { Text(stringResource(R.string.choose_default_account_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = stringResource(R.string.choose_default_account)
                    )
                },
                modifier = Modifier.clickable { open.value = true }
            )
        }
    }
    SelectAccountDialog(
        accounts = accounts,
        close = { open.value = false },
        confirm = {
            scope.launch {
                settings.saveDefaultSend(it)
            }
            open.value = false
        },
        title = stringResource(R.string.choose_default_account),
        noneChosenDesc = stringResource(R.string.none_option),
        show = open.value,
        defaultSelected = if(defaultSend == -1) null else defaultSend
    )
}