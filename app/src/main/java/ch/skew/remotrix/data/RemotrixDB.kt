package ch.skew.remotrix.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import ch.skew.remotrix.data.accountDB.AccountDao
import ch.skew.remotrix.data.accountDB.AccountData
import ch.skew.remotrix.data.forwardRuleDB.ForwardRule
import ch.skew.remotrix.data.forwardRuleDB.ForwardRuleDao
import ch.skew.remotrix.data.logDB.LogDao
import ch.skew.remotrix.data.logDB.LogData
import ch.skew.remotrix.data.roomIdDB.RoomIdDao
import ch.skew.remotrix.data.roomIdDB.RoomIdData

@Database(
    entities = [
        AccountData::class,
        ForwardRule::class,
        RoomIdData::class,
        LogData::class
    ],
    version = 2
)

abstract class RemotrixDB: RoomDatabase(){
    abstract val accountDao: AccountDao
    abstract val forwardRuleDao: ForwardRuleDao
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
                .addMigrations(migration1To2)
                .build()

        private val migration1To2 = Migration(1, 2) {
            it.execSQL(
                "CREATE TABLE IF NOT EXISTS logs_temp (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "status TEXT NOT NULL," +
                        "error_msg TEXT," +
                        "msg_type TEXT NOT NULL," +
                        "forwarder_id INTEGER," +
                        "payload TEXT NOT NULL" +
                        ");"
            )
            val cursor = it.query("SELECT * FROM logs")
            cursor.move(1)
            while(cursor.position < cursor.count){
                val cv = ContentValues()
                cv.put("id", cursor.getInt(0))
                cv.put("timestamp", cursor.getString(1))
                cv.put("status", cursor.getString(2))
                cv.put("error_msg", cursor.getString(3))
                cv.put("msg_type", if (cursor.getInt(4) == 1) "TestMessage" else "SMSForwarding")
                cv.put("forwarder_id", cursor.getInt(5))
                cv.put("payload", cursor.getString(6))
                it.insert("logs_temp", SQLiteDatabase.CONFLICT_IGNORE, cv)
                cursor.move(1)
            }
            it.execSQL("DROP TABLE IF EXISTS logs")
            it.execSQL("ALTER TABLE logs_temp RENAME TO logs")
        }
    }
}