package ch.skew.remotrix.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import ch.skew.remotrix.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField (
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    visibility: Boolean,
    toggleVisibility: () -> Unit
) {
    TextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.password)) },
        singleLine = true,
        visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = toggleVisibility) {
                if (visibility) Icon(Icons.Filled.VisibilityOff, stringResource(R.string.hide_password))
                else Icon(Icons.Filled.Visibility, stringResource(R.string.show_password))
            }
        }
    )
}