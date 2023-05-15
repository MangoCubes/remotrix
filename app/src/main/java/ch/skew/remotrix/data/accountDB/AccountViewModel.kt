package ch.skew.remotrix.data.accountDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class AccountViewModel(
    private val dao: AccountDao
): ViewModel() {

    val accounts = dao.getAllAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onEventAsync(event: AccountEventAsync): Deferred<Long> {
        when(event){
            is AccountEventAsync.AddAccount -> {
                return viewModelScope.async {
                    dao.insert(event.userId, event.baseUrl)
                }
            }
        }

    }
    fun onEvent(event: AccountEvent){
        when(event){
            is AccountEvent.DeleteAccount -> {
                viewModelScope.launch {
                    dao.delete(event.account)
                }
            }

            is AccountEvent.ActivateAccount -> {
                viewModelScope.launch {
                    dao.activateAccount(event.id, event.domain, event.managementRoom)
                }
            }
        }
    }
}