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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalInfo(
    nextPage: () -> Unit
){
    Scaffold(
        topBar = {
            TopAppBar({
                Text("Setup Complete!")
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
                headlineText = { Text("Now what?") },
                supportingText = {
                    Text("Basic setup is now complete.")
                    Text(
                        buildAnnotatedString {
                            append("You must set at least one ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("bot account")
                            }
                            append(" to forward the messages, and set it as default account in the settings. ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Default forwarder bot is not set by default, meaning if you do not set it, messages will not be forwarded!")
                            }
                        }
                    )
                    Text("Once you complete these two, you should now have messages forwarded to you.")
                }
            )
            Button(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                onClick = nextPage
            ) {
                Text("Done")
            }
        }
    }
}