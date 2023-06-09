package ch.skew.remotrix.data.logDB

sealed interface LogEventAsync {
    data class WriteAhead(val msgType: Int, val senderId: Int, val payload: String): LogEventAsync
}

sealed interface LogEvent {
    data class SetFailure(val id: Long, val status: MsgStatus, val errorMsg: String): LogEvent
    data class SetSuccess(val id: Long, val status: MsgStatus): LogEvent
}