package com.example.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.data.model.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

enum class TurntableSpeed(val rpmLabel: String, val speedFactor: Float) {
    RPM_33("33 ⅓ RPM", 1.0f),
    RPM_45("45 RPM", 1.35f),
    RPM_78("78 RPM", 2.34f)
}

enum class RepeatMode {
    OFF, ALL, ONE
}

data class EqBandInfo(
    val index: Int,
    val centerFreqHz: Int,
    val levelDb: Float, // e.g. -10 dB to +10 dB
    val minDb: Float = -12f,
    val maxDb: Float = 12f
)

enum class DeckTheme(val label: String) {
    CAR_CASSETTE("Car Cassette"),
    VINYL_TURNTABLE("Vinyl Platter")
}

data class PlayerState(
    val currentTrack: AudioTrack? = null,
    val isPlaying: Boolean = false,
    val isPreparing: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val progress: Float = 0f, // 0.0f to 1.0f (drives tonearm radius!)
    val speed: TurntableSpeed = TurntableSpeed.RPM_33,
    val pitchFinePercent: Float = 0f, // -8% to +8%
    val isTonearmDropped: Boolean = false, // Visual needle down on record
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val isShuffle: Boolean = false,
    val queue: List<AudioTrack> = emptyList(),
    val queueIndex: Int = -1,
    // VU Meter levels (0.0f to 1.0f)
    val vuLeftLevel: Float = 0f,
    val vuRightLevel: Float = 0f,
    // Equalizer & AudioFX
    val isEqEnabled: Boolean = true,
    val eqBands: List<EqBandInfo> = emptyList(),
    val selectedPresetName: String = "Warm Tube",
    val bassBoostStrength: Int = 300, // 0 to 1000
    val virtualizerStrength: Int = 200, // 0 to 1000
    // Tactile settings
    val isVinylCrackleEnabled: Boolean = true,
    val vinylCrackleVolume: Float = 0.35f,
    val isTactileHapticsEnabled: Boolean = true,
    val isNeedleSoundEnabled: Boolean = true,
    // Theme & Cassette Deck state
    val deckTheme: DeckTheme = DeckTheme.CAR_CASSETTE,
    val isTapeInserted: Boolean = true
)

/**
 * Core audio playback engine implemented using Android's Media3 ExoPlayer with MediaSession.
 * Handles local audio files including FLAC lossless, WAV, MP3, AAC, OGG Vorbis, and Opus.
 */
