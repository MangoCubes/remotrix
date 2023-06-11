package ch.skew.remotrix.data.roomIdDB

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RoomIdDao{
    @Upsert
    suspend fun insert(roomIdData: RoomIdData)
    @Query("SELECT room_id FROM room_ids WHERE phone_number = :phoneNumber AND forwarder_id = :forwarderId")
    fun getDestRoom(phoneNumber: String, forwarderId: Int): String?
}