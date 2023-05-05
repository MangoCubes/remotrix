package ch.skew.remotrix

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun DelAccountDialog(
    close: () -> Unit,
    confirm: () -> Unit,
    accountId: String?
){
    if(accountId !== null){
        Dialog(
            onDismissRequest = close
        ) {
            Surface() {
                Text(text = "Delete account %1?")
            }
        }
    }
}