class AudioPlaybackEngine(
    private val context: Context,
    val tactileEngine: TactileSoundEngine
) {
    private val tag = "AudioPlaybackEngine"
    private val scope = CoroutineScope(Dispatchers.Main)

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private var mediaSession: MediaSession? = null

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var progressJob: Job? = null
    private var vuMeterJob: Job? = null

    private val originalQueue = mutableListOf<AudioTrack>()

    val availablePresets = listOf(
        "Flat",
        "Warm Tube",
        "Deep Vinyl Bass",
        "Acoustic Hi-Fi",
        "Jazz Lounge",
        "Crisp Vocals",
        "Rock Classic"
    )

    init {
        setupMediaSession()
        setupExoPlayerListener()
        startProgressTracker()
        startVuMeterSimulation()
    }

    private fun setupMediaSession() {
        try {
            mediaSession = MediaSession.Builder(context, exoPlayer)
                .setId("VintageMediaSession")
                .build()
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize MediaSession", e)
        }
    }

    private fun setupExoPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(
                    isPlaying = isPlaying,
                    isTonearmDropped = isPlaying
                )
                if (isPlaying) {
                    tactileEngine.startVinylCrackle()
                } else {
                    tactileEngine.stopVinylCrackle()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playerState.value = _playerState.value.copy(isPreparing = true)
                    }
                    Player.STATE_READY -> {
                        val duration = exoPlayer.duration.coerceAtLeast(0L)
                        _playerState.value = _playerState.value.copy(
                            isPreparing = false,
                            durationMs = if (duration > 0) duration else _playerState.value.durationMs
                        )
                        setupAudioEffects(exoPlayer.audioSessionId)
                    }
                    Player.STATE_ENDED -> {
                        onTrackFinished()
                    }
                    Player.STATE_IDLE -> {
                        _playerState.value = _playerState.value.copy(isPreparing = false)
                    }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                setupAudioEffects(audioSessionId)
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(tag, "ExoPlayer playback error: ${error.errorCodeName} - ${error.message}", error)
                _playerState.value = _playerState.value.copy(
                    isPlaying = false,
                    isPreparing = false
                )
            }
        })
    }

    fun setQueue(tracks: List<AudioTrack>, startIndex: Int = 0, autoPlay: Boolean = false) {
        if (tracks.isEmpty()) return
        originalQueue.clear()
        originalQueue.addAll(tracks)

        val queueToUse = if (_playerState.value.isShuffle) tracks.shuffled() else tracks
        val validIndex = startIndex.coerceIn(0, queueToUse.lastIndex)

        _playerState.value = _playerState.value.copy(
            queue = queueToUse,
            queueIndex = validIndex,
            currentTrack = queueToUse[validIndex]
        )

        if (autoPlay) {
            playTrack(queueToUse[validIndex])
        }
    }

    fun playTrack(track: AudioTrack) {
        tactileEngine.playNeedleDropSound()
        tactileEngine.startVinylCrackle()

        _playerState.value = _playerState.value.copy(
            currentTrack = track,
            isPreparing = true,
            isTonearmDropped = true,
            currentPositionMs = 0L,
            durationMs = track.durationMs,
            progress = 0f
        )

        try {
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(track.uriString))
                .setMediaMetadata(mediaMetadata)
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            applyPlaybackSpeedAndPitch()
            exoPlayer.play()
        } catch (e: Exception) {
            Log.e(tag, "ExoPlayer error loading track: ${track.title}", e)
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isPreparing = false
            )
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.currentMediaItem == null) {
            val track = _playerState.value.currentTrack
                ?: _playerState.value.queue.getOrNull(_playerState.value.queueIndex)
            if (track != null) {
                playTrack(track)
            }
            return
        }

        if (exoPlayer.isPlaying) {
            tactileEngine.playNeedleLiftSound()
            tactileEngine.stopVinylCrackle()
            exoPlayer.pause()
        } else {
            tactileEngine.playNeedleDropSound()
            tactileEngine.startVinylCrackle()
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val validPos = positionMs.coerceIn(0L, _playerState.value.durationMs)
        exoPlayer.seekTo(validPos)
        val progress = if (_playerState.value.durationMs > 0) {
            validPos.toFloat() / _playerState.value.durationMs
        } else 0f

        _playerState.value = _playerState.value.copy(
            currentPositionMs = validPos,
            progress = progress
        )
    }

    fun nextTrack() {
        tactileEngine.playMechanicalClick()
        val q = _playerState.value.queue
        if (q.isEmpty()) return

        val nextIndex = (_playerState.value.queueIndex + 1) % q.size
        _playerState.value = _playerState.value.copy(queueIndex = nextIndex)
        playTrack(q[nextIndex])
    }

    fun previousTrack() {
        tactileEngine.playMechanicalClick()
        if (exoPlayer.currentPosition > 3000) {
            seekTo(0)
            return
        }

        val q = _playerState.value.queue
        if (q.isEmpty()) return

        val prevIndex = if (_playerState.value.queueIndex - 1 < 0) q.size - 1 else _playerState.value.queueIndex - 1
        _playerState.value = _playerState.value.copy(queueIndex = prevIndex)
        playTrack(q[prevIndex])
    }

    private fun onTrackFinished() {
        when (_playerState.value.repeatMode) {
            RepeatMode.ONE -> {
                val current = _playerState.value.currentTrack
                if (current != null) playTrack(current)
            }
            RepeatMode.ALL -> {
                nextTrack()
            }
            RepeatMode.OFF -> {
                val nextIdx = _playerState.value.queueIndex + 1
                if (nextIdx < _playerState.value.queue.size) {
                    _playerState.value = _playerState.value.copy(queueIndex = nextIdx)
                    playTrack(_playerState.value.queue[nextIdx])
                } else {
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        isTonearmDropped = false,
                        progress = 0f
                    )
                    tactileEngine.stopVinylCrackle()
                }
            }
        }
    }

    fun setSpeed(speed: TurntableSpeed) {
        tactileEngine.playMechanicalClick()
        _playerState.value = _playerState.value.copy(speed = speed)
        applyPlaybackSpeedAndPitch()
    }

    fun setPitchFine(pitchPercent: Float) {
        val clamped = pitchPercent.coerceIn(-8f, 8f)
        _playerState.value = _playerState.value.copy(pitchFinePercent = clamped)
        applyPlaybackSpeedAndPitch()
    }

    private fun applyPlaybackSpeedAndPitch() {
        try {
            val baseSpeed = _playerState.value.speed.speedFactor
            val pitchMultiplier = 1.0f + (_playerState.value.pitchFinePercent / 100.0f)
            val finalSpeed = (baseSpeed * pitchMultiplier).coerceIn(0.25f, 3.0f)

            // Simulating realistic turntable pitch: speeding up vinyl also pitches audio proportionally
            exoPlayer.playbackParameters = PlaybackParameters(finalSpeed, finalSpeed)
        } catch (e: Exception) {
            Log.w(tag, "Failed to apply PlaybackParameters on ExoPlayer", e)
        }
    }

    fun toggleShuffle() {
        tactileEngine.playMechanicalClick()
        val currentShuffle = _playerState.value.isShuffle
        val newShuffle = !currentShuffle
        val currentTrack = _playerState.value.currentTrack

        val newQueue = if (newShuffle) {
            originalQueue.shuffled()
        } else {
            originalQueue.toList()
        }

        val newIndex = if (currentTrack != null) {
            newQueue.indexOfFirst { it.id == currentTrack.id }.coerceAtLeast(0)
        } else 0

        _playerState.value = _playerState.value.copy(
            isShuffle = newShuffle,
            queue = newQueue,
            queueIndex = newIndex
        )
    }

    fun cycleRepeatMode() {
        tactileEngine.playMechanicalClick()
        val nextMode = when (_playerState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playerState.value = _playerState.value.copy(repeatMode = nextMode)
    }

    /**
     * Connects Android hardware Equalizer, BassBoost, Virtualizer on ExoPlayer's audioSessionId.
     */
    private fun setupAudioEffects(audioSessionId: Int) {
        if (audioSessionId == 0) return
        try {
            releaseAudioEffects()

            val eq = Equalizer(0, audioSessionId).apply {
                enabled = _playerState.value.isEqEnabled
            }
            equalizer = eq

            val bands = mutableListOf<EqBandInfo>()
            val numBands = eq.numberOfBands.toInt()
            val minRange = eq.bandLevelRange[0] / 100f // In dB
            val maxRange = eq.bandLevelRange[1] / 100f

            for (i in 0 until numBands) {
                val centerFreqHz = eq.getCenterFreq(i.toShort()) / 1000 // Convert mHz to Hz
                val levelDb = eq.getBandLevel(i.toShort()) / 100f
                bands.add(
                    EqBandInfo(
                        index = i,
                        centerFreqHz = centerFreqHz,
                        levelDb = levelDb,
                        minDb = minRange,
                        maxDb = maxRange
                    )
                )
            }

            val bb = BassBoost(0, audioSessionId).apply {
                enabled = true
                if (strengthSupported) {
                    setStrength(_playerState.value.bassBoostStrength.toShort())
                }
            }
            bassBoost = bb

            val virt = Virtualizer(0, audioSessionId).apply {
                enabled = true
                if (strengthSupported) {
                    setStrength(_playerState.value.virtualizerStrength.toShort())
                }
            }
            virtualizer = virt

            _playerState.value = _playerState.value.copy(eqBands = bands)
            applyPreset(_playerState.value.selectedPresetName)
        } catch (e: Exception) {
            Log.e(tag, "AudioFX setup error with ExoPlayer", e)
        }
    }

    fun setBandGain(bandIndex: Int, levelDb: Float) {
        val eq = equalizer ?: return
        try {
            val levelMb = (levelDb * 100).toInt().toShort()
            eq.setBandLevel(bandIndex.toShort(), levelMb)

            val updatedBands = _playerState.value.eqBands.map { band ->
                if (band.index == bandIndex) band.copy(levelDb = levelDb) else band
            }
            _playerState.value = _playerState.value.copy(
                eqBands = updatedBands,
                selectedPresetName = "Custom"
            )
        } catch (e: Exception) {
            Log.w(tag, "Failed to set band gain", e)
        }
    }

    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _playerState.value = _playerState.value.copy(bassBoostStrength = clamped)
        try {
            bassBoost?.setStrength(clamped.toShort())
        } catch (e: Exception) {
            Log.w(tag, "Failed to set bass boost", e)
        }
    }

    fun setVirtualizer(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _playerState.value = _playerState.value.copy(virtualizerStrength = clamped)
        try {
            virtualizer?.setStrength(clamped.toShort())
        } catch (e: Exception) {
            Log.w(tag, "Failed to set virtualizer", e)
        }
    }

    fun applyPreset(presetName: String) {
        val eq = equalizer
        val bands = _playerState.value.eqBands
        if (bands.isEmpty()) return

        // Gain curves in dB for 5 bands (Bass, Low-Mid, Mid, High-Mid, Treble)
        val gains: List<Float> = when (presetName) {
            "Warm Tube" -> listOf(3.5f, 2.0f, 0.5f, -1.0f, -2.5f)
            "Deep Vinyl Bass" -> listOf(6.0f, 3.5f, 0.0f, -0.5f, 1.0f)
            "Acoustic Hi-Fi" -> listOf(2.0f, 1.0f, 0.5f, 2.5f, 4.0f)
            "Jazz Lounge" -> listOf(2.5f, 1.5f, -0.5f, 1.0f, 2.0f)
            "Crisp Vocals" -> listOf(-1.5f, 1.0f, 4.0f, 3.0f, 1.5f)
            "Rock Classic" -> listOf(4.5f, 2.0f, -1.5f, 2.0f, 4.0f)
            else -> listOf(0f, 0f, 0f, 0f, 0f) // Flat
        }

        val updatedBands = bands.mapIndexed { idx, band ->
            val gain = gains.getOrElse(idx) { 0f }
            try {
                eq?.setBandLevel(idx.toShort(), (gain * 100).toInt().toShort())
            } catch (_: Exception) {}
            band.copy(levelDb = gain)
        }

        _playerState.value = _playerState.value.copy(
            selectedPresetName = presetName,
            eqBands = updatedBands
        )
    }

    fun toggleEq(enabled: Boolean) {
        tactileEngine.playMechanicalClick()
        _playerState.value = _playerState.value.copy(isEqEnabled = enabled)
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            Log.w(tag, "Failed to toggle EQ state", e)
        }
    }

    fun resetEqToFlat() {
        tactileEngine.playMechanicalClick()
        applyPreset("Flat")
        setBassBoost(0)
        setVirtualizer(0)
    }

    fun toggleVinylCrackle(enabled: Boolean) {
        tactileEngine.isCrackleEnabled = enabled
        _playerState.value = _playerState.value.copy(isVinylCrackleEnabled = enabled)
        if (enabled && _playerState.value.isPlaying) {
            tactileEngine.startVinylCrackle()
        } else {
            tactileEngine.stopVinylCrackle()
        }
    }

    fun setVinylCrackleVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        tactileEngine.crackleVolume = clamped
        _playerState.value = _playerState.value.copy(vinylCrackleVolume = clamped)
    }

    fun toggleTactileHaptics(enabled: Boolean) {
        tactileEngine.isHapticsEnabled = enabled
        _playerState.value = _playerState.value.copy(isTactileHapticsEnabled = enabled)
    }

    fun toggleNeedleSound(enabled: Boolean) {
        tactileEngine.isNeedleSoundEnabled = enabled
        _playerState.value = _playerState.value.copy(isNeedleSoundEnabled = enabled)
    }

    fun setDeckTheme(theme: DeckTheme) {
        tactileEngine.playMechanicalClick()
        _playerState.value = _playerState.value.copy(deckTheme = theme)
    }

    fun toggleTapeEject() {
        val currentlyInserted = _playerState.value.isTapeInserted
        if (currentlyInserted) {
            tactileEngine.playCassetteEjectSound()
            _playerState.value = _playerState.value.copy(isTapeInserted = false)
            if (_playerState.value.isPlaying) {
                exoPlayer.pause()
            }
        } else {
            tactileEngine.playCassetteInsertSound()
            _playerState.value = _playerState.value.copy(isTapeInserted = true)
            if (_playerState.value.currentTrack != null) {
                exoPlayer.play()
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                if (_playerState.value.isPlaying) {
                    try {
                        val current = exoPlayer.currentPosition.coerceAtLeast(0L)
                        val duration = exoPlayer.duration.coerceAtLeast(1L)
                        val progress = (current.toFloat() / duration).coerceIn(0f, 1f)

                        _playerState.value = _playerState.value.copy(
                            currentPositionMs = current,
                            durationMs = duration,
                            progress = progress
                        )
                    } catch (_: Exception) {}
                }
                delay(100)
            }
        }
    }

    /**
     * Drives dual analog VU meters with authentic needle ballistics (rise and decay damping).
     */
    private fun startVuMeterSimulation() {
        vuMeterJob?.cancel()
        vuMeterJob = scope.launch {
            var leftNeedle = 0f
            var rightNeedle = 0f
            var phase = 0.0

            while (isActive) {
                if (_playerState.value.isPlaying) {
                    phase += 0.25
                    val baseBeat = (sin(phase) * 0.5 + 0.5).toFloat()
                    val bassExtra = (_playerState.value.bassBoostStrength / 1000f) * 0.2f
                    val targetLeft = ((baseBeat * 0.6f + Random.nextFloat() * 0.35f + bassExtra)
                        .coerceIn(0.1f, 0.95f))
                    val targetRight = ((baseBeat * 0.55f + Random.nextFloat() * 0.40f + bassExtra)
                        .coerceIn(0.1f, 0.95f))

                    // Needle damping
                    leftNeedle = if (targetLeft > leftNeedle) {
                        leftNeedle + (targetLeft - leftNeedle) * 0.6f
                    } else {
                        leftNeedle - (leftNeedle - targetLeft) * 0.25f
                    }

                    rightNeedle = if (targetRight > rightNeedle) {
                        rightNeedle + (targetRight - rightNeedle) * 0.6f
                    } else {
                        rightNeedle - (rightNeedle - targetRight) * 0.25f
                    }
                } else {
                    leftNeedle = (leftNeedle - 0.08f).coerceAtLeast(0f)
                    rightNeedle = (rightNeedle - 0.08f).coerceAtLeast(0f)
                }

                _playerState.value = _playerState.value.copy(
                    vuLeftLevel = leftNeedle,
                    vuRightLevel = rightNeedle
                )

                delay(50)
            }
        }
    }

    private fun releaseAudioEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
    }

    fun release() {
        progressJob?.cancel()
        vuMeterJob?.cancel()
        releaseAudioEffects()
        try {
            mediaSession?.release()
            mediaSession = null
            exoPlayer.stop()
            exoPlayer.release()
        } catch (_: Exception) {}
        tactileEngine.release()
    }
}
