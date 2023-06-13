package ch.skew.remotrix.classes

enum class Destination(val route: String) {
    Home("home"),
    AccountList("account_list"),
    NewAccount("new_account"),
    Setup("setup"),
    Settings("settings"),
    Logs("logs")
}

enum class Setup(val route: String) {
    Welcome("welcome"),
    Permissions("permissions"),
    Manager("manager"),
    ManagerSpace("manager_space"),
    NextStep("next_step")
}