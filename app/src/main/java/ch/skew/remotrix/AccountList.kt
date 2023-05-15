package ch.skew.remotrix

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import ch.skew.remotrix.components.ScreenHelper
import ch.skew.remotrix.data.accountDB.AccountEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import net.folivo.trixnity.core.model.RoomId
import okio.Path.Companion.toPath

@Composable
@Preview
fun AccountListPreview() {
    AccountList(listOf(), {}, {}, {})
}

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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (accounts.isNotEmpty()) {
                accounts.forEach {
                    SessionItem(it, context) { account -> askDel.value = account }
                }
            } else {
                ScreenHelper(stringResource(R.string.add_account_help))
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
            val clientDir = context.filesDir.resolve("clients/${account.id}")
            val repo = createRealmRepositoriesModule {
                this.directory(clientDir.toString())
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
            context.filesDir.resolve("clients").resolve("${account.userId}.mv.db").delete()
        } catch (e: Throwable) {
            Toast.makeText(context, "Cannot locate account data. Account will be removed from device only.", Toast.LENGTH_LONG).show()
        }
        onAccountEvent(AccountEvent.DeleteAccount(account.id))
        close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionItem(
    account: Account,
    context: Context,
    delAccount: (Account) -> Unit
) {
    val open = remember{ mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    ListItem(
        headlineText = { Text(account.fullName()) },
        supportingText = { Text(account.baseUrl) },
        leadingContent = {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = account.fullName(),
            )
        },
        trailingContent = {
            IconButton(onClick = { open.value = true } ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.account_options),
                )
            }
            DropdownMenu(expanded = open.value, onDismissRequest = { open.value = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.send_test_message)) },
                    onClick = {
                        open.value = false
                        sendTestMessage(context, scope, account)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete)) },
                    onClick = {
                        open.value = false
                        delAccount(account)
                    }
                )
            }
        }
    )
}

fun sendTestMessage(context: Context, scope: CoroutineScope, account: Account){
    val clientDir = context.filesDir.resolve("clients/${account.id}")
    scope.launch {
        val client = MatrixClient.fromStore(
            repositoriesModule = createRealmRepositoriesModule {
                this.directory(clientDir.toString())
            },
            mediaStore = OkioMediaStore(context.filesDir.resolve("clients/media").absolutePath.toPath()),
            scope = scope
        ).getOrNull()
        if(client !== null){
            client.startSync()
            client.room.sendMessage(RoomId("")) {
                text(context.getString(R.string.test_msg))
            }
            Toast.makeText(context, context.getString(R.string.test_msg_sent), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, context.getString(R.string.failed_to_send_message), Toast.LENGTH_SHORT).show()
        }
    }
}