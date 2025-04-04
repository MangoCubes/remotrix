package ch.skew.remotrix.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import ch.skew.remotrix.R

@Composable
fun DelAccountDialog(
    close: () -> Unit,
    confirm: () -> Unit,
    accountId: String?
){
    accountId?.let {
        val enableButtons = remember{ mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = close,
            confirmButton = {
                Button(
                    onClick = {
                        enableButtons.value = false
                        confirm()
                    },
                    enabled = enableButtons.value
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(
                    onClick = close,
                    enabled = enableButtons.value
                ){
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(stringResource(R.string.del_account_title).format(it))
            },
            text = {
                Text(stringResource(R.string.del_account_help))
            }
        )
    }
}