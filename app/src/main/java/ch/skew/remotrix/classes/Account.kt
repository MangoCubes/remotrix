package ch.skew.remotrix.classes

import ch.skew.remotrix.data.accountDB.AccountData

/**
 * Account class
 * Unlike AccountData data class, this class is guaranteed to have all variables
 */
class Account(
    /**
     * ID that uniquely identifies this account on this device. Has no connection to the Matrix account itself.
     */
    val id: Int,
    /**
     * Also known as localpart of the user identifiers.
     * Example: "admin" part of @admin:server.com
     */
    val userId: String,
    /**
     * Domain part of the user identifiers.
     * Example: "server.com" part of @admin:server.com
     */
    val domain: String,
    /**
     * Matrix home server URL
     * This usually differs from the domain part of the user identifiers, which is why it is stored independently
     */
    val baseUrl: String,
    /**
     * Room ID of the management room
     */
    val managementRoom: String,
    /**
     * Room ID of the messaging space
     */
    val messageSpace: String
) {
    /**
     * Prints the full username
     */
    fun fullName(): String {
        return "@${this.userId}:${this.domain}"
    }

    companion object {
        /**
         * Construct a list of Accounts using AccountData
         */
        fun from(accounts: List<AccountData>): List<Account>{
            val ret: MutableList<Account> = mutableListOf()
            for (a in accounts){
                if(a.domain === null || a.managementRoom === null || a.messageSpace === null) continue
                ret.add(Account(a.id, a.userId, a.domain, a.baseUrl, a.managementRoom, a.messageSpace))
            }
            return ret
        }
    }
}