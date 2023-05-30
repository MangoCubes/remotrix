package ch.skew.remotrix

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.skew.remotrix.components.PasswordField
import ch.skew.remotrix.data.RemotrixSettings
import ch.skew.remotrix.data.accountDB.AccountEvent
import ch.skew.remotrix.data.accountDB.AccountEventAsync
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.login
import net.folivo.trixnity.client.media.okio.OkioMediaStore
import net.folivo.trixnity.client.store.repository.realm.createRealmRepositoriesModule
import net.folivo.trixnity.clientserverapi.model.authentication.IdentifierType
import net.folivo.trixnity.clientserverapi.model.rooms.DirectoryVisibility
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.space.ChildEventContent
import net.folivo.trixnity.core.model.events.m.space.ParentEventContent
import okio.Path.Companion.toPath

enum class VerificationStep {
    /**
     * Status before starting login
     */
    STANDBY,

    /**
     * Status just after pressing button and waiting for login to go through
     */
    STARTED,

    /**
     * Using this account to join the messaging space
     */
    JOINING_MESSAGING_SPACE,

    /**
     * Checking if this account have sufficient permission in the messaging space
     */
    VERIFYING_PERMISSIONS,

    /**
     * Creating a management room
     */
    CREATING_MANAGEMENT_ROOM,

    /**
     * Appending the management room under the management space as child
     */
    APPENDING_ROOM_AS_CHILD,

    /**
     * Inviting the manager to the room
     */
    INVITING_MANAGER
}

