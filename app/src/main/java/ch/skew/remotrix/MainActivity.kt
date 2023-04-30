package ch.skew.remotrix

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.skew.remotrix.ui.theme.RemotrixTheme
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.MatrixConfiguration


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val matrix = Matrix(
            context = this,
            matrixConfiguration = MatrixConfiguration(
                roomDisplayNameFallbackProvider = RoomDisplayName()
            )
        )
        setContent {
            RemotrixApp(matrix)
        }
    }
}
@Composable
fun RemotrixApp(matrix: Matrix) {
    val navController = rememberNavController()
    val session = remember { mutableStateOf(matrix.authenticationService().getLastAuthenticatedSession()) }
    val context = LocalContext.current
    session.value?.open()
    session.value?.syncService()?.startSync(true)
    RemotrixTheme {
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route
        ) {
            composable(route = Destination.Home.route) {
                HomeScreen(
                    onClickAccountList = { navController.navigate(Destination.AccountList.route) }
                )
            }
            composable(route = Destination.AccountList.route) {
                val msg = stringResource(R.string.one_account_limit_help)
                AccountList(
                    session = session.value,
                    onClickGoBack = { navController.popBackStack() },
                    onClickNewAccount = {
                        if(session.value === null) navController.navigate(Destination.NewAccount.route)
                        else Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            composable(route = Destination.NewAccount.route) {
                NewAccount(
                    onClickGoBack = { navController.popBackStack() },
                    matrix = matrix,
                    setSession = { session.value = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickAccountList: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar({
                Text(stringResource(R.string.app_name))
            })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListHeader(stringResource(R.string.accounts))
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
        }
    }
}