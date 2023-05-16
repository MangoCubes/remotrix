package ch.skew.remotrix

import android.content.Context
import android.widget.Toast
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
fun SetupScreen() {
    val formPadding = Modifier
        .fillMaxWidth()
        .padding(
            top = 10.dp,
            start = 10.dp,
            end = 10.dp
        )
    val context = LocalContext.current
    val settings = RemotrixSettings(context)
    val currentId = remember { mutableStateOf("") }
    val currentSpace = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar({
                Text(stringResource(R.string.setup_remotrix))
            })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineText = { Text(stringResource(R.string.welcome)) },
                supportingText = {
                    Text(stringResource(R.string.welcome_1))
                    Text(stringResource(R.string.welcome_2))
                    Text(stringResource(R.string.welcome_3))
                }
            )
            ListItem(
                headlineText = { Text(stringResource(R.string.set_admin_account)) },
                supportingText = {
                    Text(stringResource(R.string.welcome_4))
                }
            )
            TextField(
                value = currentId.value,
                onValueChange = { currentId.value = it },
                label = { Text(stringResource(R.string.manager_id)) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.example_account)) },
                modifier = formPadding
            )
            ListItem(
                headlineText = { Text(stringResource(R.string.set_remotrix_space)) },
                supportingText = {
                    Text(stringResource(R.string.welcome_5))
                }
            )
            TextField(
                value = currentSpace.value,
                onValueChange = { currentSpace.value = it },
                label = { Text(stringResource(R.string.existing_space_id)) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.sample_space_id)) },
                modifier = formPadding
            )
            Button(
                modifier = formPadding,
                content = { Text(stringResource(R.string.confirm)) },
                onClick = { onConfirm(currentId.value, currentSpace.value, context, scope, settings) }
            )
        }
    }
}

fun onConfirm(userId: String, spaceId: String, context: Context, scope: CoroutineScope, settings: RemotrixSettings){
    val idPattern = Regex("@[A-z0-9]+:[A-z0-9\\.]+")
    val spacePattern = Regex("![A-z]+:[A-z0-9\\.]+")
    if(idPattern.matchEntire(userId) === null){
        Toast.makeText(context, context.getString(R.string.invalid_id), Toast.LENGTH_SHORT).show()
        return
    }
    if(spacePattern.matchEntire(spaceId) === null){
        Toast.makeText(context, context.getString(R.string.invalid_space_id), Toast.LENGTH_SHORT).show()
        return
    }
    scope.launch {
        settings.saveId(userId)
        settings.saveSpaceId(spaceId)
    }
}