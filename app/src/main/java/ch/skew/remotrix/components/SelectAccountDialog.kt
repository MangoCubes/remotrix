package ch.skew.remotrix.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.skew.remotrix.R
import ch.skew.remotrix.classes.Account

@Composable
@Preview
fun SelectAccountDialogPreview(){
    SelectAccountDialog(
        accounts = listOf(Account(1, "temp", "matrix.org", "example.com", "!room:example.com")),
        close = {},
        confirm = {},
        title = "Please select an account",
        noneChosenDesc = "Choose none",
        show = true,
        defaultSelected = 1
    )
}

@Composable
fun SelectAccountDialog(
    accounts: List<Account>,
    close: () -> Unit,
    confirm: (Int?) -> Unit,
    title: String,
    noneChosenDesc: String?,
    show: Boolean,
    defaultSelected: Int?
){
    val chosen = remember { mutableStateOf<Int?>(defaultSelected) }
    if(show) {
        AlertDialog(
            onDismissRequest = close,
            confirmButton = {
                Button({ confirm(chosen.value) },
                    enabled = (noneChosenDesc === null && chosen.value !== null) || (noneChosenDesc !== null)
                ) {
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
                Column() {
                    accounts.forEach {
                        @Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS")
                        AccountRow(
                            it,
                            chosen.value !== null && it.id === chosen.value
                        ) { chosen.value = it.id }
                    }
                    if (noneChosenDesc !== null) Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = chosen.value === null,
                                onClick = { chosen.value = null },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = chosen.value === null,
                            onClick = null
                        )
                        Text(
                            text = noneChosenDesc,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun AccountRow(account: Account, selected: Boolean, onSelect: () -> Unit){
    /**
     * Recommended modifier from Android Compose website
     */
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = account.fullName(),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}