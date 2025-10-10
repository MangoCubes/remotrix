package ch.skew.remotrix.data.debugLogDB

import androidx.room.Dao
import androidx.room.Query
import ch.skew.remotrix.classes.MsgStatus
import ch.skew.remotrix.classes.MsgType
import kotlinx.coroutines.flow.Flow

@Dao
interface DebugLogDao{

    /// Write a log entry into the database
    @Query("INSERT INTO debug_log (error_msg, payload) VALUES (:errorMsg, :payload)")
    suspend fun addLog(errorMsg: String, payload: String? = null): Long

    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    fun getLogs(): Flow<List<DebugLogData>>

    @Query("DELETE FROM logs")
    suspend fun deleteAll()
}