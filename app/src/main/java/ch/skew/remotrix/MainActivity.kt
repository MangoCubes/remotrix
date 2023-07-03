package ch.skew.remotrix

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import ch.skew.remotrix.background.CommandService
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.classes.Destination
import ch.skew.remotrix.classes.SettingsDest
import ch.skew.remotrix.classes.Setup
import ch.skew.remotrix.components.ListHeader
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
import ch.skew.remotrix.data.accountDB.AccountViewModel
import ch.skew.remotrix.data.forwardRuleDB.ForwardRule
import ch.skew.remotrix.data.logDB.LogData
import ch.skew.remotrix.data.logDB.LogViewModel
import ch.skew.remotrix.settings.DebugSettings
import ch.skew.remotrix.settings.Settings
import ch.skew.remotrix.setup.AdditionalInfo
import ch.skew.remotrix.setup.AskPermissions
import ch.skew.remotrix.setup.SetManagementSpace
import ch.skew.remotrix.setup.SetManagerAccount
import ch.skew.remotrix.setup.Welcome
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
    private val logViewModel by viewModels<LogViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LogViewModel(RemotrixDB.getInstance(applicationContext).logDao) as T
                }
            }
        }
    )
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(applicationContext, CommandService::class.java)
            .apply {
                action = CommandService.START_ALL
                applicationContext.startService(this)
            }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel("command_listener", "Command Listener", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        setContent {
            val accounts by accountViewModel.accounts.collectAsState()
            val logs by logViewModel.logs.collectAsState()
            RemotrixApp(
                Account.from(accounts),
                logs
            )
        }
    }
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RemotrixApp(
    accounts: List<Account>,
    logs: List<LogData>
) {
    val settings = RemotrixSettings(LocalContext.current)
    val openedBefore = settings.getOpenedBefore.collectAsState(initial = null)
    val defaultForwarder = settings.getDefaultForwarder.collectAsState(initial = null)
    val enableOnBootMessage = settings.getEnableOnBootMessage.collectAsState(initial = null)
    val logging = settings.getLogging.collectAsState(initial = null)
    val debugAlivePing = settings.getDebugAlivePing.collectAsState(initial = null)
    val navController = rememberNavController()
    RemotrixTheme {
        if (openedBefore.value !== null) NavHost(
            navController = navController,
            startDestination =
                if(!openedBefore.value!!) Destination.Setup.route
                else Destination.Home.route

        ) {
            composable(route = Destination.Home.route) {
                HomeScreen(
                    navigate = { navController.navigate(it) },
                    defaultForwarder = defaultForwarder.value!!
                )
            }
            composable(route = Destination.AccountList.route) {
                AccountList(
                    accounts = accounts,
                    defaultForwarder = defaultForwarder.value ?: -1,
                    onClickGoBack = { navController.popBackStack() },
                    onClickNewAccount = { navController.navigate(Destination.NewAccount.route) },
                )
            }
            composable(route = Destination.NewAccount.route) {
                NewAccount(
                    onClickGoBack = { navController.popBackStack() }
                )
            }
            navigation(route = Destination.Setup.route, startDestination = Setup.Welcome.route) {
                composable(Setup.Welcome.route){
                    Welcome { navController.navigate(Setup.Permissions.route) }
                }
                composable(Setup.Permissions.route){
                    AskPermissions { navController.navigate(Setup.Manager.route) }
                }
                composable(Setup.Manager.route){
                    SetManagerAccount { navController.navigate(Setup.ManagerSpace.route) }
                }
                composable(Setup.ManagerSpace.route){
                    SetManagementSpace { navController.navigate(Setup.NextStep.route) }
                }
                composable(Setup.NextStep.route){
                    AdditionalInfo { navController.navigate(Destination.Home.route) }
                }
            }
            navigation(route = Destination.Settings.route, startDestination = SettingsDest.Default.route) {
                composable(route = SettingsDest.Default.route) {
                    Settings(
                        accounts,
                        defaultForwarder.value ?: -1,
                        logging.value ?: false,
                        enableOnBootMessage.value ?: true,
                        { navController.popBackStack() },
                        { navController.navigate(SettingsDest.Debug.route) }
                    )
                }
                composable(route = SettingsDest.Debug.route) {
                    DebugSettings(
                        { navController.popBackStack() },
                        debugAlivePing.value ?: false
                    )
                }
            }
            composable(route = Destination.Logs.route) {
                Logs(
                    accounts = accounts,
                    logs = logs,
                    isEnabled = logging.value ?: false,
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
    navigate: (String) -> Unit = {},
    defaultForwarder: Int = -1,
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
            val desc = stringResource(R.string.settings_desc) + if (forwardRules.isEmpty() && defaultForwarder == -1) stringResource(R.string.settings_desc_warning) else ""
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