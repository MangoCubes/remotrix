package ch.skew.remotrix.data.sendActionDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SendActionDao{
    @Insert
    suspend fun insert(sendAction: SendAction): Long
    @Delete
    suspend fun delete(sendAction: SendAction)
    @Query("SELECT * FROM send_action")
    fun getAllSendActions(): Flow<List<SendAction>>
    @Query("SELECT * FROM send_action WHERE sender_id = :senderId")
    fun getSendActionsBySenderId(senderId: Long): Flow<List<SendAction>>
}