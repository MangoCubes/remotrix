package ch.skew.remotrix.data.accountDB

sealed interface AccountEvent {
    data class ActivateAccount(val id: Long, val domain: String, val managementRoom: String): AccountEvent
    data class DeleteAccount(val id: Int): AccountEvent
}

sealed interface AccountEventAsync {
    data class AddAccount(val userId: String, val baseUrl: String, val messageSpace: String): AccountEventAsync
}