package ch.skew.remotrix.data.accountDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "domain")
    val domain: String?,
    @ColumnInfo(name = "base_url")
    val baseUrl: String,
    @ColumnInfo(name = "management_room")
    val managementRoom: String?,
    @ColumnInfo(name = "message_space")
    val messageSpace: String
)