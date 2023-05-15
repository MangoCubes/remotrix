package ch.skew.remotrix.classes

import ch.skew.remotrix.data.accountDB.AccountData

class Account(
    val id: Int,
    val userId: String,
    val domain: String,
    val baseUrl: String,
    val managementRoom: String
) {
    fun fullName(): String {
        return "@${this.userId}:${this.domain}"
    }

    companion object {
        fun from(accounts: List<AccountData>): List<Account>{
            val ret: MutableList<Account> = mutableListOf()
            for (a in accounts){
                if(a.domain === null || a.managementRoom === null) continue
                ret.add(Account(a.id, a.userId, a.domain, a.baseUrl, a.managementRoom))
            }
            return ret
        }
    }
}