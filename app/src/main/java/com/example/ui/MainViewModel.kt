package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackEngine
import com.example.audio.PlayerState
import com.example.audio.TactileSoundEngine
import com.example.audio.TurntableSpeed
import com.example.data.local.PhonoDatabase
import com.example.data.model.AudioTrack
import com.example.data.repository.TrackRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    TURNTABLE,
    LIBRARY,
    EQUALIZER,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PhonoDatabase.getInstance(application)
    private val repository = TrackRepository(database)

    val tactileEngine = TactileSoundEngine(application)
    val playbackEngine = AudioPlaybackEngine(application, tactileEngine)

    val playerState: StateFlow<PlayerState> = playbackEngine.playerState

    private val _selectedTab = MutableStateFlow(AppTab.TURNTABLE)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLibraryScanning = MutableStateFlow(false)
    val isLibraryScanning: StateFlow<Boolean> = _isLibraryScanning.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    private val _sleepTimerRemaining = MutableStateFlow<Int?>(null)
    val sleepTimerRemaining: StateFlow<Int?> = _sleepTimerRemaining.asStateFlow()
    private var sleepTimerJob: Job? = null

    val allTracks: StateFlow<List<AudioTrack>> = combine(
        repository.allTracks,
        _searchQuery
    ) { tracks, query ->
        if (query.isBlank()) {
            tracks
        } else {
            val q = query.trim().lowercase()
            tracks.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) || it.album.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<AudioTrack>> = repository.favoriteTracks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        // Auto-check library on start. If empty, load our vintage audiophile sampler so user has audio ready
        viewModelScope.launch {
            repository.allTracks.collect { tracks ->
                if (tracks.isEmpty()) {
                    loadVintageSampler()
                } else if (playbackEngine.playerState.value.queue.isEmpty()) {
                    playbackEngine.setQueue(tracks, 0, autoPlay = false)
                }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        tactileEngine.playMechanicalClick()
        _selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playTrack(track: AudioTrack, trackList: List<AudioTrack> = allTracks.value) {
        val index = trackList.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackEngine.setQueue(trackList, index, autoPlay = true)
        viewModelScope.launch {
            repository.recordPlay(track.id)
        }
    }

    fun togglePlayPause() {
        playbackEngine.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playbackEngine.seekTo(positionMs)
    }

    fun nextTrack() {
        playbackEngine.nextTrack()
    }

    fun previousTrack() {
        playbackEngine.previousTrack()
    }

    fun toggleShuffle() {
        playbackEngine.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playbackEngine.cycleRepeatMode()
    }

    fun setSpeed(speed: TurntableSpeed) {
        playbackEngine.setSpeed(speed)
    }

    fun setPitchFine(pitchPercent: Float) {
        playbackEngine.setPitchFine(pitchPercent)
    }

    fun setBandGain(bandIndex: Int, levelDb: Float) {
        playbackEngine.setBandGain(bandIndex, levelDb)
    }

    fun applyPreset(presetName: String) {
        tactileEngine.playMechanicalClick()
        playbackEngine.applyPreset(presetName)
    }

    fun setBassBoost(strength: Int) {
        playbackEngine.setBassBoost(strength)
    }

    fun setVirtualizer(strength: Int) {
        playbackEngine.setVirtualizer(strength)
    }

    fun toggleEq(enabled: Boolean) {
        playbackEngine.toggleEq(enabled)
    }

    fun resetEqToFlat() {
        playbackEngine.resetEqToFlat()
        showNotification("Equalizer reset to Flat (0 dB)")
    }

    fun toggleVinylCrackle(enabled: Boolean) {
        tactileEngine.playMechanicalClick()
        playbackEngine.toggleVinylCrackle(enabled)
    }

    fun setVinylCrackleVolume(volume: Float) {
        playbackEngine.setVinylCrackleVolume(volume)
    }

    fun toggleTactileHaptics(enabled: Boolean) {
        tactileEngine.playMechanicalClick()
        playbackEngine.toggleTactileHaptics(enabled)
    }

    fun toggleNeedleSound(enabled: Boolean) {
        tactileEngine.playMechanicalClick()
        playbackEngine.toggleNeedleSound(enabled)
    }

    fun scanDeviceLibrary() {
        viewModelScope.launch {
            _isLibraryScanning.value = true
            tactileEngine.triggerHapticImpulse(15, 100)
            val count = repository.scanAndSyncMediaStore(getApplication())
            _isLibraryScanning.value = false
            showNotification("Found $count local tracks on device")
        }
    }

    fun loadVintageSampler() {
        viewModelScope.launch {
            _isLibraryScanning.value = true
            val samples = repository.loadVintageSampler(getApplication())
            _isLibraryScanning.value = false
            if (playbackEngine.playerState.value.queue.isEmpty() && samples.isNotEmpty()) {
                playbackEngine.setQueue(samples, 0, autoPlay = false)
            }
            showNotification("Loaded ${samples.size} Audiophile Master tracks")
        }
    }

    fun importAudioUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isLibraryScanning.value = true
            val imported = repository.importAudioUris(getApplication(), uris)
            _isLibraryScanning.value = false
            showNotification("Imported ${imported.size} audio files")
            if (imported.isNotEmpty()) {
                playTrack(imported.first())
            }
        }
    }

    fun toggleFavorite(track: AudioTrack) {
        tactileEngine.triggerHapticImpulse(15, 120)
        viewModelScope.launch {
            repository.toggleFavorite(track.id, track.isFavorite)
        }
    }

    fun deleteTrack(track: AudioTrack) {
        viewModelScope.launch {
            repository.removeTrack(track.id)
            showNotification("Removed ${track.title} from library")
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerRemaining.value = minutes
        showNotification("Sleep timer set for $minutes minutes")
        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60000L)
                remaining--
                _sleepTimerRemaining.value = remaining
            }
            // Timer expired, pause playback gracefully
            if (playerState.value.isPlaying) {
                togglePlayPause()
            }
            _sleepTimerRemaining.value = null
            showNotification("Sleep timer ended. Playback paused.")
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemaining.value = null
        showNotification("Sleep timer cancelled")
    }

    fun showNotification(msg: String) {
        _notificationMessage.value = msg
        viewModelScope.launch {
            delay(3000)
            if (_notificationMessage.value == msg) {
                _notificationMessage.value = null
            }
        }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        playbackEngine.release()
    }
}
