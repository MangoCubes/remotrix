package ch.skew.remotrix.data.forwardRuleDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardRuleDao{
    @Query("SELECT * FROM send_action")
    fun getAllAsFlow(): Flow<List<ForwardRule>>

    @Query("SELECT * FROM send_action")
    fun getAll(): List<ForwardRule>
}