package ch.skew.remotrix.classes

import ch.skew.remotrix.data.sendActionDB.SendAction
import net.folivo.trixnity.core.model.RoomId

abstract class MsgToSend(
    open val senderId: Int,
    open val payload: String
) {
    companion object{
        fun from(msgType: Int, senderId: Int, payload: Array<String>?): MsgToSend?{
            if(payload === null) return null
            if(msgType == 1) return TestMsg(senderId, RoomId(payload[0]), payload[1])
            else if(msgType == 2) return SMSMsg(senderId, payload[0], payload[1])
            else return null
        }
    }
}

class TestMsg(
    override val senderId: Int, //ID of the account that will be used to send message via Matrix
    val to: RoomId, //ID of the room to send the message, will usually be Management room
    override val payload: String //Message to send
): MsgToSend(senderId, payload)



// Sender and payload will be the parameters for determining where the message should be sent to
class SMSMsg(
    override val senderId: Int, //ID of the account that will be used to send message via Matrix
    val sender: String, //Phone number of the SMS sender
    override val payload: String //Content of SMS message
): MsgToSend(senderId, payload) {
    fun getRoomId(rules: List<SendAction>): RoomId{
        throw Error("Not implemented")
    }
}

