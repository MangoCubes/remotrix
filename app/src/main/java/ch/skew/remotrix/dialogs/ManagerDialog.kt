package ch.skew.remotrix.dialogs

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.skew.remotrix.R
import ch.skew.remotrix.data.RemotrixSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDialog(
    isOpen: Boolean,
    close: () -> Unit
) {
    if(isOpen){
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val settings = RemotrixSettings(context)
        val managerId = settings.getId.collectAsState(initial = "")
        val currentId = remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = close,
            confirmButton = {
                Button({onConfirm(context, scope, close, settings, currentId.value)}) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                Button(close){
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(stringResource(R.string.choose_manager_account))
            },
            text = {
                TextField(
                    value = currentId.value,
                    onValueChange = { currentId.value = it },
                    label = { Text(stringResource(R.string.manager_id)) },
                    singleLine = true,
                    placeholder = { Text(managerId.value) }
                )
            }
        )
    }
}

fun onConfirm(context: Context, scope: CoroutineScope, close: () -> Unit, settings: RemotrixSettings, currentId: String){
    // Temporary solution: This pattern does not necessarily filter all invalid IDs.
    val idPattern = Regex("@[A-z0-9]+:[A-z0-9\\.]+")
    if(idPattern.matchEntire(currentId) === null){
        Toast.makeText(context, context.getString(R.string.invalid_id), Toast.LENGTH_SHORT).show()
        return
    }
    scope.launch {
        settings.saveId(currentId)
    }
    close()
}