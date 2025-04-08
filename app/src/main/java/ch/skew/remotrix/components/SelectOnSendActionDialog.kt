package ch.skew.remotrix.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import ch.skew.remotrix.R
import ch.skew.remotrix.data.RemotrixSettings.OnSend
import ch.skew.remotrix.data.onSendToString

@Composable
fun SelectOnSendActionDialog(
    close: () -> Unit,
    confirm: (OnSend) -> Unit,
    title: String,
    show: Boolean,
    defaultSelected: OnSend,
){
    val chosen = remember { mutableStateOf(defaultSelected) }
    if(show) {
        AlertDialog(
            onDismissRequest = close,
            confirmButton = {
                Button({ confirm(chosen.value) }) {
                    Text(stringResource(R.string.choose))
                }
            },
            dismissButton = {
                Button(close){
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(title)
            },
            text = {
                Column {
                    arrayOf(OnSend.Thread, OnSend.React, OnSend.Reply, OnSend.None).forEach {
                        LabelledRadioButton(onSendToString(it), it == chosen.value) {
                            chosen.value = it
                        }
                    }
                }
            }
        )
    }
}
