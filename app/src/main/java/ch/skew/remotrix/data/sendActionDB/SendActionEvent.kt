package ch.skew.remotrix.data.sendActionDB


sealed interface SendActionEvent {
    data class AddSendEvent(val sendAction: SendAction): SendActionEvent
    data class DeleteSendEvent(val sendAction: SendAction): SendActionEvent
}