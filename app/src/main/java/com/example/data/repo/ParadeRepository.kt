package com.example.data.repo

import com.example.data.entity.*
import com.example.data.remote_src.FirebaseRealtimeDb
import com.example.data.remote_src.dto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ParadeRepository(
    private val firebaseDb: FirebaseRealtimeDb,
    private val paradeDao: ParadeDao,
    private val celebrationDao: CelebrationDao,
    private val musicDao: MusicDao
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Local cached Streams
    val cachedPins: Flow<List<ParadePinEntity>> = paradeDao.getAllPins()
    val cachedPosts: Flow<List<CelebrationPostEntity>> = celebrationDao.getAllPosts()
    val cachedTracks: Flow<List<MusicTrackEntity>> = musicDao.getAllTracks()

    init {
        // Sync static lists like available premium celebration music tracks
        repositoryScope.launch {
            val trackDtos = firebaseDb.getAllMusicTracks()
            val entities = trackDtos.map { dto ->
                MusicTrackEntity(
                    id = dto.id,
                    title = dto.title,
                    artist = dto.artist,
                    url = dto.url,
                    durationSeconds = dto.durationSeconds
                )
            }
            musicDao.insertTracks(entities)
        }

        // Realtime Synchronizer: Subscribe to Realtime Firebase DTO changes and update local Room Database cache!
        repositoryScope.launch {
            firebaseDb.pinsFlow.collect { listDtos ->
                val entities = listDtos.map { dto ->
                    ParadePinEntity(
                        id = dto.id,
                        title = dto.title,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        hostEmail = dto.hostEmail,
                        hostName = dto.hostName,
                        hostAvatar = dto.hostAvatar,
                        headcount = dto.headcount,
                        description = dto.description,
                        musicTrackId = dto.musicTrackId,
                        celebrateImageUrl = dto.celebrateImageUrl,
                        timestamp = dto.timestamp,
                        commentsCount = dto.commentsCount
                    )
                }
                // Update local Room database for reactive local display
                paradeDao.clearPins()
                paradeDao.insertPins(entities)
            }
        }

        // Realtime Synchronizer: Celebrate posts sync
        repositoryScope.launch {
            firebaseDb.postsFlow.collect { postDtos ->
                val entities = postDtos.map { dto ->
                    CelebrationPostEntity(
                        id = dto.id,
                        userEmail = dto.userEmail,
                        userDisplayName = dto.userDisplayName,
                        userAvatarUrl = dto.userAvatarUrl,
                        imageUrl = dto.imageUrl,
                        description = dto.description,
                        timestamp = dto.timestamp,
                        likesCount = dto.likesCount
                    )
                }
                // Clear and insert latest posts
                celebrationDao.clearPosts()
                celebrationDao.insertPosts(entities)
            }
        }
    }

    // Interactive Actions: Creating pins, pinning celebrations, choosing track, adding sync
    suspend fun createParadePin(
        title: String,
        lat: Double,
        lng: Double,
        hostEmail: String,
        hostName: String,
        hostAvatar: String,
        description: String,
        musicTrackId: String?,
        localImagePath: String?,
        headcount: Int = 1
    ) {
        val uniqueId = "pin_${System.currentTimeMillis()}"
        val pinDto = ParadePinDto(
            id = uniqueId,
            title = title,
            latitude = lat,
            longitude = lng,
            hostEmail = hostEmail,
            hostName = hostName,
            hostAvatar = hostAvatar,
            headcount = headcount,
            description = description,
            musicTrackId = musicTrackId,
            celebrateImageUrl = localImagePath,
            timestamp = System.currentTimeMillis()
        )

        // 1. Sync directly to the Remote Firebase Server
        firebaseDb.createPin(pinDto)

        // 2. Fallback Cache directly in local Room DB for instant speed
        paradeDao.insertPin(
            ParadePinEntity(
                id = pinDto.id,
                title = pinDto.title,
                latitude = pinDto.latitude,
                longitude = pinDto.longitude,
                hostEmail = pinDto.hostEmail,
                hostName = pinDto.hostName,
                hostAvatar = pinDto.hostAvatar,
                headcount = pinDto.headcount,
                description = pinDto.description,
                musicTrackId = pinDto.musicTrackId,
                celebrateImageUrl = pinDto.celebrateImageUrl,
                timestamp = pinDto.timestamp,
                commentsCount = pinDto.commentsCount
            )
        )
    }

    suspend fun shareLocalCelebration(
        userEmail: String,
        userName: String,
        userAvatar: String,
        imageUrl: String,
        description: String
    ) {
        val uniquePostId = "post_${System.currentTimeMillis()}"
        val postDto = CelebrationPostDto(
            id = uniquePostId,
            userEmail = userEmail,
            userDisplayName = userName,
            userAvatarUrl = userAvatar,
            imageUrl = imageUrl,
            description = description,
            timestamp = System.currentTimeMillis()
        )

        // Sync to remote realtime database
        firebaseDb.addCelebrationPost(postDto)

        // Cache block
        celebrationDao.insertPost(
            CelebrationPostEntity(
                id = postDto.id,
                userEmail = postDto.userEmail,
                userDisplayName = postDto.userDisplayName,
                userAvatarUrl = postDto.userAvatarUrl,
                imageUrl = postDto.imageUrl,
                description = postDto.description,
                timestamp = postDto.timestamp,
                likesCount = postDto.likesCount
            )
        )
    }

    suspend fun simulateLiveIncomingPin() {
        firebaseDb.mockIncomingForeignPin()
    }
}
