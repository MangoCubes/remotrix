package ch.skew.remotrix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.skew.remotrix.ui.theme.RemotrixTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun MainScreen() {
    RemotrixTheme {
        Scaffold(
            topBar = {
                TopAppBar({
                    Text(stringResource(R.string.app_name))
                })
            }
        ) { padding ->
            Box(Modifier.padding(padding)){
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(){
    Column {
        ListItem(
            headlineText = { Text(stringResource(R.string.manage_accounts)) },
            supportingText = { Text(stringResource(R.string.manage_accounts_desc)) },
            leadingContent = {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = stringResource(R.string.manage_accounts),
                )
            }
        )
        Divider()

    }
}