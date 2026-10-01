package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PlayerState
import com.example.audio.RepeatMode
import com.example.audio.TurntableSpeed
import com.example.data.model.AudioTrack
import com.example.ui.components.DualVuMeterView
import com.example.ui.components.RotatingVinylRecord
import com.example.ui.theme.AmberTubeGlow
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.CourierPrimeFontFamily
import com.example.ui.theme.CreamIvory
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.VintageBrass
import com.example.ui.theme.VintageBrassLight
import com.example.ui.theme.VinylBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerState: PlayerState,
    sleepTimerRemaining: Int?,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSetSpeed: (TurntableSpeed) -> Unit,
    onSetPitchFine: (Float) -> Unit,
    onToggleCrackle: (Boolean) -> Unit,
    onSetCrackleVolume: (Float) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onSelectQueueTrack: (AudioTrack) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSpecsDialog by remember { mutableStateOf(false) }

    val track = playerState.currentTrack
    val currentProgress = if (isScrubbing) scrubProgress else playerState.progress
    val currentPosMs = if (isScrubbing) {
        (scrubProgress * playerState.durationMs).toLong()
    } else {
        playerState.currentPositionMs
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VinylBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Bar: App Monogram, Hi-Res Badge & Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "V I N T A G E",
                    fontFamily = PlayfairFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = VintageBrass,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "ANALOG HI-FI MASTER",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 8.sp,
                    color = MutedIvory,
                    letterSpacing = 1.sp
                )
            }

            // Audio Spec Badge (FLAC 24-bit / 96kHz, WAV, etc.)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (sleepTimerRemaining != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AmberTubeGlow.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Sleep Timer",
                                tint = AmberTubeGlow,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${sleepTimerRemaining}m",
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 9.sp,
                                color = AmberTubeGlow
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceCard)
                        .border(1.dp, VintageBrass.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .clickable { showSpecsDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("specs_badge_button")
                ) {
                    Text(
                        text = track?.let { "${it.codec} · ${it.bitDepth}" } ?: "FLAC LOSSLESS",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = VintageBrassLight,
                        letterSpacing = 0.8.sp
                    )
                }

                IconButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_eq_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Equalizer",
                        tint = VintageBrass,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { showQueueSheet = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_queue_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = VintageBrass,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Animated Vinyl Turntable (Rotating vinyl record synced with playback state)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.15f)
        ) {
            RotatingVinylRecord(
                isPlaying = playerState.isPlaying,
                progress = playerState.progress,
                speed = playerState.speed,
                title = track?.title ?: "",
                artist = track?.artist ?: "",
                showTonearm = true,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Audio Metadata (Strictly clean, NO album art, vintage typography)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = track?.title ?: "Select a Track",
                fontFamily = PlayfairFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CreamIvory,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("track_title_text")
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = track?.artist ?: "Local Hi-Fi Collection",
                fontFamily = PlayfairFontFamily,
                fontSize = 15.sp,
                color = VintageBrass,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("track_artist_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = track?.let { "${it.album} · ${it.sampleRate}" } ?: "Direct Local Audio Playback",
                fontFamily = CourierPrimeFontFamily,
                fontSize = 11.sp,
                color = MutedIvory.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Dual VU Meter (Left and Right dancing needles)
        DualVuMeterView(
            leftLevel = playerState.vuLeftLevel,
            rightLevel = playerState.vuRightLevel,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Scrub Bar & Monospace Vintage Time Counter
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = currentProgress,
                onValueChange = {
                    isScrubbing = true
                    scrubProgress = it
                },
                onValueChangeFinished = {
                    onSeek((scrubProgress * playerState.durationMs).toLong())
                    isScrubbing = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = VintageBrass,
                    activeTrackColor = VintageBrass,
                    inactiveTrackColor = Color(0xFF2E2B26)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playback_seekbar")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = AudioTrack.formatDuration(currentPosMs),
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 12.sp,
                    color = CreamIvory
                )
                Text(
                    text = "PROGRESS TRACKER",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 9.sp,
                    color = DarkMuted
                )
                Text(
                    text = AudioTrack.formatDuration(playerState.durationMs),
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 12.sp,
                    color = MutedIvory
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Turntable Speed Selector (Vintage Mechanical Push Buttons: 33 ⅓, 45, 78 RPM)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TurntableSpeed.values().forEach { speedOption ->
                val isSelected = playerState.speed == speedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) VintageBrass else Color.Transparent)
                        .clickable { onSetSpeed(speedOption) }
                        .padding(vertical = 7.dp)
                        .testTag("speed_${speedOption.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = speedOption.rpmLabel,
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF14120C) else MutedIvory
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6b. Precision Analog Pitch Fader & Quartz Lock (±8.0%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, Color(0xFF2E2B25), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isQuartzLocked = kotlin.math.abs(playerState.pitchFinePercent) < 0.1f
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isQuartzLocked) AmberTubeGlow else DarkMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isQuartzLocked) "QUARTZ LOCK · STROBE SYNC" else "VARIABLE PITCH CONTROL",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isQuartzLocked) AmberTubeGlow else MutedIvory
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "%+.1f%%".format(playerState.pitchFinePercent),
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (kotlin.math.abs(playerState.pitchFinePercent) < 0.1f) VintageBrassLight else AmberTubeGlow
                        )
                        if (kotlin.math.abs(playerState.pitchFinePercent) >= 0.1f) {
                            Text(
                                text = "RESET",
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = VintageBrass,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF24201B))
                                    .clickable { onSetPitchFine(0f) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("pitch_reset_button")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Slider(
                    value = playerState.pitchFinePercent,
                    onValueChange = onSetPitchFine,
                    valueRange = -8f..8f,
                    colors = SliderDefaults.colors(
                        thumbColor = VintageBrass,
                        activeTrackColor = VintageBrass,
                        inactiveTrackColor = Color(0xFF2C2823)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fine_pitch_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Master Hi-Fi Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            IconButton(
                onClick = onToggleShuffle,
                modifier = Modifier.testTag("shuffle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playerState.isShuffle) AmberTubeGlow else DarkMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Previous Button
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("previous_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = CreamIvory,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Big Tactile Play/Pause Rocker
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(SurfaceCardElevated)
                    .border(2.5.dp, VintageBrass, CircleShape)
                    .clickable { onPlayPause() }
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                    tint = VintageBrassLight,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next Button
            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("next_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = CreamIvory,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Repeat Button
            IconButton(
                onClick = onCycleRepeat,
                modifier = Modifier.testTag("repeat_button")
            ) {
                Icon(
                    imageVector = when (playerState.repeatMode) {
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        RepeatMode.ALL -> Icons.Default.Repeat
                        RepeatMode.OFF -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat Mode",
                    tint = if (playerState.repeatMode != RepeatMode.OFF) AmberTubeGlow else DarkMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 8. Vinyl Surface Noise & Tactile Quick Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, Color(0xFF2F2B25), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onToggleCrackle(!playerState.isVinylCrackleEnabled) }
                        .testTag("toggle_crackle_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (playerState.isVinylCrackleEnabled) AmberTubeGlow else DarkMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "VINYL SURFACE NOISE",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playerState.isVinylCrackleEnabled) VintageBrass else MutedIvory
                        )
                        Text(
                            text = if (playerState.isVinylCrackleEnabled) "Needle crackle active" else "Clean digital bypass",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 8.sp,
                            color = MutedIvory.copy(alpha = 0.6f)
                        )
                    }
                }

                if (playerState.isVinylCrackleEnabled) {
                    Slider(
                        value = playerState.vinylCrackleVolume,
                        onValueChange = onSetCrackleVolume,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberTubeGlow,
                            activeTrackColor = AmberTubeGlow,
                            inactiveTrackColor = Color(0xFF383228)
                        ),
                        modifier = Modifier
                            .width(110.dp)
                            .testTag("crackle_volume_slider")
                    )
                }
            }
        }
    }

    // Modal Queue Sheet
    if (showQueueSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = sheetState,
            containerColor = ChassisDark,
            contentColor = CreamIvory
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT PHONO QUEUE",
                            fontFamily = PlayfairFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VintageBrass
                        )
                        Text(
                            text = "${playerState.queue.size} TRACKS · DIRECT MEMORY BUFFER",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 9.sp,
                            color = MutedIvory
                        )
                    }
                    IconButton(onClick = { showQueueSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MutedIvory,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (playerState.queue.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Queue is empty. Select tracks from the library.",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 12.sp,
                            color = DarkMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(playerState.queue) { idx, qTrack ->
                            val isCurrent = qTrack.id == playerState.currentTrack?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrent) SurfaceCardElevated else SurfaceCard)
                                    .border(
                                        1.dp,
                                        if (isCurrent) VintageBrass.copy(alpha = 0.6f) else Color(0xFF282520),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        onSelectQueueTrack(qTrack)
                                        showQueueSheet = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "%02d".format(idx + 1),
                                    fontFamily = CourierPrimeFontFamily,
                                    fontSize = 11.sp,
                                    color = if (isCurrent) AmberTubeGlow else DarkMuted,
                                    modifier = Modifier.width(26.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = qTrack.title,
                                        fontFamily = PlayfairFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) VintageBrassLight else CreamIvory,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${qTrack.artist} · ${qTrack.codec}",
                                        fontFamily = CourierPrimeFontFamily,
                                        fontSize = 10.sp,
                                        color = MutedIvory,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = qTrack.formattedDuration,
                                    fontFamily = CourierPrimeFontFamily,
                                    fontSize = 10.sp,
                                    color = MutedIvory
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Specs Dialog
    if (showSpecsDialog) {
        AlertDialog(
            onDismissRequest = { showSpecsDialog = false },
            containerColor = SurfaceCardElevated,
            title = {
                Text(
                    text = "AUDIOPHILE SIGNAL PATH",
                    fontFamily = PlayfairFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VintageBrass
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val activeTrack = playerState.currentTrack
                    val specs = listOf(
                        "Codec Format" to (activeTrack?.codec ?: "FLAC Lossless"),
                        "Bit Depth" to (activeTrack?.bitDepth ?: "24-bit linear PCM"),
                        "Sampling Rate" to (activeTrack?.sampleRate ?: "96.0 kHz Native"),
                        "Decoding Pipeline" to "Media3 Bit-Perfect Engine",
                        "Equalizer Stage" to (if (playerState.isEqEnabled) playerState.selectedPresetName else "Pure Analog Bypass"),
                        "Surface Crackle" to (if (playerState.isVinylCrackleEnabled) "${(playerState.vinylCrackleVolume * 100).toInt()}%" else "Disabled")
                    )
                    specs.forEach { (label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 11.sp,
                                color = MutedIvory
                            )
                            Text(
                                text = value,
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CreamIvory
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpecsDialog = false }) {
                    Text(
                        text = "CLOSE",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VintageBrass
                    )
                }
            }
        )
    }
}
