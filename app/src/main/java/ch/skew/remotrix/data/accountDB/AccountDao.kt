package ch.skew.remotrix.data.accountDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao{
    @Query("INSERT INTO accounts (user_id, domain, base_url) VALUES (:user_id, NULL, :base_url)")
    suspend fun insert(user_id: String, base_url: String): Long
    @Delete
    suspend fun delete(account: Account)
    @Query("SELECT * FROM accounts WHERE domain NOT NULL")
    fun getAllAccounts(): Flow<List<Account>>
    @Query("SELECT * FROM accounts WHERE id = :id AND domain NOT NULL")
    fun getAccountById(id: Int): Flow<List<Account>>
    @Query("SELECT * FROM accounts WHERE user_id = :user_id AND domain NOT NULL LIMIT 1")
    fun getAccountByUserId(user_id: String): Flow<List<Account>>
    @Query("UPDATE accounts SET domain = :domain WHERE id = :accountId")
    suspend fun activateAccount(accountId: Long, domain: String)
}