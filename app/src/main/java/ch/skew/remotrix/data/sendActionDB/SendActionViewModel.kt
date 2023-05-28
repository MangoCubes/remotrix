package ch.skew.remotrix.data.sendActionDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SendActionViewModel(
    private val dao: SendActionDao
): ViewModel() {
    val sendActions = dao.getAllAsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onEvent(event: SendActionEvent) {
        when(event) {
            is SendActionEvent.AddSendEvent -> viewModelScope.launch {
                dao.insert(event.sendAction)
            }
            is SendActionEvent.DeleteSendEvent -> viewModelScope.launch {
                dao.delete(event.sendAction)
            }
        }
    }
}