@Composable
@Preview
fun NewAccountPreview(){
    val scope = rememberCoroutineScope()
    NewAccount({}, {}, {scope.async {return@async 1}})
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccount(
    onClickGoBack: () -> Unit,
    onAccountEvent: (AccountEvent) -> Unit,
    onAccountEventAsync: (AccountEventAsync) -> Deferred<Long>
){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val step = remember { mutableStateOf(VerificationStep.STANDBY) }
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
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val username = remember{ mutableStateOf("") }
            val password = remember{ mutableStateOf("") }
            val baseUrl = remember{ mutableStateOf("") }
            val messageSpaceId = remember{ mutableStateOf("") }
            val revealPassword = remember{ mutableStateOf(false) }
            val enabled = remember{ mutableStateOf(true) }
            val errorMsg = remember{ mutableStateOf("") }
            val settings = RemotrixSettings(context)
            val managerId = settings.getManagerId.collectAsState(initial = "")
            val managementSpace = settings.getManagementSpaceId.collectAsState(initial = "")
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text(stringResource(R.string.username)) },
                singleLine = true,
                enabled = enabled.value
            )
            PasswordField(
                modifier = Modifier.fillMaxWidth(),
                value = password.value,
                onValueChange = { password.value = it },
                visibility = revealPassword.value,
                toggleVisibility = { revealPassword.value = !revealPassword.value },
                enabled = enabled.value
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = baseUrl.value,
                onValueChange = { baseUrl.value = it },
                label = { Text(stringResource(R.string.homeserver_url_label)) },
                singleLine = true,
                enabled = enabled.value
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = messageSpaceId.value,
                onValueChange = { messageSpaceId.value = it },
                label = { Text(stringResource(R.string.message_space_id)) },
                singleLine = true,
                enabled = enabled.value
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                content = { Text(stringResource(R.string.log_in)) },
                onClick = {
                    revealPassword.value = false
                    enabled.value = false
                    errorMsg.value = ""
                    onLoginClick(
                        context,
                        scope,
                        username.value,
                        password.value,
                        baseUrl.value,
                        managerId.value,
                        managementSpace.value,
                        messageSpaceId.value,
                        onAccountEventAsync,
                        {
                            errorMsg.value = it
                            enabled.value = true
                        },
                        onAccountEvent,
                        onClickGoBack,
                        { step.value = it }
                    )
                },
                enabled = enabled.value
            )
            if (errorMsg.value.isNotEmpty()) Text(errorMsg.value)
            Column {
                if(step.value === VerificationStep.STARTED)Text(stringResource(R.string.logging_in))
                if(step.value === VerificationStep.JOINING_MESSAGING_SPACE)Text(stringResource(R.string.joining_the_messaging_space))
                if(step.value === VerificationStep.VERIFYING_PERMISSIONS)Text(stringResource(R.string.checking_if_this_account_have_correct_permissions))
                if(step.value === VerificationStep.CREATING_MANAGEMENT_ROOM)Text(stringResource(R.string.creating_management_room))
                if(step.value === VerificationStep.APPENDING_ROOM_AS_CHILD)Text(stringResource(R.string.appending_management_room_as_child_room))
                if(step.value === VerificationStep.INVITING_MANAGER)Text(stringResource(R.string.inviting_manager_account))
            }
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
    managementSpaceId: String?,
    messagingSpace: String,
    addAccount: (AccountEventAsync) -> Deferred<Long>,
    abort: (String) -> Unit,
    onAccountEvent: (AccountEvent) -> Unit,
    onClickGoBack: () -> Unit,
    update: (VerificationStep) -> Unit
) {
    val baseUrl: String
    if (inputUrl === "") baseUrl = "https://matrix-client.matrix.org"
    else if (!inputUrl.startsWith("http")) baseUrl = "https://$inputUrl"
    else baseUrl = inputUrl
    val localpart = Regex("@([a-z0-9_.-]+):").find(username.lowercase())?.value ?: username.lowercase()
    scope.launch {
        update(VerificationStep.STARTED)
        val id = addAccount(AccountEventAsync.AddAccount(localpart, baseUrl, messagingSpace)).await()
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
        update(VerificationStep.JOINING_MESSAGING_SPACE)

        client.api.rooms.joinRoom(RoomId(messagingSpace)).getOrElse {
            abort(context.getString(R.string.cannot_join_message_space))
        }

        update(VerificationStep.CREATING_MANAGEMENT_ROOM)

        /**
         * Step 1: Room is created by the new account. It also claims to be child of the management space.
         * Step 2: This account attempts to append the new room as the management space's child.
         * Step 3-1: If it fails, new account leaves the room to delete it.
         * Step 3-2: If it succeeds, an invitation is sent to manager.
         */
        val via = setOf(client.userId.domain)
        val roomName = context.getString(R.string.management_room_name).format(client.userId)
        val roomId = client.api.rooms.createRoom(
            visibility = DirectoryVisibility.PRIVATE,
            name = roomName,
            topic = context.getString(R.string.management_room_desc).format(client.userId),
            initialState = if(managementSpaceId === null) null else listOf(
                Event.InitialStateEvent(
                    content = ParentEventContent(true, via),
                    stateKey = managementSpaceId
                )
            )
        ).getOrElse {
            // Assumes that room has not been created at all
            clientDir.deleteRecursively()
            client.logout()
            abort(it.message ?: context.getString(R.string.cannot_create_management_room))
            return@launch
        }
        // If room is created under a certain space, it needs to be registered under parent room
        if(managementSpaceId !== null){
            update(VerificationStep.APPENDING_ROOM_AS_CHILD)
            client.api.rooms.joinRoom(RoomId(managementSpaceId))
            client.api.rooms.sendStateEvent(RoomId(managementSpaceId), ChildEventContent(suggested = false, via = via), roomId.full).getOrElse {
                clientDir.deleteRecursively()
                client.api.rooms.leaveRoom(roomId)
                client.logout()
                abort(context.getString(R.string.no_child_space_permission).format(client.userId))
                return@launch
            }
        }
        update(VerificationStep.INVITING_MANAGER)
        client.api.rooms.inviteUser(roomId, UserId(managerId))
        Toast.makeText(context, context.getString(R.string.logged_in).format(client.userId), Toast.LENGTH_SHORT).show()
        onAccountEvent(AccountEvent.ActivateAccount(id, client.userId.domain, roomId.full))
        onClickGoBack()
    }
}