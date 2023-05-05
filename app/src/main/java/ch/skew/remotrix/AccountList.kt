package ch.skew.remotrix

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import ch.skew.remotrix.data.Account
import ch.skew.remotrix.data.AccountEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import okio.Path.Companion.toPath

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountList(
    accounts: List<Account>,
    onAccountEvent: (AccountEvent) -> Unit,
    onClickGoBack: () -> Unit,
    onClickNewAccount: () -> Unit
){
    val askDel = remember{ mutableStateOf<Account?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    DelAccountDialog(close = { askDel.value = null }, confirm = {
        deleteAccount(context, scope, askDel.value, onAccountEvent) { askDel.value = null }
    }, accountId = askDel.value?.userId)
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
                SessionItem(it) { account -> askDel.value = account }
            }
        }
    }
}

fun deleteAccount(
    context: Context,
    scope: CoroutineScope,
    account: Account?,
    onAccountEvent: (AccountEvent) -> Unit,
    close: () -> Unit
) {
    if (account === null) return
    scope.launch {
        try {
            val dir = context.filesDir.resolve("clients").resolve(account.userId)
            val repo = createRealmRepositoriesModule {
                this.directory(dir.toString())
            }
            val mediaStore = OkioMediaStore(context.filesDir.resolve("clients/media").absolutePath.toPath())
            val matrixClient = MatrixClient.fromStore(
                repositoriesModule = repo,
                mediaStore = mediaStore,
                scope = scope,
            ).getOrThrow()
            if(matrixClient === null) {
                Toast.makeText(context, "Cannot logout. Account will be removed from device only.", Toast.LENGTH_LONG).show()
            } else {
                matrixClient.logout()
                Toast.makeText(context, "Logout successful.", Toast.LENGTH_SHORT).show()
            }
            dir.deleteRecursively()
        } catch (e: Throwable) {
            Toast.makeText(context, "Cannot locate account data. Account will be removed from device only.", Toast.LENGTH_LONG).show()
        }
        onAccountEvent(AccountEvent.DeleteAccount(account))
        close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionItem(
    account: Account,
    delAccount: (Account) -> Unit
) {
    ListItem(
        headlineText = { Text(account.userId) },
        supportingText = { Text(account.homeServer) },
        leadingContent = {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = account.homeServer,
            )
        },
        trailingContent = {
            IconButton(onClick = { delAccount(account)} ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete_account),
                )
            }
        }
    )
}
