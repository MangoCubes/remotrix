package ch.skew.remotrix

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    LocalContext.current.filesDir.resolve("clients").mkdirs()
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
            SessionItem()
        }
    }
}

//fun loadClient(scope: CoroutineScope, context: Context) {
//
//    Log.i("T", context.filesDir.resolve("clients").list().joinToString(separator = ", "))
//
//    scope.launch {
//        val client = MatrixClient.fromStore(
//            repositoriesModule = createRealmRepositoriesModule(),
//            mediaStore = OkioMediaStore(context.filesDir.absolutePath.toPath()),
//            scope = scope,
//        ).getOrThrow()
//        Toast.makeText(context, "${client?.userId}", Toast.LENGTH_LONG).show()
//    }
//}
@Composable
fun SessionItem() {
//    if(session === null) Text(stringResource(R.string.add_account_help))
//    else ListItem(
//        headlineText = { Text(session.myUserId) },
//        supportingText = { Text(session.sessionParams.homeServerUrl) },
//        leadingContent = {
//            Icon(
//                Icons.Filled.AccountCircle,
//                contentDescription = session.myUserId,
//            )
//        },
//        trailingContent = {
//            IconButton(onClick = unsetSession) {
//                Icon(
//                    Icons.Filled.Delete,
//                    contentDescription = session.myUserId,
//                )
//            }
//        }
//    )
}
