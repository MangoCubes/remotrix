package ch.skew.remotrix.data.roomIdDB

sealed interface RoomIdEvent {
    data class AddEntry(val roomIdData: RoomIdData): RoomIdEvent
    data class DelEntry(val roomIdData: RoomIdData): RoomIdEvent
}