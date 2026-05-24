package com.example.data.repo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GoonerProfile(
    val email: String,
    val displayName: String,
    val avatar: String, // e.g. "avatar_1", "avatar_2" representing custom player icons or faces
    val badge: String = "Invincibles", // Title badge
    val quote: String = "North London Forever!",
    val isGoogleAuthenticated: Boolean = false
)

class AuthRepository {

    // Default Gooner (personalized)
    private val _currentUser = MutableStateFlow<GoonerProfile?>(null)
    val currentUser: StateFlow<GoonerProfile?> = _currentUser.asStateFlow()

    init {
        // Initially unsigned in (encourages personalized onboarding / Google Sign-In promo)
        // Let's keep it null initially so the user can experience the slick Google Sign-In prompt!
    }

    suspend fun loginWithGoogle(email: String, name: String, avatar: String): GoonerProfile {
        // Create authenticated profile!
        val badge = when {
            email.contains("henry", ignoreCase = true) -> "Invincible King 👑"
            email.contains("wright", ignoreCase = true) -> "Legendary Striker ⚽"
            email.contains("admin", ignoreCase = true) -> "Manager Arteta Elite 👔"
            else -> "Emirates Faithful 🔴"
        }
        val profile = GoonerProfile(
            email = email,
            displayName = name,
            avatar = avatar,
            badge = badge,
            quote = "Come on you Gunners! Today is our day! 🏆",
            isGoogleAuthenticated = true
        )
        _currentUser.emit(profile)
        return profile
    }

    suspend fun skipLogin(): GoonerProfile {
        // Create standard guest builder
        val profile = GoonerProfile(
            email = "gooner_guest@emirates.org",
            displayName = "Guest Gooner",
            avatar = "avatar_3",
            badge = "Parade Explorer",
            quote = "Ready to chant!",
            isGoogleAuthenticated = false
        )
        _currentUser.emit(profile)
        return profile
    }

    suspend fun updateProfile(name: String, quote: String, badge: String, avatar: String) {
        _currentUser.value?.let { current ->
            _currentUser.emit(
                current.copy(
                    displayName = name,
                    quote = quote,
                    badge = badge,
                    avatar = avatar
                )
            )
        }
    }

    suspend fun logout() {
        _currentUser.emit(null)
    }
}
