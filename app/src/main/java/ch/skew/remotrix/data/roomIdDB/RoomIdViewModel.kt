package ch.skew.remotrix.data.roomIdDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class RoomIdViewModel(
    private val dao: RoomIdDao
): ViewModel() {

    fun getRoomId(phoneNumber: String, senderId: String): Flow<String>{
        return dao.getDestRoom(phoneNumber, senderId)
    }

    fun onEvent(event: RoomIdEvent){
        when(event){
            is RoomIdEvent.AddEntry -> {
                viewModelScope.launch {
                    dao.insert(event.roomIdData)
                }
            }

            is RoomIdEvent.DelEntry -> {
                viewModelScope.launch {
                    dao.delete(event.roomIdData)
                }
            }
        }
    }
}