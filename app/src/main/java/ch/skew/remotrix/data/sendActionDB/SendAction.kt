package ch.skew.remotrix.data.sendActionDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ch.skew.remotrix.data.accountDB.Account

/**
 * Determines where messages should be sent to upon receiving messages via SMS.
 * TODO: Make it bidirectional (Currently only supports SMS -> Matrix only)
 */
@Entity(
    tableName = "send_action",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SendAction(
    /**
     * Account that will be used for sending messages via Matrix
     * Obviously will be foreign key
     */
    @ColumnInfo(index = true, name = "id")
    val userId: Long,
    /**
     * ID of the space that would be used to send messages
     * This must be space, and this account requires permission to create a room within this space
     */
    @ColumnInfo(name = "space_id")
    val spaceId: String,
    /**
     * A string that contains the following: %1 and %2
     * This string will be used as a name for new rooms created when a message arrives from someone you have never received one from before
     */
    @ColumnInfo(name = "name_format")
    val nameFormat: String,
    /**
     * ID of this send action
     */
    @PrimaryKey(autoGenerate = true)
val id: Long,
)