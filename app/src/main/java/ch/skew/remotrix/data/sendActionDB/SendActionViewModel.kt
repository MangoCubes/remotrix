package ch.skew.remotrix.data.sendActionDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SendActionViewModel(
    private val dao: SendActionDao
): ViewModel() {
    val sendActions = dao.getAllAsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}