package ch.skew.remotrix.data

sealed interface AccountEvent {
    data class ActivateAccount(val accountId: Int): AccountEvent
    data class DeleteAccount(val account: Account): AccountEvent
}

sealed interface AccountEventAsync {
    data class AddAccount(val account: Account): AccountEventAsync
}