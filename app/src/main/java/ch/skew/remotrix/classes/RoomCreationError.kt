package ch.skew.remotrix.classes

import ch.skew.remotrix.data.logDB.MsgStatus

class RoomCreationError(
    override val cause: Throwable,
    val error: MsgStatus
): Throwable()