package ch.skew.remotrix.classes

sealed class CommandAction {
    class Reply(val msg: String) : CommandAction()
    class Thread(val msg: String) : CommandAction()
}