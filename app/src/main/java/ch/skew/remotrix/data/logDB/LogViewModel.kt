package ch.skew.remotrix.data.logDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class LogViewModel(
    private val dao: LogDao
): ViewModel() {

    val logs = dao.getLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onEvent(event: LogEvent){
        when(event){
            is LogEvent.AddFailure -> {
                viewModelScope.launch {
                    dao.insertError(event.status, event.errorMsg, event.msgType, event.senderId, event.payload)
                }
            }

            is LogEvent.AddSuccess -> {
                viewModelScope.launch {
                    dao.insertSuccess(event.status, event.msgType, event.senderId, event.payload)
                }
            }
        }
    }
}