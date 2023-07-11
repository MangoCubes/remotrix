package ch.skew.remotrix.data.logDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.skew.remotrix.classes.MsgStatus
import ch.skew.remotrix.classes.MsgType

@Entity(tableName = "logs")
data class LogData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
    val timestamp: String,
    @ColumnInfo(name = "status")
    val status: MsgStatus,
    @ColumnInfo(name = "error_msg")
    val errorMsg: String?,
    @ColumnInfo(name = "msg_type")
    val msgType: MsgType,
    @ColumnInfo(name = "forwarder_id")
    val forwarderId: Int?,
    @ColumnInfo(name = "payload")
    val payload: String
)