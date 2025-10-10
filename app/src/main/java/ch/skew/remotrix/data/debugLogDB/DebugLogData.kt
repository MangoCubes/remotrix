package ch.skew.remotrix.data.debugLogDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.skew.remotrix.classes.MsgStatus
import ch.skew.remotrix.classes.MsgType

/**
 * Database for storing errors that have occurred when the messages are either sent or received
 * Stores issues that have occurred only, successful transmissions should never be stored
 * TODO: Perhaps make it opt-in to store successful transmissions too?
 */
@Entity(tableName = "debug_log")
data class DebugLogData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
    val timestamp: String,
    @ColumnInfo(name = "error_msg")
    val errorMsg: String,
    /**
     * The data that is supposed to be sent
     * This is nullable because storing user information is opt-in
     */
    @ColumnInfo(name = "payload")
    val payload: String?
)