package ch.skew.remotrix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.classes.Destination
import ch.skew.remotrix.components.ListHeader
import ch.skew.remotrix.components.SelectAccountDialog
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
import ch.skew.remotrix.data.accountDB.AccountEvent
import ch.skew.remotrix.data.accountDB.AccountEventAsync
import ch.skew.remotrix.data.accountDB.AccountViewModel
import ch.skew.remotrix.data.sendActionDB.SendAction
import ch.skew.remotrix.data.sendActionDB.SendActionEvent
import ch.skew.remotrix.data.sendActionDB.SendActionViewModel
import ch.skew.remotrix.ui.theme.RemotrixTheme
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            RemotrixDB::class.java,
            "accounts.db"
        ).build()
    }

    @Suppress("UNCHECKED_CAST")
    private val accountViewModel by viewModels<AccountViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AccountViewModel(db.accountDao) as T
                }
            }
        }
    )
    @Suppress("UNCHECKED_CAST")
    private val sendActionViewModel by viewModels<SendActionViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SendActionViewModel(db.sendActionDao) as T
                }
            }
        }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val accounts by accountViewModel.accounts.collectAsState()
            val sendActions by sendActionViewModel.sendActions.collectAsState()
            RemotrixApp(
                accountViewModel::onEvent,
                accountViewModel::onEventAsync,
                Account.from(accounts),
                sendActionViewModel::onEvent,
                sendActions
            )
        }
    }
}
@Composable
fun RemotrixApp(
    onAccountEvent: (AccountEvent) -> Unit,
    onAccountEventAsync: (AccountEventAsync) -> Deferred<Long>,
    accounts: List<Account>,
    onSendActionEvent: (SendActionEvent) -> Unit,
    sendActions: List<SendAction>
) {
    val settings = RemotrixSettings(LocalContext.current)
    val openedBefore = settings.getOpenedBefore.collectAsState(initial = null)
    val defaultSend = settings.getDefaultSend.collectAsState(initial = null)
    val navController = rememberNavController()
    RemotrixTheme {
        if (openedBefore.value !== null && defaultSend.value !== null) NavHost(
            navController = navController,
            startDestination =
                if(!openedBefore.value!!) Destination.Setup.route
                else Destination.Home.route

        ) {
            composable(route = Destination.Home.route) {
                HomeScreen(
                    accounts,
                    onClickAccountList = { navController.navigate(Destination.AccountList.route) },
                    onClickShowSetup = { navController.navigate(Destination.Setup.route) },
                    defaultSend.value!!
                )
            }
            composable(route = Destination.AccountList.route) {
                AccountList(
                    accounts = accounts,
                    onAccountEvent = onAccountEvent,
                    onClickGoBack = { navController.popBackStack() },
                    onClickNewAccount = { navController.navigate(Destination.NewAccount.route) },
                )
            }
            composable(route = Destination.NewAccount.route) {
                NewAccount(
                    onClickGoBack = { navController.popBackStack() },
                    onAccountEvent = onAccountEvent,
                    onAccountEventAsync = onAccountEventAsync
                )
            }
            composable(route = Destination.Setup.route) {
                SetupScreen(
                    done = { navController.navigate(Destination.Home.route) },
                    goBack = { navController.popBackStack() },
                    openedBefore = openedBefore.value!!
                )
            }
        }
    }
}
@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    accounts: List<Account> = listOf(),
    onClickAccountList: () -> Unit = {},
    onClickShowSetup: () -> Unit = {},
    defaultSend: Int = -1
) {
    val open = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val settings = RemotrixSettings(context)
    Scaffold(
        topBar = {
            TopAppBar({
                Text(stringResource(R.string.app_name))
            })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListHeader(stringResource(R.string.setup))
            ListItem(
                headlineText = { Text(stringResource(R.string.set_manager_account)) },
                supportingText = { Text(stringResource(R.string.set_manager_account_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.AdminPanelSettings,
                        contentDescription = stringResource(R.string.manage_accounts),
                    )
                },
                modifier = Modifier.clickable {onClickShowSetup()}
            )
            ListItem(
                headlineText = { Text(stringResource(R.string.manage_accounts)) },
                supportingText = { Text(stringResource(R.string.manage_accounts_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = stringResource(R.string.manage_accounts),
                    )
                },
                modifier = Modifier.clickable { onClickAccountList() }
            )
            ListHeader(stringResource(R.string.behaviour))
            ListItem(
                headlineText = { Text(stringResource(R.string.choose_default_account)) },
                supportingText = { Text(stringResource(R.string.choose_default_account_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.Message,
                        contentDescription = stringResource(R.string.choose_default_account)
                    )
                },
                modifier = Modifier.clickable { open.value = true }
            )
            ListHeader(stringResource(R.string.current_status))

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
        title = "Choose Default Account",
        noneChosenDesc = "Drop messages that do not match any rules",
        show = open.value,
        defaultSelected = defaultSend
    )
}