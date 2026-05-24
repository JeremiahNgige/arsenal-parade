package com.example.data.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParadeDao {

    @Query("SELECT * FROM parade_pins ORDER BY headcount DESC")
    fun getAllPins(): Flow<List<ParadePinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPins(pins: List<ParadePinEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: ParadePinEntity)

    @Query("DELETE FROM parade_pins WHERE id = :id")
    suspend fun deletePinById(id: String)

    @Query("DELETE FROM parade_pins")
    suspend fun clearPins()
}

@Dao
interface CelebrationDao {

    @Query("SELECT * FROM celebration_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CelebrationPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CelebrationPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CelebrationPostEntity)

    @Query("DELETE FROM celebration_posts")
    suspend fun clearPosts()
}

@Dao
interface MusicDao {

    @Query("SELECT * FROM music_tracks")
    fun getAllTracks(): Flow<List<MusicTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<MusicTrackEntity>)
}
