package ch.skew.remotrix.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.skew.remotrix.R
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.classes.MsgStatus
import ch.skew.remotrix.classes.MsgType
import ch.skew.remotrix.data.logDB.LogData

@Preview
@Composable
fun LogViewerDialogPreview(){
    LogViewerDialog(
        log = LogData(
            id = 3,
            timestamp = "11223344",
            status = MsgStatus.CANNOT_CREATE_ROOM,
            errorMsg = null,
            msgType = MsgType.TestMessage,
            payload = "Test message",
            forwarderId = 2
        ),
        close = {},
        account = null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerDialog(
    log: LogData?,
    close: () -> Unit,
    account: Account?
) {
    if (log !== null) {
        AlertDialog(
            onDismissRequest = close,
            confirmButton = {},
            dismissButton = {
                Button(
                    onClick = close
                ){
                    Text(stringResource(R.string.close))
                }
            },
            title = {
                Text(stringResource(R.string.log_viewer_title).format(log.id))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ListItem(
                        headlineText = { Text(stringResource(R.string.message_status)) },
                        supportingText = { Text(stringResource(id = MsgStatus.translateStatus(log.status))) }
                    )
                    ListItem(
                        headlineText = { Text(stringResource(R.string.sent_at)) },
                        supportingText = { Text(log.timestamp) },
                    )
                    if (log.errorMsg !== null) ListItem(
                        headlineText = { Text(stringResource(R.string.error_message)) },
                        supportingText = { Text(log.errorMsg) },
                    )
                    ListItem(
                        headlineText = { Text(stringResource(R.string.message_type)) },
                        supportingText = { Text(
                            when(log.msgType) {
                                MsgType.TestMessage -> stringResource(R.string.test_message)
                                MsgType.SMSForwarding -> stringResource(R.string.sms_message)
                            }
                        ) }
                    )
                    ListItem(
                        headlineText = { Text(stringResource(R.string.message_sent_as)) },
                        supportingText = {
                            Text(
                                if(account === null) stringResource(R.string.unknown_sender)
                                else account.userId
                            )
                        }
                    )
                    ListItem(
                        headlineText = { Text(stringResource(R.string.message_content)) },
                        supportingText = { Text(log.payload) }
                    )
                }
            }
        )
    }
}