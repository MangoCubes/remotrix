package ch.skew.remotrix.data.roomIdDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomIdDao{
    @Upsert
    suspend fun insert(roomIdData: RoomIdData)
    @Delete
    suspend fun delete(roomIdData: RoomIdData)
    @Query("SELECT room_id FROM room_ids WHERE phone_number = :phoneNumber AND sender_id = :senderId")
    fun getDestRoom(phoneNumber: String, senderId: String): Flow<String>
}