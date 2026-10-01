package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Generates tactile sound effects:
 * 1. Procedural vinyl surface crackle & dust pops (AudioTrack pink noise + Poisson spikes)
 * 2. Needle drop & needle lift mechanical acoustics
 * 3. Vintage hi-fi mechanical switch clicks
 * 4. Device tactile haptic pulses
 */
class TactileSoundEngine(private val context: Context) {
    private val tag = "TactileSoundEngine"

    private var crackleTrack: AudioTrack? = null
    private var crackleJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    var isCrackleEnabled: Boolean = true
    var crackleVolume: Float = 0.40f
        set(value) {
            field = value.coerceIn(0f, 1f)
            try {
                crackleTrack?.setVolume(field)
            } catch (e: Exception) {
                Log.w(tag, "Could not set crackle volume", e)
            }
        }

    var isHapticsEnabled: Boolean = true
    var isNeedleSoundEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Starts subtle vinyl surface noise and dust crackles.
     */
    fun startVinylCrackle() {
        if (!isCrackleEnabled) return
        if (crackleJob?.isActive == true) return

        crackleJob = scope.launch {
            try {
                val sampleRate = 22050
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(sampleRate * 2) // 2-second buffer

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.setVolume(crackleVolume)
                track.play()
                crackleTrack = track

                val pcmBuffer = ShortArray(1024)
                val random = Random()
                var pinkFilterState = 0.0

                while (isActive) {
                    for (i in pcmBuffer.indices) {
                        // Pink-ish noise generation for vintage surface hiss
                        val white = (random.nextDouble() * 2.0 - 1.0)
                        pinkFilterState = 0.95 * pinkFilterState + 0.05 * white
                        var sample = pinkFilterState * 0.12

                        // Random Poisson micro-crackle spikes (dust particle hitting the diamond stylus)
                        if (random.nextDouble() < 0.003) {
                            val popMagnitude = 0.4 + random.nextDouble() * 0.5
                            val popSign = if (random.nextBoolean()) 1.0 else -1.0
                            sample += popSign * popMagnitude
                        }

                        pcmBuffer[i] = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                    }

                    track.write(pcmBuffer, 0, pcmBuffer.size)
                }
            } catch (e: Exception) {
                Log.w(tag, "Vinyl crackle stream error", e)
            }
        }
    }

    /**
     * Stops the continuous vinyl crackle noise.
     */
    fun stopVinylCrackle() {
        crackleJob?.cancel()
        crackleJob = null
        try {
            crackleTrack?.stop()
            crackleTrack?.release()
        } catch (e: Exception) {
            Log.w(tag, "Error releasing crackle track", e)
        }
        crackleTrack = null
    }

    /**
     * Plays the authentic acoustic needle drop sound:
     * Stylus landing thump + groove lead-in slide.
     */
    fun playNeedleDropSound() {
        triggerHapticImpulse(durationMs = 28, amplitude = 180)
        if (!isNeedleSoundEnabled) return

        scope.launch {
            try {
                val sampleRate = 22050
                val durationSec = 0.45
                val numSamples = (sampleRate * durationSec).toInt()
                val pcm = ShortArray(numSamples)
                val random = Random()

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Low resonant thump (65 Hz) of needle hitting vinyl
                    val thump = sin(2.0 * PI * 65.0 * t) * exp(-18.0 * t)
                    // High-frequency friction click
                    val click = if (t < 0.015) (random.nextDouble() * 2.0 - 1.0) * exp(-200.0 * t) else 0.0
                    // Groove surface sweep
                    val grooveHiss = (random.nextDouble() * 2.0 - 1.0) * 0.15 * (1.0 - exp(-10.0 * t)) * exp(-3.0 * t)

                    val mixed = (thump * 0.7 + click * 0.5 + grooveHiss * 0.4)
                    pcm[i] = (mixed.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                }

                playOneShotPcm(pcm, sampleRate)
            } catch (e: Exception) {
                Log.w(tag, "Needle drop sound error", e)
            }
        }
    }

    /**
     * Plays needle lift sound when playback is paused or tonearm returned.
     */
    fun playNeedleLiftSound() {
        triggerHapticImpulse(durationMs = 18, amplitude = 120)
        if (!isNeedleSoundEnabled) return

        scope.launch {
            try {
                val sampleRate = 22050
                val durationSec = 0.20
                val numSamples = (sampleRate * durationSec).toInt()
                val pcm = ShortArray(numSamples)
                val random = Random()

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val liftFriction = (random.nextDouble() * 2.0 - 1.0) * exp(-25.0 * t) * 0.4
                    val click = sin(2.0 * PI * 850.0 * t) * exp(-80.0 * t) * 0.3
                    val mixed = liftFriction + click
                    pcm[i] = (mixed.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                }

                playOneShotPcm(pcm, sampleRate)
            } catch (e: Exception) {
                Log.w(tag, "Needle lift sound error", e)
            }
        }
    }

    /**
     * Plays a crisp mechanical switch click sound (simulating heavy bakelite / aluminum toggle).
     */
    fun playMechanicalClick() {
        triggerHapticImpulse(durationMs = 12, amplitude = 90)

        scope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * 0.04).toInt()
                val pcm = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Double pulse mechanical relay snap
                    val click1 = sin(2.0 * PI * 1800.0 * t) * exp(-300.0 * t)
                    val click2 = if (t > 0.008) sin(2.0 * PI * 2400.0 * (t - 0.008)) * exp(-250.0 * (t - 0.008)) else 0.0
                    val mixed = (click1 + click2) * 0.6
                    pcm[i] = (mixed.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                }

                playOneShotPcm(pcm, sampleRate)
            } catch (e: Exception) {
                Log.w(tag, "Mechanical click error", e)
            }
        }
    }

    private fun playOneShotPcm(pcm: ShortArray, sampleRate: Int) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcm, 0, pcm.size)
        track.play()

        scope.launch {
            kotlinx.coroutines.delay((pcm.size * 1000L / sampleRate) + 100)
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    fun triggerHapticImpulse(durationMs: Long = 20, amplitude: Int = 150) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        amplitude.coerceIn(1, 255)
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        stopVinylCrackle()
    }
}
