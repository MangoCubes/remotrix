package ch.skew.remotrix.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao{
    @Upsert
    suspend fun insert(account: Account)
    @Delete
    suspend fun delete(account: Account)
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>
}