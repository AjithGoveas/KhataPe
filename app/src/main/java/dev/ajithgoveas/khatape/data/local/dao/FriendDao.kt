package dev.ajithgoveas.khatape.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.ajithgoveas.khatape.data.local.entity.FriendEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(friend: FriendEntity): Long

    @Query("SELECT * FROM friends ORDER BY createdAt DESC")
    fun getAll(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE id = :id")
    fun getById(id: Long): Flow<FriendEntity?>

    @Delete
    suspend fun delete(friend: FriendEntity): Int

    @Query("DELETE FROM friends")
    suspend fun deleteAll()
}