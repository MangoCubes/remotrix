package ch.skew.remotrix.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.skew.remotrix.data.accountDB.AccountDao
import ch.skew.remotrix.data.accountDB.AccountData
import ch.skew.remotrix.data.roomIdDB.RoomIdDao
import ch.skew.remotrix.data.roomIdDB.RoomIdData
import ch.skew.remotrix.data.sendActionDB.SendAction
import ch.skew.remotrix.data.sendActionDB.SendActionDao

@Database(
    entities = [
        AccountData::class,
        SendAction::class,
        RoomIdData::class
    ],
    version = 1,
    exportSchema = false
)

abstract class RemotrixDB: RoomDatabase(){
    abstract val accountDao: AccountDao
    abstract val sendActionDao: SendActionDao
    abstract val roomIdDao: RoomIdDao
}