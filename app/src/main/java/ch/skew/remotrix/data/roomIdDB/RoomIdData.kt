package ch.skew.remotrix.data.roomIdDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import ch.skew.remotrix.data.accountDB.AccountData

@Entity(
    tableName = "room_ids",
    primaryKeys = ["phone_number", "forwarder_id"],
    foreignKeys = [
        ForeignKey(
            entity = AccountData::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("forwarder_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RoomIdData(
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    @ColumnInfo(name = "forwarder_id", index = true)
    val senderId: Int,
    @ColumnInfo(name = "room_id")
    val roomId: String
)