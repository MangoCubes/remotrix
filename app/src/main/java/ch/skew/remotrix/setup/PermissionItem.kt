package ch.skew.remotrix.setup

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun PermissionItemPreview() {
    PermissionItem("Permission", Manifest.permission.RECEIVE_MMS, true, {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionItem(
    name: String,
    permission: String,
    granted: Boolean,
    onInfoClick: () -> Unit
) {
    ListItem(
        headlineText = { Text(name) },
        leadingContent = {
            if(granted) Icon(
                Icons.Filled.CheckBox,
                contentDescription = "Permission granted",
            )
            else Icon(
                Icons.Filled.CheckBoxOutlineBlank,
                contentDescription = "Permission not granted"
            )
        },
        trailingContent = {
            IconButton(onInfoClick) {
                Icon(Icons.Filled.Info, contentDescription = "Permission description")
            }
        }
    )
}