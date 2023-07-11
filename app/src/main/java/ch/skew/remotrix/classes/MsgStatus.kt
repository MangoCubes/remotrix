package ch.skew.remotrix.classes

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
    CANNOT_CREATE_CHILD_ROOM
}