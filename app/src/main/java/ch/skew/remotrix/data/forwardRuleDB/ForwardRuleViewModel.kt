package ch.skew.remotrix.data.forwardRuleDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ForwardRuleViewModel(
    private val dao: ForwardRuleDao
): ViewModel() {
    val sendActions = dao.getAllAsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}