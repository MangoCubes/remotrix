package ch.skew.remotrix.classes

import ch.skew.remotrix.R

enum class MsgStatus{
    /**
     * This error indicates that message sending failed because message got stuck in the outbox when the task is over.
     * This error is logged into the database first, and gets overwritten to one of the codes below if the message gets sent or if sending fails before time's up.
     */
    MESSAGE_SENDING_FAILED,

    MESSAGE_MAX_ATTEMPTS_REACHED,

    MESSAGE_SENT,

    MESSAGE_DROPPED,

    NO_SUITABLE_FORWARDER,

    CANNOT_LOAD_MATRIX_CLIENT,

    CANNOT_CREATE_ROOM,

    /**
     * This error differs from MsgStatus.CANNOT_CREATE_ROOM in the sense that while the client managed to create a room, it failed to make it child of the messaging space.
     */
    CANNOT_CREATE_CHILD_ROOM;
    companion object {
        fun translateStatus(status: MsgStatus): Int {
            return when(status){
                MESSAGE_SENDING_FAILED -> R.string.message_sending_failed
                MESSAGE_SENT -> R.string.message_sent
                MESSAGE_DROPPED -> R.string.message_dropped
                NO_SUITABLE_FORWARDER -> R.string.no_suitable_forwarder
                CANNOT_LOAD_MATRIX_CLIENT -> R.string.cannot_load_matrix_client
                CANNOT_CREATE_ROOM -> R.string.cannot_create_room
                CANNOT_CREATE_CHILD_ROOM -> R.string.cannot_create_child_room
                MESSAGE_MAX_ATTEMPTS_REACHED -> R.string.message_max_attempts_reached
            }
        }
    }
}