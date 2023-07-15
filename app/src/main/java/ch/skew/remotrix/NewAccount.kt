package ch.skew.remotrix

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import ch.skew.remotrix.background.CommandService
import ch.skew.remotrix.components.LabelledRadioButton
import ch.skew.remotrix.components.PasswordField
import ch.skew.remotrix.data.RemotrixDB
import ch.skew.remotrix.data.RemotrixSettings
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
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
import net.folivo.trixnity.core.model.events.m.room.CreateEventContent
import net.folivo.trixnity.core.model.events.m.room.EncryptionEventContent
import net.folivo.trixnity.core.model.events.m.room.HistoryVisibilityEventContent
import net.folivo.trixnity.core.model.events.m.space.ChildEventContent
import net.folivo.trixnity.core.model.events.m.space.ParentEventContent
import net.folivo.trixnity.core.model.keys.EncryptionAlgorithm
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
     * If no messaging space was selected during login, a space will be created for the user instead.
     */
    CREATING_MESSAGING_SPACE,

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
    NewAccount {}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccount(
    onClickGoBack: () -> Unit
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
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
                .verticalScroll(scroll),
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
            val autoMsgSpace = remember { mutableStateOf(true) }
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
            Column {
                LabelledRadioButton(label = stringResource(R.string.auto), selected = autoMsgSpace.value) {
                    autoMsgSpace.value = true
                }
                LabelledRadioButton(label = stringResource(R.string.manual), selected = !autoMsgSpace.value) {
                    autoMsgSpace.value = false
                }
            }
            if(!autoMsgSpace.value) TextField(
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
                        if (autoMsgSpace.value) null else messageSpaceId.value,
                        {
                            errorMsg.value = it
                            enabled.value = true
                        },
                        onClickGoBack,
                        { step.value = it }
                    )
                },
                enabled = enabled.value && username.value.isNotEmpty() && password.value.isNotEmpty() && (autoMsgSpace.value || messageSpaceId.value.isNotEmpty())
            )
            if (errorMsg.value.isNotEmpty()) Text(errorMsg.value)
            Column {
                if(step.value === VerificationStep.STARTED)Text(stringResource(R.string.logging_in))
                if(step.value === VerificationStep.JOINING_MESSAGING_SPACE)Text(stringResource(R.string.joining_the_messaging_space))
                if(step.value === VerificationStep.VERIFYING_PERMISSIONS)Text(stringResource(R.string.checking_if_this_account_have_correct_permissions))
                if(step.value === VerificationStep.CREATING_MANAGEMENT_ROOM)Text(stringResource(R.string.creating_management_room))
                if(step.value === VerificationStep.APPENDING_ROOM_AS_CHILD)Text(stringResource(R.string.appending_management_room_as_child_room))
                if(step.value === VerificationStep.INVITING_MANAGER)Text(stringResource(R.string.inviting_manager_account))
                if(step.value === VerificationStep.CREATING_MESSAGING_SPACE)Text(stringResource(R.string.creating_messaging_space))
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
    messagingSpaceInput: String?,
    abort: (String) -> Unit,
    onClickGoBack: () -> Unit,
    update: (VerificationStep) -> Unit
) {
    val baseUrl: String = if (inputUrl === "") "https://matrix-client.matrix.org"
    else if (!inputUrl.startsWith("http")) "https://$inputUrl"
    else inputUrl
    val localPart =
        Regex("@([a-z0-9_.-]+):").find(username.lowercase())?.value ?: username.lowercase()
    scope.launch {
        update(VerificationStep.STARTED)
        val accountDao = RemotrixDB.getInstance(context).accountDao
        val id = accountDao.insert(localPart, baseUrl)
        val clientDir = context.filesDir.resolve("clients/${id}")
        clientDir.mkdirs()
        val repo = createRealmRepositoriesModule {
            this.directory(clientDir.toString())
        }
        val client = MatrixClient.login(
            baseUrl = Url(baseUrl),
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
        // The "via" part of m.space.child/parent event.
        val via = setOf(client.userId.domain)
        val msgSpace: RoomId
        if(messagingSpaceInput === null) {
            update(VerificationStep.CREATING_MESSAGING_SPACE)
            msgSpace = client.api.rooms.createRoom(
                visibility = DirectoryVisibility.PRIVATE,
                name = context.getString(R.string.sms_forwarder),
                creationContent = CreateEventContent(
                    type = CreateEventContent.RoomType.Space,
                    creator = client.userId
                ),
                initialState = listOf(
                    Event.InitialStateEvent(
                        content = HistoryVisibilityEventContent(HistoryVisibilityEventContent.HistoryVisibility.SHARED),
                        stateKey = ""
                    )
                )
            ).getOrElse {
                clientDir.deleteRecursively()
                abort(it.message ?: context.getString(R.string.generic_error))
                return@launch
            }
            client.api.rooms.inviteUser(msgSpace, UserId(managerId)).getOrElse {
                client.api.rooms.leaveRoom(msgSpace)
                clientDir.deleteRecursively()
                abort(it.message ?: context.getString(R.string.generic_error))
                return@launch
            }
        } else {
            update(VerificationStep.JOINING_MESSAGING_SPACE)
            msgSpace = RoomId(messagingSpaceInput)
            client.api.rooms.joinRoom(msgSpace)
            val testRoom = client.api.rooms.createRoom(
                visibility = DirectoryVisibility.PRIVATE,
                name = "Test room",
                initialState = listOf(
                    // Initial event for making this room child of the message room
                    Event.InitialStateEvent(
                        content = ParentEventContent(true, via),
                        stateKey = messagingSpaceInput
                    ),
                    Event.InitialStateEvent(
                        content = HistoryVisibilityEventContent(HistoryVisibilityEventContent.HistoryVisibility.SHARED),
                        stateKey = ""
                    )
                )
            ).getOrElse {
                client.logout()
                clientDir.deleteRecursively()
                abort(
                    context.getString(R.string.cannot_create_room_in_the_messaging_space) + (it.message
                        ?: context.getString(R.string.generic_error))
                )
                return@launch
            }
            // This state ensures that the parent room recognises the child room as its child.
            client.api.rooms.sendStateEvent(
                msgSpace,
                ChildEventContent(suggested = false, via = via),
                testRoom.full
            ).getOrElse {
                // Forwarder leaves the room to ensure it is removed in case state cannot be set.
                client.api.rooms.leaveRoom(testRoom)
                client.logout()
                clientDir.deleteRecursively()
                abort(
                    context.getString(R.string.child_room_creation_failed) + (it.message
                        ?: context.getString(R.string.generic_error))
                )
                return@launch
            }

            client.api.rooms.sendStateEvent(
                testRoom, ChildEventContent(), testRoom.full
            )
            client.api.rooms.leaveRoom(testRoom)
        }
        update(VerificationStep.CREATING_MANAGEMENT_ROOM)
        /**
         * Step 1: Room is created by the new account. It also claims to be child of the management space.
         * Step 2: This account attempts to append the new room as the management space's child.
         * Step 3-1: If it fails, new account leaves the room to delete it.
         * Step 3-2: If it succeeds, an invitation is sent to manager.
         */
        val roomName = context.getString(R.string.management_room_name).format(client.userId)
        val initState = mutableListOf<Event.InitialStateEvent<*>>(
            Event.InitialStateEvent(
                content = HistoryVisibilityEventContent(HistoryVisibilityEventContent.HistoryVisibility.SHARED),
                stateKey = ""
            ),
            Event.InitialStateEvent(
                content = EncryptionEventContent(
                    algorithm = EncryptionAlgorithm.Megolm
                ),
                stateKey = ""
            )
        )
        if(managementSpaceId !== null) initState.add(
            Event.InitialStateEvent(
                content = ParentEventContent(true, via),
                stateKey = managementSpaceId
            )
        )
        val roomId = client.api.rooms.createRoom(
            visibility = DirectoryVisibility.PRIVATE,
            name = roomName,
            topic = context.getString(R.string.management_room_desc).format(client.userId),
            initialState = initState
        ).getOrElse {
            client.logout()
            // Assumes that room has not been created at all
            clientDir.deleteRecursively()
            abort(it.message ?: context.getString(R.string.cannot_create_management_room))
            return@launch
        }
        // If room is created under a certain space, it needs to be registered under parent room
        if (managementSpaceId !== null) {
            update(VerificationStep.APPENDING_ROOM_AS_CHILD)
            client.api.rooms.joinRoom(RoomId(managementSpaceId))
            client.api.rooms.sendStateEvent(
                RoomId(managementSpaceId),
                ChildEventContent(suggested = false, via = via),
                roomId.full
            ).getOrElse {
                clientDir.deleteRecursively()
                client.api.rooms.leaveRoom(roomId)
                client.logout()
                abort(context.getString(R.string.no_child_space_permission).format(client.userId))
                return@launch
            }
        }
        update(VerificationStep.INVITING_MANAGER)
        client.api.rooms.inviteUser(roomId, UserId(managerId))
        Toast.makeText(
            context,
            context.getString(R.string.logged_in).format(client.userId),
            Toast.LENGTH_SHORT
        ).show()
        accountDao.activateAccount(id, client.userId.domain, roomId.full, msgSpace.full)
        Intent(context, CommandService::class.java)
            .apply {
                action = CommandService.RELOAD
                context.startService(this)
            }
        onClickGoBack()
    }
}