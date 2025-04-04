package ch.skew.remotrix.data.roomIdDB

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RoomIdDao{
    @Upsert
    suspend fun insert(roomIdData: RoomIdData)
    @Query("SELECT room_id FROM room_ids WHERE phone_number = :phoneNumber AND forwarder_id = :forwarderId")
    suspend fun getDestRoom(phoneNumber: String, forwarderId: Int): String?

    @Query("SELECT phone_number FROM room_ids WHERE room_id = :roomId AND forwarder_id = :forwarderId")
    suspend fun getPhoneNumber(roomId: String, forwarderId: Int): String?
    @Query("DELETE FROM room_ids WHERE room_id = :roomId")
    suspend fun delRoomById(roomId: String)
}