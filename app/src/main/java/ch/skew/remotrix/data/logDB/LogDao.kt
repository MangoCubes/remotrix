package ch.skew.remotrix.data.logDB

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao{

    @Query("INSERT INTO logs (status, msgType, senderId, payload) VALUES (:defStatus, :msgType, :senderId, :payload)")
    suspend fun writeAhead(msgType: Int, senderId: Int, payload: String, defStatus: MsgStatus = MsgStatus.MESSAGE_SENDING_FAILED): Long

    @Query("UPDATE logs SET status = :status, errorMsg = :errorMsg WHERE id = :id")
    suspend fun setFailure(id: Long, status: MsgStatus, errorMsg: String?)
    @Query("UPDATE logs SET status = :status WHERE id = :id")
    suspend fun setSuccess(id: Long, status: MsgStatus)
    @Query("SELECT * FROM logs")
    fun getLogs(): Flow<List<LogData>>
}