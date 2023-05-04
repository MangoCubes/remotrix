package ch.skew.remotrix

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.skew.remotrix.data.Account
import ch.skew.remotrix.data.AccountEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountList(
    accounts: List<Account>,
    onAccountEvent: (AccountEvent) -> Unit,
    onClickGoBack: () -> Unit,
    onClickNewAccount: () -> Unit
){
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.account_list)) },
                navigationIcon = {
                    IconButton(onClickGoBack) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.go_back))
                    }
                },
                actions = {
                    IconButton(onClickNewAccount) {
                        Icon(Icons.Filled.Add, stringResource(R.string.add_new_account))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            accounts.forEach {
                SessionItem(it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionItem(account: Account) {
    ListItem(
        headlineText = { Text(account.userId) },
        leadingContent = {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = account.homeServer,
            )
        },
//        trailingContent = {
//            IconButton(onClick = unsetSession) {
//                Icon(
//                    Icons.Filled.Delete,
//                    contentDescription = session.myUserId,
//                )
//            }
//        }
    )
}
