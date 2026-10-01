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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.EqBandInfo
import com.example.ui.theme.AmberGlowDim
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
fun EqualizerScreen(
    isEqEnabled: Boolean,
    eqBands: List<EqBandInfo>,
    selectedPreset: String,
    availablePresets: List<String>,
    bassBoost: Int,
    virtualizer: Int,
    onToggleEq: (Boolean) -> Unit,
    onResetFlat: () -> Unit,
    onPresetSelected: (String) -> Unit,
    onBandGainChanged: (Int, Float) -> Unit,
    onBassBoostChanged: (Int) -> Unit,
    onVirtualizerChanged: (Int) -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HI-FI EQUALIZER",
                    fontFamily = PlayfairFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = VintageBrass,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "PARAMETRIC ANALOG TONE ARCHITECTURE",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 8.sp,
                    color = MutedIvory,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceCard)
                    .border(1.dp, VintageBrass.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isEqEnabled) selectedPreset.uppercase() else "BYPASS",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEqEnabled) AmberTubeGlow else DarkMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Master DSP Stage Bypass / Active Switch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, Color(0xFF332F28), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isEqEnabled) AmberTubeGlow else DarkMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isEqEnabled) "DSP EQUALIZER STAGE: ACTIVE" else "DSP STAGE: HARDWARE BYPASS",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEqEnabled) VintageBrassLight else MutedIvory
                        )
                        Text(
                            text = if (isEqEnabled) "Real-time biquad audio tone filtering" else "Pure bitstream direct audio output",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 8.sp,
                            color = MutedIvory.copy(alpha = 0.6f)
                        )
                    }
                }

                Switch(
                    checked = isEqEnabled,
                    onCheckedChange = onToggleEq,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF14120C),
                        checkedTrackColor = VintageBrass,
                        uncheckedThumbColor = DarkMuted,
                        uncheckedTrackColor = Color(0xFF24211D)
                    ),
                    modifier = Modifier.testTag("eq_power_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACOUSTIC PRESETS",
                fontFamily = CourierPrimeFontFamily,
                fontSize = 10.sp,
                color = MutedIvory,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "RESET TO FLAT",
                fontFamily = CourierPrimeFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = VintageBrass,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF221F1B))
                    .clickable { onResetFlat() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("reset_flat_button")
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val visiblePresets = availablePresets.take(4)
            visiblePresets.forEach { preset ->
                val isSelected = selectedPreset == preset
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) VintageBrass else SurfaceCard)
                        .border(1.dp, if (isSelected) VintageBrassLight else Color(0xFF332F28), RoundedCornerShape(6.dp))
                        .clickable { onPresetSelected(preset) }
                        .padding(vertical = 7.dp)
                        .testTag("preset_$preset"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset,
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF14120C) else CreamIvory,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val remainingPresets = availablePresets.drop(4)
            remainingPresets.forEach { preset ->
                val isSelected = selectedPreset == preset
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) VintageBrass else SurfaceCard)
                        .border(1.dp, if (isSelected) VintageBrassLight else Color(0xFF332F28), RoundedCornerShape(6.dp))
                        .clickable { onPresetSelected(preset) }
                        .padding(vertical = 7.dp)
                        .testTag("preset_$preset"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset,
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF14120C) else CreamIvory,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5-Band Equalizer Console
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .border(1.dp, VintageBrass.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "5-BAND FREQUENCY SHAPING",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VintageBrassLight
                    )
                    Text(
                        text = "±12 dB RANGE",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 9.sp,
                        color = DarkMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Faders
                val defaultFrequencies = listOf(60, 230, 910, 3600, 14000)
                val bandsToRender = if (eqBands.isNotEmpty()) eqBands else {
                    defaultFrequencies.mapIndexed { idx, freq ->
                        EqBandInfo(idx, freq, 0f, -12f, 12f)
                    }
                }

                bandsToRender.forEach { band ->
                    val freqLabel = if (band.centerFreqHz >= 1000) {
                        "%.1f kHz".format(band.centerFreqHz / 1000f)
                    } else {
                        "${band.centerFreqHz} Hz"
                    }

                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = freqLabel,
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CreamIvory
                            )
                            Text(
                                text = "%+.1f dB".format(band.levelDb),
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 11.sp,
                                color = if (band.levelDb > 0) AmberTubeGlow else if (band.levelDb < 0) MutedIvory else DarkMuted
                            )
                        }

                        Slider(
                            value = band.levelDb,
                            onValueChange = { onBandGainChanged(band.index, it) },
                            valueRange = band.minDb..band.maxDb,
                            enabled = isEqEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = VintageBrass,
                                activeTrackColor = VintageBrass,
                                inactiveTrackColor = Color(0xFF2E2B25),
                                disabledThumbColor = DarkMuted,
                                disabledActiveTrackColor = DarkMuted.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("eq_band_${band.index}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Analog Tube Enhancements (Bass Boost & 3D Soundstage)
        Text(
            text = "ANALOG HARMONIC STAGES",
            fontFamily = CourierPrimeFontFamily,
            fontSize = 10.sp,
            color = MutedIvory,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bass Boost Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard)
                    .border(1.dp, Color(0xFF332F28), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "BASS BOOST",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberTubeGlow
                    )
                    Text(
                        text = "${bassBoost / 10}%",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CreamIvory
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = bassBoost.toFloat(),
                        onValueChange = { onBassBoostChanged(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = isEqEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberTubeGlow,
                            activeTrackColor = AmberTubeGlow,
                            inactiveTrackColor = Color(0xFF2E2B25),
                            disabledThumbColor = DarkMuted,
                            disabledActiveTrackColor = DarkMuted.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bass_boost_slider")
                    )
                }
            }

            // Virtualizer / Soundstage Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard)
                    .border(1.dp, Color(0xFF332F28), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "3D SOUNDSTAGE",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VintageBrass
                    )
                    Text(
                        text = "${virtualizer / 10}%",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CreamIvory
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = virtualizer.toFloat(),
                        onValueChange = { onVirtualizerChanged(it.toInt()) },
                        valueRange = 0f..1000f,
                        enabled = isEqEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = VintageBrass,
                            activeTrackColor = VintageBrass,
                            inactiveTrackColor = Color(0xFF2E2B25),
                            disabledThumbColor = DarkMuted,
                            disabledActiveTrackColor = DarkMuted.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("virtualizer_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Audiophile Engineering Note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCardElevated)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "AUDIOPHILE ARCHITECTURE SPECIFICATION",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = VintageBrassLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Native 24-bit linear PCM & Free Lossless Audio Codec (FLAC) rendering pipeline. Direct hardware audio session filtering with low-latency DSP biquad filters. Zero cloud resampling.",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 10.sp,
                    color = MutedIvory
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
