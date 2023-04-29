package ch.skew.remotrix

import org.matrix.android.sdk.api.provider.RoomDisplayNameFallbackProvider

class RoomDisplayName : RoomDisplayNameFallbackProvider {
    override fun getNameFor1member(name: String): String {
        return name
    }

    override fun getNameFor2members(name1: String, name2: String): String {
        return "$name1 and $name2"
    }

    override fun getNameFor3members(name1: String, name2: String, name3: String): String {
        return "$name1, $name2 and $name3"
    }

    override fun getNameFor4members(
        name1: String,
        name2: String,
        name3: String,
        name4: String
    ): String {
        return "$name1 and 3 others"
    }

    override fun getNameFor4membersAndMore(
        name1: String,
        name2: String,
        name3: String,
        remainingCount: Int
    ): String {
        return "$name1 and ${remainingCount - 1} others"
    }

    override fun getNameForEmptyRoom(isDirect: Boolean, leftMemberNames: List<String>): String {
        return "Empty room"
    }

    override fun getNameForRoomInvite(): String {
        return "Invited room"
    }

}