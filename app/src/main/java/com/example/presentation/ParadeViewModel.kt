package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.AppDatabase
import com.example.data.entity.CelebrationPostEntity
import com.example.data.entity.MusicTrackEntity
import com.example.data.entity.ParadePinEntity
import com.example.data.remote_src.FirebaseRealtimeDb
import com.example.data.repo.AuthRepository
import com.example.data.repo.GoonerProfile
import com.example.data.repo.ParadeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface MapUiState {
    object Idle : MapUiState
    data class SelectedPin(val pin: ParadePinEntity) : MapUiState
    object CreatePinForm : MapUiState
}

class ParadeViewModel(application: Application) : AndroidViewModel(application) {

    // Database and repository configurations
    private val db = AppDatabase.getDatabase(application)
    private val firebaseDb = FirebaseRealtimeDb()
    
    val authRepo = AuthRepository()
    val paradeRepo = ParadeRepository(
        firebaseDb = firebaseDb,
        paradeDao = db.paradeDao(),
        celebrationDao = db.celebrationDao(),
        musicDao = db.musicDao()
    )

    // Reactive State streams
    val cachedPins: StateFlow<List<ParadePinEntity>> = paradeRepo.cachedPins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedPosts: StateFlow<List<CelebrationPostEntity>> = paradeRepo.cachedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedTracks: StateFlow<List<MusicTrackEntity>> = paradeRepo.cachedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<GoonerProfile?> = authRepo.currentUser

    // UI Interactive States
    private val _mapUiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val mapUiState: StateFlow<MapUiState> = _mapUiState.asStateFlow()

    private val _currentTrack = MutableStateFlow<MusicTrackEntity?>(null)
    val currentTrack: StateFlow<MusicTrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Screen tabs (0 = Map, 1 = Chants, 2 = Feed, 3 = Profile)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Form inputs
    private val _pinTitle = MutableStateFlow("")
    val pinTitle: StateFlow<String> = _pinTitle.asStateFlow()

    private val _pinDesc = MutableStateFlow("")
    val pinDesc: StateFlow<String> = _pinDesc.asStateFlow()

    private val _selectedTrackId = MutableStateFlow<String?>(null)
    val selectedTrackId: StateFlow<String?> = _selectedTrackId.asStateFlow()

    private val _capturedImageUri = MutableStateFlow<String?>(null)
    val capturedImageUri: StateFlow<String?> = _capturedImageUri.asStateFlow()

    // Trigger simulation of incoming syncs
    private val _lastSimulatedPinEvent = MutableSharedFlow<String>()
    val lastSimulatedPinEvent: SharedFlow<String> = _lastSimulatedPinEvent

    init {
        // Auto-select first track
        viewModelScope.launch {
            cachedTracks.collect { tracks ->
                if (tracks.isNotEmpty() && _currentTrack.value == null) {
                    _currentTrack.emit(tracks.first())
                }
            }
        }
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun selectPin(pin: ParadePinEntity) {
        _mapUiState.value = MapUiState.SelectedPin(pin)
        // Auto-load track if this pin has a linked celebration track
        pin.musicTrackId?.let { id ->
            val trackObj = cachedTracks.value.find { it.id == id }
            if (trackObj != null) {
                playTrack(trackObj)
            }
        }
    }

    fun clearSelection() {
        _mapUiState.value = MapUiState.Idle
    }

    fun enterCreatePinForm() {
        _mapUiState.value = MapUiState.CreatePinForm
        // Reset form variables
        _pinTitle.value = ""
        _pinDesc.value = ""
        _selectedTrackId.value = null
        _capturedImageUri.value = null
    }

    fun updateFormFields(title: String, desc: String, trackId: String?, imageUri: String?) {
        _pinTitle.value = title
        _pinDesc.value = desc
        _selectedTrackId.value = trackId
        _capturedImageUri.value = imageUri
    }

    fun playTrack(track: MusicTrackEntity) {
        _currentTrack.value = track
        _isPlaying.value = true
    }

    fun togglePlayback() {
        _isPlaying.value = !_isPlaying.value
    }

    fun playNext() {
        val tracks = cachedTracks.value
        val current = _currentTrack.value
        if (tracks.isNotEmpty() && current != null) {
            val idx = tracks.indexOf(current)
            val nextIdx = (idx + 1) % tracks.size
            playTrack(tracks[nextIdx])
        }
    }

    fun createLocalCelebrationPin(lat: Double, lng: Double) {
        val user = currentUser.value ?: return
        val currentTitle = _pinTitle.value.ifEmpty { "${user.displayName}'s Gunners Zone" }
        val currentDesc = _pinDesc.value.ifEmpty { "We represent North London here today! COYG!" }

        viewModelScope.launch {
            paradeRepo.createParadePin(
                title = currentTitle,
                lat = lat,
                lng = lng,
                hostEmail = user.email,
                hostName = user.displayName,
                hostAvatar = user.avatar,
                description = currentDesc,
                musicTrackId = _selectedTrackId.value,
                localImagePath = _capturedImageUri.value
            )
            _mapUiState.value = MapUiState.Idle
        }
    }

    fun handleSimulateGoogleSignIn(email: String, name: String) {
        val avatars = listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5", "avatar_6")
        val chosenAvatar = avatars[Random.nextInt(avatars.size)]
        viewModelScope.launch {
            authRepo.loginWithGoogle(email, name, chosenAvatar)
        }
    }

    fun handleGuestAccess() {
        viewModelScope.launch {
            authRepo.skipLogin()
        }
    }

    fun simulateIncomingCelebration() {
        viewModelScope.launch {
            paradeRepo.simulateLiveIncomingPin()
            _lastSimulatedPinEvent.emit("A global Gooner event was synced instantly from across the globe via Firebase Sync!")
        }
    }

    fun postCelebrationFeed(description: String, imagePath: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            paradeRepo.shareLocalCelebration(
                userEmail = user.email,
                userName = user.displayName,
                userAvatar = user.avatar,
                imageUrl = imagePath,
                description = description
            )
        }
    }

    fun updatePersonalSettings(name: String, bio: String, badge: String, avatar: String) {
        viewModelScope.launch {
            authRepo.updateProfile(name, bio, badge, avatar)
        }
    }

    fun handleLogout() {
        viewModelScope.launch {
            authRepo.logout()
            _mapUiState.value = MapUiState.Idle
        }
    }
}
