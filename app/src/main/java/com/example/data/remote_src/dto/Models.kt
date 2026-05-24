package com.example.data.remote_src.dto

data class UserDto(
    val email: String,
    val displayName: String,
    val avatarUrl: String,
    val joinedAt: Long = System.currentTimeMillis()
)

data class MusicTrackDto(
    val id: String,
    val title: String,
    val artist: String,
    val url: String = "",
    val durationSeconds: Int = 180
)

data class CelebrationPostDto(
    val id: String,
    val userEmail: String,
    val userDisplayName: String,
    val userAvatarUrl: String,
    val imageUrl: String,  // Local file path or mock remote URL
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0
)

data class ParadePinDto(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val hostEmail: String,
    val hostName: String,
    val hostAvatar: String,
    val headcount: Int,
    val description: String,
    val musicTrackId: String? = null,
    val celebrateImageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val commentsCount: Int = 0
)
