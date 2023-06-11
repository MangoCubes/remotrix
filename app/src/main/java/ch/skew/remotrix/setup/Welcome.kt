package ch.skew.remotrix.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.skew.remotrix.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun Welcome(){
    Scaffold(
        topBar = {
            TopAppBar({
                Text(stringResource(R.string.welcome))
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ListItem(
                headlineText = { Text("What does this app do?") },
                supportingText = {
                    Text("This app will forward messages that arrive on this phone to various rooms over Matrix. Note that this app uses the space feature of Matrix to organise the rooms. Do look it up if you are not familiar with it.")
                    Text(
                        buildAnnotatedString {
                            append("Once your phone receives a message, the ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("bot account")
                            }
                            append(" will create a room under the messaging space, then invites the ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("manager account")
                            }
                            append(" into it.")
                        }
                    )
                }
            )
            Button(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                onClick = {}
            ) {
                Text("Continue")
            }
        }
    }
}