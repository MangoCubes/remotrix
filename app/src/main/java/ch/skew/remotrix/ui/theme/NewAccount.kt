package ch.skew.remotrix.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.skew.remotrix.R

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun NewAccount(
    onClickGoBack: () -> Unit = {}
){
    val formPadding = Modifier
        .fillMaxWidth()
        .padding(
            top = 10.dp,
            start = 10.dp,
            end = 10.dp
        )
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.new_account)) },
                navigationIcon = {
                    IconButton(onClickGoBack) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.go_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val username = remember{ mutableStateOf("") }
            val password = remember{ mutableStateOf("") }
            val baseUrl = remember{ mutableStateOf("") }
            TextField(
                modifier = formPadding,
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text(stringResource(R.string.username)) },
                singleLine = true
            )
            TextField(
                modifier = formPadding,
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text(stringResource(R.string.password)) },
                singleLine = true
            )
            TextField(
                modifier = formPadding,
                value = baseUrl.value,
                onValueChange = { baseUrl.value = it },
                label = { Text(stringResource(R.string.homeserver_url_label)) },
                singleLine = true
            )
        }
    }
}