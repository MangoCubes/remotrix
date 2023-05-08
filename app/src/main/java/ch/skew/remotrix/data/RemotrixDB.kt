package ch.skew.remotrix.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ch.skew.remotrix.data.accountDB.Account
import ch.skew.remotrix.data.accountDB.AccountDao

@Database(
    entities = [Account::class],
    version = 1,
    exportSchema = false
)

abstract class RemotrixDB: RoomDatabase(){
    abstract val dao: AccountDao
}