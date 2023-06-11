package ch.skew.remotrix

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
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
import ch.skew.remotrix.data.RemotrixSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SetupScreen(
    done: () -> Unit = {},
    goBack: () -> Unit = {},
    openedBefore: Boolean = true
) {
    val context = LocalContext.current
    val settings = RemotrixSettings(context)
    val existingManagerId = settings.getManagerId.collectAsState(initial = "")
    val existingManagementSpaceId = settings.getManagementSpaceId.collectAsState(initial = "")
    val currentManagerId = remember { mutableStateOf("") }
    val currentManagementSpaceId = remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar({
                Text(stringResource(R.string.setup_remotrix))
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ListItem(
                headlineText = { Text(stringResource(R.string.welcome)) },
                supportingText = {
                    Text(stringResource(R.string.welcome_1))
                }
            )
            ListItem(
                headlineText = { Text(stringResource(R.string.set_admin_account)) },
                supportingText = {
                    Text(stringResource(R.string.welcome_2))
                }
            )
            TextField(
                value = currentManagerId.value,
                onValueChange = { currentManagerId.value = it },
                label = { Text(stringResource(R.string.manager_id)) },
                singleLine = true,
                placeholder = { Text(if(existingManagerId.value === "") stringResource(R.string.example_account) else existingManagerId.value) },
                modifier = Modifier.fillMaxWidth()
            )
            ListItem(
                headlineText = { Text(stringResource(R.string.management_space)) },
                supportingText = {
                    Text(stringResource(R.string.welcome_4))
                }
            )
            TextField(
                value = currentManagementSpaceId.value,
                onValueChange = { currentManagementSpaceId.value = it },
                label = { Text(stringResource(R.string.existing_space_id)) },
                singleLine = true,
                placeholder = { Text(if(existingManagementSpaceId.value === null) stringResource(R.string.sample_space_id) else existingManagementSpaceId.value!!) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                content = { Text(stringResource(R.string.confirm)) },
                onClick = {onConfirm(currentManagerId.value, currentManagementSpaceId.value, context, scope, settings, done) }
            )
            if(openedBefore) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    content = { Text(stringResource(R.string.cancel)) },
                    onClick = goBack
                )
            }
        }
    }
}

@Suppress("RegExpRedundantEscape")
fun onConfirm(userId: String, managementSpaceId: String?, context: Context, scope: CoroutineScope, settings: RemotrixSettings, done: () -> Unit){
    val idPattern = Regex("@[A-z0-9]+:[A-z0-9\\.]+")
    val spacePattern = Regex("![A-z]+:[A-z0-9\\.]+")
    if(idPattern.matchEntire(userId) === null){
        Toast.makeText(context, context.getString(R.string.invalid_id), Toast.LENGTH_SHORT).show()
        return
    }
    if(managementSpaceId !== null && spacePattern.matchEntire(managementSpaceId) === null){
        Toast.makeText(context, context.getString(R.string.invalid_space_id), Toast.LENGTH_SHORT).show()
        return
    }
    scope.launch {
        settings.saveManagerId(userId)
        settings.saveManagementSpaceId(managementSpaceId)
        settings.saveOpenedBefore()
        Toast.makeText(context, context.getString(R.string.setup_complete), Toast.LENGTH_SHORT).show()
        done()
    }
}