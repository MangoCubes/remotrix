package ch.skew.remotrix

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.skew.remotrix.components.PasswordField
import ch.skew.remotrix.data.RemotrixSettings
import ch.skew.remotrix.data.accountDB.AccountEvent
import ch.skew.remotrix.data.accountDB.AccountEventAsync
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.login
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import net.folivo.trixnity.clientserverapi.model.rooms.DirectoryVisibility
import net.folivo.trixnity.core.model.UserId
import okio.Path.Companion.toPath

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccount(
    onClickGoBack: () -> Unit,
    onAccountEvent: (AccountEvent) -> Unit,
    onAccountEventAsync: (AccountEventAsync) -> Deferred<Long>
){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val formPadding = Modifier
        .fillMaxWidth()
        .padding(
            top = 10.dp,
            start = 10.dp,
            end = 10.dp
        )
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.new_account)) },
                navigationIcon = {
                    IconButton(onClickGoBack) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.go_back))
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
            val username = remember{ mutableStateOf("") }
            val password = remember{ mutableStateOf("") }
            val baseUrl = remember{ mutableStateOf("") }
            val revealPassword = remember{ mutableStateOf(false) }
            val enabled = remember{ mutableStateOf(true) }
            val errorMsg = remember{ mutableStateOf("") }
            val settings = RemotrixSettings(context)
            val managerId = settings.getId.collectAsState(initial = "")
            TextField(
                modifier = formPadding,
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text(stringResource(R.string.username)) },
                singleLine = true,
                enabled = enabled.value
            )
            PasswordField(
                modifier = formPadding,
                value = password.value,
                onValueChange = { password.value = it },
                visibility = revealPassword.value,
                toggleVisibility = { revealPassword.value = !revealPassword.value },
                enabled = enabled.value
            )
            TextField(
                modifier = formPadding,
                value = baseUrl.value,
                onValueChange = { baseUrl.value = it },
                label = { Text(stringResource(R.string.homeserver_url_label)) },
                singleLine = true,
                enabled = enabled.value
            )
            Button(
                modifier = formPadding,
                content = { Text(stringResource(R.string.log_in)) },
                onClick = {
                    revealPassword.value = false
                    enabled.value = false
                    errorMsg.value = ""
                    onLoginClick(context, scope, username.value, password.value, baseUrl.value, managerId.value, { id, baseUrl ->
                        onAccountEventAsync(AccountEventAsync.AddAccount(id, baseUrl))
                    },
                    {
                        errorMsg.value = it
                        enabled.value = true
                    }, onAccountEvent, onClickGoBack)
                },
                enabled = enabled.value
            )
            Text(errorMsg.value, modifier = formPadding)
        }
    }
}
fun onLoginClick(
    context: Context,
    scope: CoroutineScope,
    username: String,
    password: String,
    inputUrl: String,
    managerId: String,
    addAccount: (String, String) -> Deferred<Long>,
    abort: (String) -> Unit,
    onAccountEvent: (AccountEvent) -> Unit,
    onClickGoBack: () -> Unit
) {
    val baseUrl: String
    if (inputUrl === "") baseUrl = "https://matrix-client.matrix.org"
    else if (!inputUrl.startsWith("http")) baseUrl = "https://$inputUrl"
    else baseUrl = inputUrl
    scope.launch {
        val id = addAccount(username, baseUrl).await()
        val clientDir = context.filesDir.resolve("clients/${id}")
        clientDir.mkdirs()
        val repo = createRealmRepositoriesModule {
            this.directory(clientDir.toString())
        }
        val client = MatrixClient.login(baseUrl = Url(baseUrl),
            identifier = IdentifierType.User(username),
            password = password,
            repositoriesModule = repo,
            mediaStore = OkioMediaStore(context.filesDir.resolve("clients/media").absolutePath.toPath()),
            scope = scope,
        ).getOrElse {
            clientDir.deleteRecursively()
            abort(it.message ?: context.getString(R.string.generic_error))
            return@launch
        }
        client.api.rooms.createRoom(
            visibility = DirectoryVisibility.PRIVATE,
            name = context.getString(R.string.management_room_name).format(client.userId),
            topic = context.getString(R.string.management_room_desc).format(client.userId),
            invite = setOf(UserId(managerId))
        ).getOrElse {
            clientDir.deleteRecursively()
            abort(it.message ?: context.getString(R.string.cannot_create_management_room))
            return@launch
        }
        Toast.makeText(context, context.getString(R.string.logged_in).format(client.userId), Toast.LENGTH_SHORT).show()
        onAccountEvent(AccountEvent.ActivateAccount(id, client.userId.domain))
        onClickGoBack()
    }
}