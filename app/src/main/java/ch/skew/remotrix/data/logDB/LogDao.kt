package ch.skew.remotrix.data.logDB

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao{

    @Query("INSERT INTO logs (status, msg_type, payload) VALUES (:defStatus, :msgType, :payload)")
    suspend fun writeAhead(msgType: Int, payload: String, defStatus: MsgStatus = MsgStatus.MESSAGE_SENDING_FAILED): Long

    @Query("UPDATE logs SET status = :status, error_msg = :errorMsg, forwarder_id = :forwarderId WHERE id = :id")
    suspend fun setFailure(id: Long, status: MsgStatus, errorMsg: String?, forwarderId: Int?)
    @Query("UPDATE logs SET status = :status, forwarder_id = :forwarderId WHERE id = :id")
    suspend fun setSuccess(id: Long, status: MsgStatus, forwarderId: Int?)
    @Query("SELECT * FROM logs")
    fun getLogs(): Flow<List<LogData>>

    @Query("DELETE FROM logs")
    suspend fun deleteAll()
}