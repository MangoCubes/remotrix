package ch.skew.remotrix.data.debugLogDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class DebugLogViewModel(
    private val dao: DebugLogDao
): ViewModel() {
    val logs = dao.getLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}