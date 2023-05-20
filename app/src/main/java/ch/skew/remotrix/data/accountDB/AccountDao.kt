package ch.skew.remotrix.data.accountDB

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao{
    @Query("INSERT INTO accounts (user_id, domain, base_url, message_space) VALUES (:userId, NULL, :baseUrl, :messageSpace)")
    suspend fun insert(userId: String, baseUrl: String, messageSpace: String): Long
    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Int)
    @Query("SELECT * FROM accounts WHERE domain NOT NULL")
    fun getAllAccounts(): Flow<List<AccountData>>
    @Query("SELECT * FROM accounts WHERE id = :id AND domain NOT NULL")
    fun getAccountById(id: Int): Flow<List<AccountData>>
    @Query("SELECT * FROM accounts WHERE user_id = :user_id AND domain NOT NULL LIMIT 1")
    fun getAccountByUserId(user_id: String): Flow<List<AccountData>>
    @Query("UPDATE accounts SET domain = :domain, management_room = :managementRoom WHERE id = :accountId")
    suspend fun activateAccount(accountId: Long, domain: String, managementRoom: String)
}