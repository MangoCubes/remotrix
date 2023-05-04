package ch.skew.remotrix.data

sealed interface AccountEvent {
    data class DeleteAccount(val account: Account): AccountEvent
    data class AddAccount(val account: Account): AccountEvent
}