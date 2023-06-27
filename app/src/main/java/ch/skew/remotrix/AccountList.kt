package ch.skew.remotrix

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.components.DelAccountDialog
import ch.skew.remotrix.components.ScreenHelper
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.background.SendMsgWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.fromStore
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import net.folivo.trixnity.core.model.RoomId
import okio.Path.Companion.toPath
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
@Preview
fun AccountListPreview() {
    AccountList(listOf(), -1, {}, {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountList(
    accounts: List<Account>,
    defaultForwarder: Int,
    onClickGoBack: () -> Unit,
    onClickNewAccount: () -> Unit
){
    val askDel = remember{ mutableStateOf<Account?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    DelAccountDialog(close = { askDel.value = null }, confirm = {
        deleteAccount(context, scope, askDel.value) { askDel.value = null }
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
                if (defaultForwarder == -1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                append("You have not set any default forwarder or rules in the settings. ")
                                withStyle(style = SpanStyle(color = Color.Red)) {
                                    append("If you do not set one, this app will not forward any messages!")
                                }
                            }
                        )
                    }
                }
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
                Toast.makeText(context, context.getString(R.string.cannot_logout_no_account), Toast.LENGTH_LONG).show()
            } else {
                matrixClient.api.rooms.leaveRoom(RoomId(account.managementRoom))
                matrixClient.api.rooms.leaveRoom(RoomId(account.messageSpace))
                matrixClient.logout()
                Toast.makeText(context, context.getString(R.string.logout_successful), Toast.LENGTH_SHORT).show()
            }
            context.filesDir.resolve("clients").resolve("${account.userId}.mv.db").delete()
        } catch (e: Throwable) {
            Toast.makeText(context, context.getString(R.string.cannot_logout_data_not_found), Toast.LENGTH_LONG).show()
        }
        RemotrixDB.getInstance(context).accountDao.delete(account.id)
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
                        sendTestMessage(context, account)
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

fun sendTestMessage(context: Context, account: Account){
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val current = formatter.format(Calendar.getInstance().time)
    val work = OneTimeWorkRequestBuilder<SendMsgWorker>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                ).build()
        )
        .setInputData(
            Data.Builder()
                .putInt("senderId", account.id)
                .putInt("msgType", 1)
                .putStringArray("payload", arrayOf(account.managementRoom, context.getString(R.string.test_msg).format(current)))
                .build()
        )
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()
    Toast.makeText(context, context.getString(R.string.test_msg_sent).format(current), Toast.LENGTH_LONG).show()
    val workManager = WorkManager.getInstance(context)
    workManager.enqueue(work)
}