package ch.skew.remotrix.classes

class RoomCreationError(
    override val cause: Throwable,
    val error: MsgStatus
): Throwable()