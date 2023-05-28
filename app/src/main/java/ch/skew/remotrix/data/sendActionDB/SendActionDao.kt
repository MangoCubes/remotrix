package ch.skew.remotrix.data.sendActionDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SendActionDao{
    @Insert
    suspend fun insert(sendAction: SendAction)
    @Delete
    suspend fun delete(sendAction: SendAction)
    @Query("SELECT * FROM send_action")
    fun getAllAsFlow(): Flow<List<SendAction>>

    @Query("SELECT * FROM send_action")
    fun getAll(): List<SendAction>
}