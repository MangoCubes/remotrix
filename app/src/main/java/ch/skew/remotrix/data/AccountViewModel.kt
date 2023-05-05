package ch.skew.remotrix.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class AccountViewModel(
    private val dao: AccountDao
): ViewModel() {

    val accounts = dao.getAllAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onEvent(event: AccountEvent){
        when(event){
            is AccountEvent.AddAccount -> {
                viewModelScope.launch {
                    dao.insert(event.account)
                }
            }
            is AccountEvent.DeleteAccount -> {
                viewModelScope.launch {
                    dao.delete(event.account)
                }
            }
        }
    }
}