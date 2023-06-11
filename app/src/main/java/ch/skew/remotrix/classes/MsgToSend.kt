package ch.skew.remotrix.classes

import ch.skew.remotrix.data.forwardRuleDB.ForwardRule
import net.folivo.trixnity.core.model.RoomId

/**
 * Abstract class that covers all form of messages sent by this app
 */
abstract class MsgToSend(
    open val payload: String
) {
    companion object{
        /**
         * Constructs a list of MsgToSend subclasses, based on msgType
         * Because Enum cannot fit into worker inputData, Int is temporarily used to differentiate between various message types.
         * 1: TestMsg
         * 2: SMSMsg
         * -1: Default and invalid, these should be dropped.
         */
        fun from(msgType: Int, senderId: Int, payload: Array<String>?): MsgToSend?{
            if(payload === null) return null
            if(msgType == 1) return TestMsg(senderId, RoomId(payload[0]), payload[1])
            else if(msgType == 2) return SMSMsg(payload[0], payload[1])
            else return null
        }
    }
}

/**
 * Test messages which can be sent from account management page
 */
class TestMsg(
    /**
     * ID of the account that will be used to send this message via Matrix
     */
    val senderId: Int,
    /**
     * ID of the room in which the message will be sent to
     * For now, this will always be a management room
     */
    val to: RoomId,
    /**
     * Actual body of the message
     */
    override val payload: String
): MsgToSend(payload)

fun matchRegex(pattern: String, from: String, matchEntire: Boolean = false): Boolean{
    if(pattern === "") return true
    val regex = Regex(pattern)
    return ((!matchEntire && regex.matches(from))
            || (matchEntire && regex.matchEntire(from) !== null))
}

/**
 * SMS messages that will be forwarded into appropriate chatroom
 * Sender and payload will be the parameters for determining the chatroom for this message.
 */
class SMSMsg(
    /**
     * Phone number of the SMS sender
     */
    val sender: String,
    /**
     * Content of SMS message
     */
    override val payload: String
): MsgToSend(payload) {
    /**
     * Given a list of rules, this will calculate which Matrix sender should be used.
     * (Given a Matrix sender and phone number of this SMS message, a unique room will be selected.)
     */
    fun getSenderId(rules: List<ForwardRule>): Int?{
        for(rule in rules){
            if(matchRegex(rule.senderRegex, sender) && matchRegex(rule.bodyRegex, payload)) return rule.senderId
        }
        return null
    }
}