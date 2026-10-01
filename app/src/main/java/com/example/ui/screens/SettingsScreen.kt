package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DeckTheme
import com.example.ui.theme.AmberTubeGlow
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

@Composable
fun SettingsScreen(
    isCrackleEnabled: Boolean,
    crackleVolume: Float,
    isHapticsEnabled: Boolean,
    isNeedleSoundEnabled: Boolean,
    sleepTimerRemaining: Int?,
    onToggleCrackle: (Boolean) -> Unit,
    onSetCrackleVolume: (Float) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleNeedleSound: (Boolean) -> Unit,
    onStartSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    deckTheme: DeckTheme = DeckTheme.CAR_CASSETTE,
    onSetDeckTheme: (DeckTheme) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VinylBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Column {
            Text(
                text = "VINTAGE SETTINGS",
                fontFamily = PlayfairFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VintageBrass,
                letterSpacing = 2.sp
            )
            Text(
                text = "TACTILE ACOUSTICS & AUDIOPHILE PREFERENCES",
                fontFamily = CourierPrimeFontFamily,
                fontSize = 8.sp,
                color = MutedIvory,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 0. Visual Player Theme Selection (Car Cassette Deck vs Vinyl Turntable)
        Text(
            text = "DEFAULT PLAYER THEME",
            fontFamily = CourierPrimeFontFamily,
            fontSize = 10.sp,
            color = VintageBrassLight,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DeckTheme.values().forEach { theme ->
                val isSelected = deckTheme == theme
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SurfaceCardElevated else SurfaceCard)
                        .border(
                            1.5.dp,
                            if (isSelected) VintageBrass else Color(0xFF2E2B25),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSetDeckTheme(theme) }
                        .padding(12.dp)
                        .testTag("settings_theme_${theme.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (theme == DeckTheme.CAR_CASSETTE) "📼" else "📀",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = theme.label,
                            fontFamily = PlayfairFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) VintageBrassLight else CreamIvory
                        )
                        Text(
                            text = if (theme == DeckTheme.CAR_CASSETTE) "In-Dash Stereo" else "Platter & Arm",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 9.sp,
                            color = if (isSelected) AmberTubeGlow else DarkMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Tactile Sound Effects Section
        Text(
            text = "TACTILE VINYL ACOUSTICS",
            fontFamily = CourierPrimeFontFamily,
            fontSize = 10.sp,
            color = VintageBrassLight,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, Color(0xFF332F28), RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column {
                // Vinyl crackle toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vinyl Surface Crackle",
                            fontFamily = PlayfairFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CreamIvory
                        )
                        Text(
                            text = "Synthesized micro-pops and groove dust hiss",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.sp,
                            color = MutedIvory
                        )
                    }
                    Switch(
                        checked = isCrackleEnabled,
                        onCheckedChange = onToggleCrackle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF14120C),
                            checkedTrackColor = VintageBrass,
                            uncheckedThumbColor = DarkMuted,
                            uncheckedTrackColor = Color(0xFF24211D)
                        ),
                        modifier = Modifier.testTag("settings_crackle_switch")
                    )
                }

                if (isCrackleEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Surface Noise Level: ${(crackleVolume * 100).toInt()}%",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 11.sp,
                        color = AmberTubeGlow
                    )
                    Slider(
                        value = crackleVolume,
                        onValueChange = onSetCrackleVolume,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberTubeGlow,
                            activeTrackColor = AmberTubeGlow,
                            inactiveTrackColor = Color(0xFF2E2B25)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_crackle_volume_slider")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Needle Drop Acoustics Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Needle Drop & Lift Acoustics",
                            fontFamily = PlayfairFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CreamIvory
                        )
                        Text(
                            text = "Authentic mechanical stylus landing thud and friction slide",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.sp,
                            color = MutedIvory
                        )
                    }
                    Switch(
                        checked = isNeedleSoundEnabled,
                        onCheckedChange = onToggleNeedleSound,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF14120C),
                            checkedTrackColor = VintageBrass,
                            uncheckedThumbColor = DarkMuted,
                            uncheckedTrackColor = Color(0xFF24211D)
                        ),
                        modifier = Modifier.testTag("settings_needle_sound_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tactile Haptics Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tactile Mechanical Haptics",
                            fontFamily = PlayfairFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CreamIvory
                        )
                        Text(
                            text = "Physical haptic pulses on needle drop and switches",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.sp,
                            color = MutedIvory
                        )
                    }
                    Switch(
                        checked = isHapticsEnabled,
                        onCheckedChange = onToggleHaptics,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF14120C),
                            checkedTrackColor = VintageBrass,
                            uncheckedThumbColor = DarkMuted,
                            uncheckedTrackColor = Color(0xFF24211D)
                        ),
                        modifier = Modifier.testTag("settings_haptics_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Sleep Timer Section
        Text(
            text = "VINTAGE SLEEP TIMER",
            fontFamily = CourierPrimeFontFamily,
            fontSize = 10.sp,
            color = VintageBrassLight,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, Color(0xFF332F28), RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (sleepTimerRemaining != null) "Active: ${sleepTimerRemaining}m remaining" else "Timer Inactive",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sleepTimerRemaining != null) AmberTubeGlow else MutedIvory
                    )

                    if (sleepTimerRemaining != null) {
                        Text(
                            text = "Cancel",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53E3E),
                            modifier = Modifier
                                .clickable { onCancelSleepTimer() }
                                .padding(4.dp)
                                .testTag("cancel_sleep_timer")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        val isCurrent = sleepTimerRemaining == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) AmberTubeGlow else Color(0xFF1E1C1A))
                                .clickable { onStartSleepTimer(mins) }
                                .padding(vertical = 8.dp)
                                .testTag("timer_${mins}m"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color(0xFF14120C) else CreamIvory
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Local Audio Guarantee & Specs
        Text(
            text = "SYSTEM ARCHITECTURE & PRIVACY",
            fontFamily = CourierPrimeFontFamily,
            fontSize = 10.sp,
            color = VintageBrassLight,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, Color(0xFF332F28), RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = VintageBrass,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Local Storage Only",
                        fontFamily = PlayfairFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CreamIvory
                    )
                }
                Text(
                    text = "Zero streaming integrations. Zero cloud accounts. Your personal library never leaves your device.",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 10.sp,
                    color = MutedIvory
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = VintageBrass,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Full Lossless Codec Compatibility",
                        fontFamily = PlayfairFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CreamIvory
                    )
                }
                Text(
                    text = "Supports FLAC, WAV, MP3, AAC, M4A, OGG Vorbis, and Opus. Features 24-bit 96kHz / 192kHz linear PCM bit-perfect playback.",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 10.sp,
                    color = MutedIvory
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. The Vintage Philosophy Manifesto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCardElevated)
                .border(1.dp, VintageBrass.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "THE VINTAGE HI-FI PHILOSOPHY",
                    fontFamily = PlayfairFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VintageBrass
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Music was meant to be experienced with tangible presence and mindful attention. Vintage brings the physical joy of rotating vinyl records, needle drops, vacuum tube warmth, and losslessly mastered local tracks back into your hands—free from recommendation algorithms, tracking, and compressed cloud streams.",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 10.sp,
                    color = CreamIvory.copy(alpha = 0.85f),
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ANALOG SOUND CRAFTSMANSHIP",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 8.sp,
                        color = AmberTubeGlow
                    )
                    Text(
                        text = "VERSION 1.0 · BIT-PERFECT",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 8.sp,
                        color = DarkMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
