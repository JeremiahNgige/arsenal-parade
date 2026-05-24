package com.example.data.remote_src

import com.example.data.remote_src.dto.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class FirebaseRealtimeDb {

    private val scope = CoroutineScope(Dispatchers.Default)

    // Simulated Realtime Database registers
    private val mockPins = mutableListOf<ParadePinDto>()
    private val mockPosts = mutableListOf<CelebrationPostDto>()
    private val mockTracks = mutableListOf<MusicTrackDto>()

    // Flows representing Realtime subscriptions
    private val _pinsFlow = MutableSharedFlow<List<ParadePinDto>>(replay = 1)
    val pinsFlow: SharedFlow<List<ParadePinDto>> = _pinsFlow

    private val _postsFlow = MutableSharedFlow<List<CelebrationPostDto>>(replay = 1)
    val postsFlow: SharedFlow<List<CelebrationPostDto>> = _postsFlow

    init {
        // Pre-configure tracks
        mockTracks.addAll(
            listOf(
                MusicTrackDto("track_1", "North London Forever", "Louis Dunford", "", 240),
                MusicTrackDto("track_2", "Super Mik Arteta", "Ashburton Army Chant", "", 120),
                MusicTrackDto("track_3", "We Love You Arsenal", "Gunners Stadium Hymn", "", 150),
                MusicTrackDto("track_4", "Saka & Emile Smith Rowe", "To the tune of 'Rockin All Over The World'", "", 95),
                MusicTrackDto("track_5", "Bukayo Saka Song", "Saka-La-La-La", "", 110),
                MusicTrackDto("track_6", "Waka Waka (Kai Havertz)", "60 Million Down the Drain", "", 130)
            )
        )

        // Pre-configure initial global celebratory parade epicenters
        mockPins.addAll(
            listOf(
                ParadePinDto(
                    id = "pin_london_1",
                    title = "Emirates Stadium - Arsenal, London",
                    latitude = 51.5549,
                    longitude = -0.1084,
                    hostEmail = "gooner_london@arsenal.com",
                    hostName = "Henry Wright",
                    hostAvatar = "avatar_1",
                    headcount = 150000,
                    description = "OFFICIAL CHAMPIONS PARADE! The bus starts from Emirates Stadium going through Islington Town Hall! ABSOLUTELY ROCKING HERE!",
                    musicTrackId = "track_1",
                    celebrateImageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&q=80&w=600",
                    timestamp = System.currentTimeMillis() - 7200000,
                    commentsCount = 1420
                ),
                ParadePinDto(
                    id = "pin_tokyo_1",
                    title = "Shibuya Fan Fest - Tokyo",
                    latitude = 35.6580,
                    longitude = 139.7016,
                    hostEmail = "shibuya_gooners@gmail.com",
                    hostName = "Kenji Suzuki",
                    hostAvatar = "avatar_5",
                    headcount = 4200,
                    description = "Red and white flags flying all over Shibuya Crossing! Joining the celebration from Tokyo! 🔴⚪️",
                    musicTrackId = "track_2",
                    celebrateImageUrl = "https://images.unsplash.com/photo-1540959733332-eab4deceeaf7?auto=format&fit=crop&q=80&w=600",
                    timestamp = System.currentTimeMillis() - 3600000,
                    commentsCount = 210
                ),
                ParadePinDto(
                    id = "pin_nairobi_1",
                    title = "Nairobi Arsenal Hub - Kenya",
                    latitude = -1.2921,
                    longitude = 36.8219,
                    hostEmail = "jeremiah_nai@gmail.com",
                    hostName = "Jeremiah Ngige",
                    hostAvatar = "avatar_3",
                    headcount = 8500,
                    description = "Nairobi is fully RED today! Singing North London Forever under the sun! Screaming our lungs out!",
                    musicTrackId = "track_3",
                    celebrateImageUrl = "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&q=80&w=600",
                    timestamp = System.currentTimeMillis() - 1800000,
                    commentsCount = 480
                ),
                ParadePinDto(
                    id = "pin_chicago_1",
                    title = "The Globe Pub Fan Zone - Chicago",
                    latitude = 41.9472,
                    longitude = -87.6775,
                    hostEmail = "chicago_guns@yahoo.com",
                    hostName = "Sarah Connor",
                    hostAvatar = "avatar_2",
                    headcount = 2100,
                    description = "Irving Park is buzzing! Huge screens set up outside the pub, flares lit, champions parade stream live!",
                    musicTrackId = "track_6",
                    celebrateImageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&q=80&w=600",
                    timestamp = System.currentTimeMillis() - 900000,
                    commentsCount = 115
                )
            )
        )

        // Initial fan posts
        mockPosts.addAll(
            listOf(
                CelebrationPostDto(
                    id = "post_1",
                    userEmail = "gooner_london@arsenal.com",
                    userDisplayName = "Henry Wright",
                    userAvatarUrl = "avatar_1",
                    imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&q=80&w=600",
                    description = "The trophy is here!! North London is RED and beautiful! Come on you Gunners!!! 🏆🔴⚪️",
                    timestamp = System.currentTimeMillis() - 3600000,
                    likesCount = 2450
                ),
                CelebrationPostDto(
                    id = "post_2",
                    userEmail = "kenji_tk@gmail.com",
                    userDisplayName = "Kenji Suzuki",
                    userAvatarUrl = "avatar_5",
                    imageUrl = "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?auto=format&fit=crop&q=80&w=600",
                    description = "Chanting North London Forever live in Shibuya after midnight! What a moment to be alive!",
                    timestamp = System.currentTimeMillis() - 1200000,
                    likesCount = 890
                )
            )
        )

        // Publish initial states
        scope.launch {
            _pinsFlow.emit(mockPins.toList())
            _postsFlow.emit(mockPosts.toList())

            // Simulate realtime background sync (new pins pop up globally or headcount updates dynamically)
            while (true) {
                delay(12000) // update or sync every 12s
                if (mockPins.isNotEmpty()) {
                    // Random headcount increases of 10 to 100 fans representing realtime joins
                    val randomIndex = Random.nextInt(mockPins.size)
                    val targetPin = mockPins[randomIndex]
                    val updatedPin = targetPin.copy(
                        headcount = targetPin.headcount + Random.nextInt(5, 45),
                        commentsCount = targetPin.commentsCount + Random.nextInt(0, 3)
                    )
                    mockPins[randomIndex] = updatedPin
                    _pinsFlow.emit(mockPins.toList())
                }
            }
        }
    }

    fun getAllMusicTracks(): List<MusicTrackDto> {
        return mockTracks
    }

    suspend fun createPin(dto: ParadePinDto): ParadePinDto {
        mockPins.add(dto)
        _pinsFlow.emit(mockPins.toList())
        return dto
    }

    suspend fun addCelebrationPost(post: CelebrationPostDto) {
        mockPosts.add(0, post) // Prepend newest
        _postsFlow.emit(mockPosts.toList())

        // Also increase headcount on host's pin if relevant
        val targetPinIndex = mockPins.indexOfFirst { it.title.contains("Emirates", ignoreCase = true) }
        if (targetPinIndex != -1) {
            val pin = mockPins[targetPinIndex]
            mockPins[targetPinIndex] = pin.copy(headcount = pin.headcount + 1)
            _pinsFlow.emit(mockPins.toList())
        }
    }

    suspend fun mockIncomingForeignPin() {
        // Trigger a simulated incoming celebration pin from Sydney or Paris
        val randomNum = Random.nextInt(1, 100)
        val isParis = randomNum % 2 == 0
        val newPin = if (isParis) {
            ParadePinDto(
                id = "pin_paris_${System.currentTimeMillis()}",
                title = "Eiffel Tower Arsenal Club - Paris",
                latitude = 48.8584,
                longitude = 2.2945,
                hostEmail = "french_gooner@voila.fr",
                hostName = "Olivier Giroux",
                hostAvatar = "avatar_4",
                headcount = 1100,
                description = "Chanting under the Parisian skies! COYG! 🏆⚽️",
                musicTrackId = "track_4",
                timestamp = System.currentTimeMillis()
            )
        } else {
            ParadePinDto(
                id = "pin_sydney_${System.currentTimeMillis()}",
                title = "Sydney Harbour Fan Base - Australia",
                latitude = -33.8688,
                longitude = 151.2093,
                hostEmail = "syd_guns@ozemail.com",
                hostName = "Garry Neville (Gooner)",
                hostAvatar = "avatar_6",
                headcount = 2150,
                description = "Sunrise celebrations at Sydney Harbour Bridge! We won the league! COYG!",
                musicTrackId = "track_2",
                timestamp = System.currentTimeMillis()
            )
        }
        mockPins.add(newPin)
        _pinsFlow.emit(mockPins.toList())
    }
}
