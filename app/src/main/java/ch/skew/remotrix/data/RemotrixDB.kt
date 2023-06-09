package ch.skew.remotrix.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ch.skew.remotrix.data.accountDB.AccountDao
import ch.skew.remotrix.data.accountDB.AccountData
import ch.skew.remotrix.data.logDB.LogDao
import ch.skew.remotrix.data.logDB.LogData
import ch.skew.remotrix.data.roomIdDB.RoomIdDao
import ch.skew.remotrix.data.roomIdDB.RoomIdData
import ch.skew.remotrix.data.sendActionDB.SendAction
import ch.skew.remotrix.data.sendActionDB.SendActionDao

@Database(
    entities = [
        AccountData::class,
        SendAction::class,
        RoomIdData::class,
        LogData::class
    ],
    version = 1,
    exportSchema = false
)

abstract class RemotrixDB: RoomDatabase(){
    abstract val accountDao: AccountDao
    abstract val sendActionDao: SendActionDao
    abstract val roomIdDao: RoomIdDao
    abstract val logDao: LogDao
    companion object {

        @Volatile
        private var instance: RemotrixDB? = null

        fun getInstance(context: Context): RemotrixDB {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, RemotrixDB::class.java, "accounts.db")
                .build()
    }
}