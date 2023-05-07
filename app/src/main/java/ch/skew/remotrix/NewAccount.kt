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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.skew.remotrix.components.PasswordField
import ch.skew.remotrix.data.Account
import ch.skew.remotrix.data.AccountEventAsync
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.login
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.exposed.createExposedRepositoriesModule
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import okio.Path.Companion.toPath
import org.jetbrains.exposed.sql.Database

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccount(
    onClickGoBack: () -> Unit,
    onAccountEventAsync: (AccountEventAsync) -> Unit
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
                    onLoginClick(context, scope, username.value, password.value, baseUrl.value, {
                        errorMsg.value = it
                        enabled.value = true
                    }, {
                        onAccountEventAsync(AccountEventAsync.AddAccount(it))
                        onClickGoBack()
                    })
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
    abort: (String) -> Unit,
    success: (Account) -> Unit
) {
    val baseUrl: String
    if (inputUrl === "") baseUrl = "https://matrix-client.matrix.org"
    else if (!inputUrl.startsWith("http")) baseUrl = "https://$inputUrl"
    else baseUrl = inputUrl
    val clientDir = context.filesDir.resolve("clients/${username}")
    clientDir.mkdirs()
    scope.launch {
        val repo = createExposedRepositoriesModule(Database.connect("jdbc:h2:${clientDir.resolve("data")}", "org.h2.Driver"))
        try{
            val client = MatrixClient.login(baseUrl = Url(baseUrl),
                identifier = IdentifierType.User(username),
                password = password,
                repositoriesModule = repo,
                mediaStore = OkioMediaStore(context.filesDir.resolve("clients/media").absolutePath.toPath()),
                scope = scope,
            ).getOrThrow()
            Toast.makeText(context, context.getString(R.string.logged_in).format(client.userId), Toast.LENGTH_SHORT).show()
            success(Account(client.userId.localpart, client.userId.domain, baseUrl))
        } catch (e: Throwable) {
            clientDir.deleteRecursively()
            abort(e.message ?: context.getString(R.string.generic_error))
        }
    }
}

/**
 * Results:
 * Realm: Cannot create multiple realms with same name, temp, then rename strategy does not work and the temp directory may be left empty, leading to invalid databases.
 * SQLite: Not supported
 * H2: Renaming DB causes crash, and the program automatically adds .mv.db extension at the end, so renaming is annoying
 *
 * Potential solution: Add ID to DB, use ID as folder names to ensure uniqueness
 */