package ch.skew.remotrix.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Account::class],
    version = 1
)
abstract class AccountDatabase: RoomDatabase(){
    abstract val dao: AccountDao
}