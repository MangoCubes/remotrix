package ch.skew.remotrix

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.matrix.android.sdk.api.session.Session

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountList(
    session: Session?,
    onClickGoBack: () -> Unit,
    onClickNewAccount: () -> Unit,
    unsetSession: () -> Unit
){
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.account_list)) },
                navigationIcon = {
                    IconButton(onClickGoBack) {
                        Icon(Icons.Filled.ArrowBack, stringResource(R.string.go_back))
                    }
                },
                actions = {
                    IconButton(onClickNewAccount) {
                        Icon(Icons.Filled.Add, stringResource(R.string.add_new_account))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SessionItem(session, unsetSession)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionItem(session: Session?, unsetSession: () -> Unit) {
    if(session === null) Text(stringResource(R.string.add_account_help))
    else ListItem(
        headlineText = { Text(session.myUserId) },
        supportingText = { Text(session.sessionParams.homeServerUrl) },
        leadingContent = {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = session.myUserId,
            )
        },
        trailingContent = {
            IconButton(onClick = unsetSession) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = session.myUserId,
                )
            }
        }
    )
}
