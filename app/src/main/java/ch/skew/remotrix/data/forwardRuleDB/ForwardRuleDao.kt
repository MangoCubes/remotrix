package ch.skew.remotrix.data.forwardRuleDB

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardRuleDao{
    @Query("SELECT * FROM forward_rule")
    fun getAllAsFlow(): Flow<List<ForwardRule>>

    @Query("SELECT * FROM forward_rule")
    fun getAll(): List<ForwardRule>
}