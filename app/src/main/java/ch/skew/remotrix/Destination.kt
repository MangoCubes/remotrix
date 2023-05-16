package ch.skew.remotrix

enum class Destination(val route: String) {
    Home("home"),
    AccountList("account_list"),
    NewAccount("new_account"),
    Setup("setup")
}