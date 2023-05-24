package ch.skew.remotrix.data.roomIdDB

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "room_id", primaryKeys = ["phone_number", "sender_id"])
data class RoomIdData(
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    @ColumnInfo(name = "sender_id")
    val senderId: String,
    @ColumnInfo(name = "room_id")
    val roomId: String,
)