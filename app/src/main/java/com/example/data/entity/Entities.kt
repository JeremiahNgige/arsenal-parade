package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parade_pins")
data class ParadePinEntity(
    @PrimaryKey val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val hostEmail: String,
    val hostName: String,
    val hostAvatar: String,
    val headcount: Int,
    val description: String,
    val musicTrackId: String?,
    val celebrateImageUrl: String?,
    val timestamp: Long,
    val commentsCount: Int
)

@Entity(tableName = "celebration_posts")
data class CelebrationPostEntity(
    @PrimaryKey val id: String,
    val userEmail: String,
    val userDisplayName: String,
    val userAvatarUrl: String,
    val imageUrl: String,
    val description: String,
    val timestamp: Long,
    val likesCount: Int
)

@Entity(tableName = "music_tracks")
data class MusicTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val durationSeconds: Int
)
