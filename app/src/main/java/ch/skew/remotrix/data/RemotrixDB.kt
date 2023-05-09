package ch.skew.remotrix.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.skew.remotrix.data.accountDB.Account
import ch.skew.remotrix.data.accountDB.AccountDao
import ch.skew.remotrix.data.sendActionDB.SendAction
import ch.skew.remotrix.data.sendActionDB.SendActionDao

@Database(
    entities = [
        Account::class,
        SendAction::class
    ],
    version = 1,
    exportSchema = false
)

abstract class RemotrixDB: RoomDatabase(){
    abstract val accountDao: AccountDao
    abstract val sendActionDao: SendActionDao
}