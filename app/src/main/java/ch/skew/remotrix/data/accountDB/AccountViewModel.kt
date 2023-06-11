package ch.skew.remotrix.data.accountDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class AccountViewModel(
    private val dao: AccountDao
): ViewModel() {
    val accounts = dao.getAllAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}