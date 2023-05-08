package ch.skew.remotrix.data.accountDB

sealed interface AccountEvent {
    data class ActivateAccount(val id: Long, val domain: String): AccountEvent
    data class DeleteAccount(val account: Account): AccountEvent
}

sealed interface AccountEventAsync {
    data class AddAccount(val userId: String, val baseUrl: String): AccountEventAsync
}