package ch.skew.remotrix

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.skew.remotrix.classes.Account
import ch.skew.remotrix.classes.MsgStatus
import ch.skew.remotrix.data.logDB.LogData

@Preview
@Composable
fun LogsPreview(){
    Logs(listOf(), listOf(LogData(1, "9:41", MsgStatus.MESSAGE_SENT, null, 1, 1, "hello world"), LogData(2, "9:42", MsgStatus.CANNOT_CREATE_CHILD_ROOM, "asdf", 2, 1, "hello world")), true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Logs(
    accounts: List<Account>,
    logs: List<LogData>,
    isEnabled: Boolean,
    goBack: () -> Unit = {}
){
    val hideSuccesses = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar({
                Text(stringResource(R.string.logs))
            }, navigationIcon = {
                IconButton(goBack) {
                    Icon(Icons.Filled.ArrowBack, stringResource(R.string.go_back))
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
        ) {
            if(!isEnabled) Text(stringResource(R.string.logging_disabled_alert))
            Row(
                modifier = Modifier
                    .clickable(onClick = { hideSuccesses.value = !hideSuccesses.value })
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.hide_successful_sends))
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Switch(
                    checked = hideSuccesses.value,
                    onCheckedChange = null
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(logs) { log ->
                    val success =
                        log.status === MsgStatus.MESSAGE_SENT || log.status === MsgStatus.MESSAGE_DROPPED
                    if (!(success && hideSuccesses.value)) {
                        val msg = when(log.status){
                            MsgStatus.MESSAGE_SENDING_FAILED -> stringResource(R.string.message_sending_failed)
                            MsgStatus.MESSAGE_SENT -> stringResource(R.string.message_sent)
                            MsgStatus.MESSAGE_DROPPED -> stringResource(R.string.message_dropped)
                            MsgStatus.NO_SUITABLE_FORWARDER -> stringResource(R.string.no_suitable_forwarder)
                            MsgStatus.CANNOT_LOAD_MATRIX_CLIENT -> stringResource(R.string.cannot_load_matrix_client)
                            MsgStatus.CANNOT_CREATE_ROOM -> stringResource(R.string.cannot_create_room)
                            MsgStatus.CANNOT_CREATE_CHILD_ROOM -> stringResource(R.string.cannot_create_child_room)
                            MsgStatus.MESSAGE_MAX_ATTEMPTS_REACHED -> stringResource(R.string.message_max_attempts_reached)
                        }
                        val forwarderInfo = if (log.forwarderId === null) ""
                        else {
                            val accountUsed = accounts.find { it.id == log.forwarderId }
                            if (accountUsed === null) "(Forwarder: Unknown (ID: %s))".format(
                                log.forwarderId
                            )
                            else "(Forwarder: %s)".format(accountUsed.userId)
                        }
                        Text("[${if (success) stringResource(R.string.success) else stringResource(R.string.failure)}] " + log.timestamp + ": " + msg + " " + forwarderInfo)
                    }
                }
            }
        }
    }
}