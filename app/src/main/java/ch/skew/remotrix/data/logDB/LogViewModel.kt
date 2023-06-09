package ch.skew.remotrix.data.logDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class LogViewModel(
    private val dao: LogDao
): ViewModel() {

    val logs = dao.getLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onEventAsync(event: LogEventAsync): Deferred<Long> {
        when(event){
            is LogEventAsync.WriteAhead -> {
                return viewModelScope.async {
                    dao.writeAhead(event.msgType, event.senderId, event.payload)
                }
            }
        }
    }

    fun onEvent(event: LogEvent){
        when(event){
            is LogEvent.SetFailure -> {
                viewModelScope.launch {
                    dao.setFailure(event.id, event.status, event.errorMsg)
                }
            }

            is LogEvent.SetSuccess -> {
                viewModelScope.launch {
                    dao.setSuccess(event.id, event.status)
                }
            }
        }
    }
}