package ch.skew.remotrix.data.logDB

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao{
    @Query("INSERT INTO logs (success, error, errorMsg, msgType, senderId, payload) VALUES (0, :error, :errorMsg, :msgType, :senderId, :payload)")
    suspend fun insertError(error: String, errorMsg: String, msgType: Int, senderId: Int, payload: String)
    @Query("INSERT INTO logs (success, error, errorMsg, msgType, senderId, payload) VALUES (1, NULL, NULL, :msgType, :senderId, :payload)")
    suspend fun insertSuccess(msgType: Int, senderId: Int, payload: String)
    @Query("SELECT * FROM logs WHERE success = 0")
    fun getFailedLogs(): Flow<List<LogData>>
    @Query("SELECT * FROM logs")
    fun getLogs(): Flow<List<LogData>>
}