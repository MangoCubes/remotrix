package ch.skew.remotrix.data.forwardRuleDB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ch.skew.remotrix.data.accountDB.AccountData

/**
 * Determines where messages should be sent to upon receiving messages via SMS.
 * TODO: Make it bidirectional (Currently only supports SMS -> Matrix only)
 */
@Entity(
    tableName = "send_action",
    foreignKeys = [
        ForeignKey(
            entity = AccountData::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("sender_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ForwardRule(
    /**
     * Account that will be used for sending messages via Matrix
     * Obviously will be foreign key
     */
    @ColumnInfo(name = "sender_id", index = true)
    val senderId: Int,
    /**
     * Regex that gets testes against the phone number of the sender
     */
    @ColumnInfo(name = "sender_regex")
    val senderRegex: String,
    /**
     * Regex that gets testes against the body of the message
     */
    @ColumnInfo(name = "body_regex")
    val bodyRegex: String,
    /**
     * Priority: Lower means tested against first
     */
    @ColumnInfo(name = "priority", index = true)
    val priority: Int,
    /**
     * ID of this send action
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long,
)