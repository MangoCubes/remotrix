package ch.skew.remotrix.data.logDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logs")
data class LogData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
    val timestamp: String,
    @ColumnInfo(name = "success")
    val success: Boolean,
    @ColumnInfo(name = "error")
    val error: String?,
    @ColumnInfo(name = "errorMsg")
    val errorMsg: String?,
    @ColumnInfo(name = "msgType")
    val msgType: Int,
    @ColumnInfo(name = "senderId")
    val senderId: Int,
    @ColumnInfo(name = "payload")
    val payload: String
)