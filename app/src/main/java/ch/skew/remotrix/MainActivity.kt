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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.classes.Destination
import ch.skew.remotrix.components.ListHeader
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
import ch.skew.remotrix.data.accountDB.AccountViewModel
import ch.skew.remotrix.data.forwardRuleDB.ForwardRule
import ch.skew.remotrix.data.forwardRuleDB.ForwardRuleViewModel
import ch.skew.remotrix.data.logDB.LogData
import ch.skew.remotrix.data.logDB.LogViewModel
import ch.skew.remotrix.ui.theme.RemotrixTheme


class MainActivity : ComponentActivity() {

    @Suppress("UNCHECKED_CAST")
    private val accountViewModel by viewModels<AccountViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AccountViewModel(RemotrixDB.getInstance(applicationContext).accountDao) as T
                }
            }
        }
    )
    @Suppress("UNCHECKED_CAST")
    private val forwardRuleViewModel by viewModels<ForwardRuleViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ForwardRuleViewModel(RemotrixDB.getInstance(applicationContext).forwardRuleDao) as T
                }
            }
        }
    )
    @Suppress("UNCHECKED_CAST")
    private val logViewModel by viewModels<LogViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LogViewModel(RemotrixDB.getInstance(applicationContext).logDao) as T
                }
            }
        }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val accounts by accountViewModel.accounts.collectAsState()
            val sendActions by forwardRuleViewModel.forwardRule.collectAsState()
            val logs by logViewModel.logs.collectAsState()
            RemotrixApp(
                Account.from(accounts),
                sendActions,
                logs
            )
        }
    }
}
@Composable
fun RemotrixApp(
    accounts: List<Account>,
    forwardRules: List<ForwardRule>,
    logs: List<LogData>
) {
    val settings = RemotrixSettings(LocalContext.current)
    val openedBefore = settings.getOpenedBefore.collectAsState(initial = null)
    val defaultSend = settings.getDefaultSend.collectAsState(initial = null)
    val logging = settings.getLogging.collectAsState(initial = null)
    val navController = rememberNavController()
    RemotrixTheme {
        if (openedBefore.value !== null && defaultSend.value !== null && logging.value !== null) NavHost(
            navController = navController,
            startDestination =
                if(!openedBefore.value!!) Destination.Setup.route
                else Destination.Home.route

        ) {
            composable(route = Destination.Home.route) {
                HomeScreen(
                    accounts,
                    navigate = { navController.navigate(it) },
                    defaultSend = defaultSend.value!!
                )
            }
            composable(route = Destination.AccountList.route) {
                AccountList(
                    accounts = accounts,
                    onClickGoBack = { navController.popBackStack() },
                    onClickNewAccount = { navController.navigate(Destination.NewAccount.route) },
                )
            }
            composable(route = Destination.NewAccount.route) {
                NewAccount(
                    onClickGoBack = { navController.popBackStack() }
                )
            }
            composable(route = Destination.Setup.route) {
                SetupScreen(
                    done = { navController.navigate(Destination.Home.route) },
                    goBack = { navController.popBackStack() },
                    openedBefore = openedBefore.value!!
                )
            }
            composable(route = Destination.Settings.route) {
                Settings(
                    accounts = accounts,
                    defaultSend = defaultSend.value!!,
                    goBack = { navController.popBackStack() },
                    logging = logging.value!!
                )
            }
            composable(route = Destination.Logs.route) {
                Logs(
                    accounts = accounts,
                    logs = logs,
                    isEnabled = logging.value!!,
                    goBack = { navController.popBackStack() }
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
    navigate: (String) -> Unit = {},
    defaultSend: Int = -1,
    forwardRules: List<ForwardRule> = listOf()
) {
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
                modifier = Modifier.clickable { navigate(Destination.Setup.route) }
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
                modifier = Modifier.clickable { navigate(Destination.AccountList.route) }
            )
            val desc = stringResource(R.string.settings_desc) + if (forwardRules.isEmpty() && defaultSend == -1) stringResource(R.string.settings_desc_warning) else ""
            ListItem(
                headlineText = { Text(stringResource(R.string.settings)) },
                supportingText = { Text(desc) },
                leadingContent = {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                },
                modifier = Modifier.clickable { navigate(Destination.Settings.route) }
            )
            ListHeader(stringResource(R.string.current_status))
            ListItem(
                headlineText = { Text(stringResource(R.string.view_logs)) },
                supportingText = { Text(stringResource(R.string.view_logs_desc)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.Storage,
                        contentDescription = stringResource(R.string.view_logs)
                    )
                },
                modifier = Modifier.clickable { navigate(Destination.Logs.route) }
            )
        }
    }
}