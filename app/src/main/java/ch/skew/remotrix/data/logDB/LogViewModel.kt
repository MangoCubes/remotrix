package ch.skew.remotrix.data.logDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class LogViewModel(
    private val dao: LogDao
): ViewModel() {
    val logs = dao.getLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}