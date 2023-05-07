package ch.skew.remotrix.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao{
    @Upsert
    suspend fun insert(account: Account): Long
    @Delete
    suspend fun delete(account: Account)
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>
    @Query("SELECT * FROM accounts WHERE id = :id AND activated = 1")
    fun getAccountById(id: Int): Flow<List<Account>>
    @Query("SELECT * FROM accounts WHERE user_id = :user_id LIMIT 1")
    fun getAccountByUserId(user_id: String): Flow<List<Account>>
    @Query("UPDATE accounts SET activated = 1 WHERE id = :accountId")
    fun activateAccount(accountId: Int)
}