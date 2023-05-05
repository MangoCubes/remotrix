package ch.skew.remotrix.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey
    val userId: String,
    @ColumnInfo(name = "homeServer")
    val homeServer: String
)
