package ch.skew.remotrix.classes

sealed class CommandAction {
    class Reply(val msg: String) : CommandAction()
    class Reaction(val reaction: String) : CommandAction()
}