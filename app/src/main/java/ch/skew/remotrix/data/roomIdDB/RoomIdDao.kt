package ch.skew.remotrix.data.roomIdDB

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RoomIdDao{
    @Upsert
    suspend fun insert(roomIdData: RoomIdData)

    /// Given a phone humber, get the corresponding room ID
    @Query("SELECT room_id FROM room_ids WHERE phone_number = :phoneNumber AND forwarder_id = :forwarderId")
    suspend fun getDestRoom(phoneNumber: String, forwarderId: Int): String?

    /// Given a room ID, get the corresponding phone number
    @Query("SELECT phone_number FROM room_ids WHERE room_id = :roomId AND forwarder_id = :forwarderId")
    suspend fun getPhoneNumber(roomId: String, forwarderId: Int): String?

    /// Delete a room given a room ID
    @Query("DELETE FROM room_ids WHERE room_id = :roomId")
    suspend fun delRoomById(roomId: String)
}