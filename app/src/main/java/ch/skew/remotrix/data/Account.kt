package ch.skew.remotrix.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

@Entity(tableName = "accounts")
class Account(
    @PrimaryKey
    val userId: String,
    @ColumnInfo(name = "homeServer")
    val homeServer: String
)

@Dao
interface AccountDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)
    @Update
    suspend fun update(account: Account)
    @Delete
    suspend fun delete(account: Account)
}

@Database(entities = [Account::class], version = 1, exportSchema = false)
abstract class AccountRoomDB() : RoomDatabase() {
    abstract fun AccountDao(): AccountDao
    companion object {
        @Volatile
        private var INSTANCE: AccountRoomDB? = null
        fun getDB(context: Context): AccountRoomDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AccountRoomDB::class.java,
                    "account_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }

}
