package ch.skew.remotrix.data.logDB

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao{

    @Query("INSERT INTO logs (status, msg_type, forwarder_id, payload) VALUES (:defStatus, :msgType, :forwarderId, :payload)")
    suspend fun writeAhead(msgType: Int, forwarderId: Int, payload: String, defStatus: MsgStatus = MsgStatus.MESSAGE_SENDING_FAILED): Long

    @Query("UPDATE logs SET status = :status, error_msg = :errorMsg WHERE id = :id")
    suspend fun setFailure(id: Long, status: MsgStatus, errorMsg: String?)
    @Query("UPDATE logs SET status = :status WHERE id = :id")
    suspend fun setSuccess(id: Long, status: MsgStatus)
    @Query("SELECT * FROM logs")
    fun getLogs(): Flow<List<LogData>>

    @Query("DELETE FROM logs")
    suspend fun deleteAll()
}