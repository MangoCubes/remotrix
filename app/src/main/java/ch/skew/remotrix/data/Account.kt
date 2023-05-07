package ch.skew.remotrix.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "domain")
    val domain: String,
    @ColumnInfo(name = "base_url")
    val baseUrl: String,
    @ColumnInfo(name = "activated")
    val activated: Boolean
) {
    constructor(userId: String, domain: String, baseUrl: String): this(0, userId, domain, baseUrl, false)
    fun fullName(): String {
        return "@${this.userId}:${this.domain}"
    }
}