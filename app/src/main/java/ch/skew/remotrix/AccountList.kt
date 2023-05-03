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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountList(
    onClickGoBack: () -> Unit,
    onClickNewAccount: () -> Unit
){
    val clientsDir = LocalContext.current.filesDir.resolve("clients")
    if(!clientsDir.exists()) clientsDir.mkdirs()
    val clients = clientsDir.list()
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
            if (clients != null) {
                clients.forEach {
                    SessionItem(it)
                }
            } else {

            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionItem(name: String) {
    ListItem(
        headlineText = { Text(name) },
        leadingContent = {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = name,
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
