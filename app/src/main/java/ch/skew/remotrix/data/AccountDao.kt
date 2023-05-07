package ch.skew.remotrix.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import net.folivo.trixnity.core.model.UserId

@Dao
interface AccountDao{
    @Upsert
    suspend fun insert(account: Account)
    @Delete
    suspend fun delete(account: Account)
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>
    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountById(id: Int): Flow<List<Account>>
    @Query("SELECT * FROM accounts WHERE user_id = :userId LIMIT 1")
    fun getAccountByUserId(userId: UserId): Flow<List<Account>>
}