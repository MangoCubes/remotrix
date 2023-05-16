package ch.skew.remotrix

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SetupScreen() {
    Scaffold(
        topBar = {
            TopAppBar({
                Text(stringResource(R.string.setup_remotrix))
            })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineText = { Text(stringResource(R.string.welcome)) },
                supportingText = {
                    Text(stringResource(R.string.welcome_1))
                    Text(stringResource(R.string.welcome_2))
                    Text(stringResource(R.string.welcome_3))
                    Text(stringResource(R.string.welcome_4))
                    Text(stringResource(R.string.welcome_5))
                }
            )

        }
    }
}