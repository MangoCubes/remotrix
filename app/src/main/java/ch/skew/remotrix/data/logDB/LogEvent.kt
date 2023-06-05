package ch.skew.remotrix.data.logDB

sealed interface LogEvent {
    data class AddFailure(val error: MsgError, val errorMsg: String, val msgType: Int, val senderId: Int, val payload: String): LogEvent
    data class AddSuccess(val msgType: Int, val senderId: Int, val payload: String): LogEvent
}