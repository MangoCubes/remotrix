package ch.skew.remotrix

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun DelAccountDialog(
    close: () -> Unit,
    confirm: () -> Unit,
    accountId: String?
){
    accountId?.let {
        AlertDialog(
            onDismissRequest = close,
            confirmButton = {
                Button(confirm) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                Button(close){
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