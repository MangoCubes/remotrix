package ch.skew.remotrix.data.logDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MsgError{
    /**
     * 1
     */
    UNRECOGNISED_MESSAGE_CODE,

    /**
     * 2
     */
    NO_SUITABLE_FORWARDER,

    /**
     * 3
     */
    CANNOT_LOAD_MATRIX_CLIENT,

    /**
     * 4
     */
    CANNOT_CREATE_ROOM,

    /**
     * 5
     * This error differs from MsgError.CANNOT_CREATE_ROOM in the sense that while the client managed to create a room, it failed to make it child of the messaging space.
     */
    CANNOT_CREATE_CHILD_ROOM,

    /**
     * 6
     */
    UNRECOGNISED_MESSAGE_CLASS

}
@Entity(tableName = "logs")
data class LogData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
    val timestamp: String,
    @ColumnInfo(name = "success")
    val success: Boolean,
    @ColumnInfo(name = "error")
    val error: MsgError?,
    @ColumnInfo(name = "errorMsg")
    val errorMsg: String?,
    @ColumnInfo(name = "msgType")
    val msgType: Int,
    @ColumnInfo(name = "senderId")
    val senderId: Int,
    @ColumnInfo(name = "payload")
    val payload: String
)