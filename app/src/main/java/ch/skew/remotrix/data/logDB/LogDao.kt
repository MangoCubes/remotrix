package ch.skew.remotrix.data.logDB

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao{
    @Query("INSERT INTO logs (status, errorMsg, msgType, senderId, payload) VALUES (:status, :errorMsg, :msgType, :senderId, :payload)")
    suspend fun insertError(status: MsgStatus, errorMsg: String?, msgType: Int, senderId: Int, payload: String)
    @Query("INSERT INTO logs (status, msgType, senderId, payload) VALUES (:status, :msgType, :senderId, :payload)")
    suspend fun insertSuccess(status: MsgStatus, msgType: Int, senderId: Int, payload: String)
    @Query("SELECT * FROM logs")
    fun getLogs(): Flow<List<LogData>>
}