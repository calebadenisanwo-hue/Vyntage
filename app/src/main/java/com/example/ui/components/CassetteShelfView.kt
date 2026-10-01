package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrack
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

/**
 * Visual Vintage Cassette Tape Shelf & Rack.
 * Displays local music library as physical cassette cases in a car dashboard tape rack.
 * Tapping any cassette pulls it out and inserts it into the cassette deck!
 */
@Composable
fun CassetteShelfView(
    tracks: List<AudioTrack>,
    currentTrackId: Long?,
    isPlaying: Boolean,
    onSelectTape: (AudioTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    // Array of authentic vintage J-Card cassette spine colors
    val spineColors = listOf(
        Pair(Color(0xFF2C3E50), Color(0xFF1A252F)), // Midnight Blue
        Pair(Color(0xFF78281F), Color(0xFF511B15)), // Burgundy / Maroon
        Pair(Color(0xFF1E8449), Color(0xFF145A32)), // British Racing Green
        Pair(Color(0xFF935116), Color(0xFF6E3C0F)), // Warm Amber Ochre
        Pair(Color(0xFF2E4053), Color(0xFF1C2833)), // Slate Charcoal
        Pair(Color(0xFF5B2C6F), Color(0xFF3E1E4C)), // Deep Plum
        Pair(Color(0xFF17202A), Color(0xFF0E1319))  // Jet Black
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Shelf Header & Car Rack Trim
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DASHBOARD CASSETTE RACK",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VintageBrass,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "TAP CASSETTE TO INSERT INTO PLAYER",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 8.sp,
                    color = MutedIvory,
                    letterSpacing = 0.8.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF181614))
                    .border(1.dp, VintageBrass.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${tracks.size} TAPES",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberTubeGlow
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Physical Wooden / Slotted Cassette Shelf Rack
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF231F1C), // Deep walnut / glovebox black
                            Color(0xFF161412),
                            Color(0xFF0F0E0C)
                        )
                    )
                )
                .border(1.5.dp, Color(0xFF38322B), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 520.dp)
                    .testTag("cassette_shelf_rack"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tracks, key = { _, item -> item.id }) { index, track ->
                    val isCurrent = track.id == currentTrackId
                    val colorPair = spineColors[index % spineColors.size]

                    CassetteSpineItem(
                        index = index + 1,
                        track = track,
                        isCurrent = isCurrent,
                        isPlaying = isPlaying && isCurrent,
                        spineBackground = colorPair.first,
                        spineShadow = colorPair.second,
                        onClick = { onSelectTape(track) }
                    )
                }
            }
        }
    }
}

/**
 * Individual physical cassette case spine sitting on the rack shelf.
 */
@Composable
private fun CassetteSpineItem(
    index: Int,
    track: AudioTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    spineBackground: Color,
    spineShadow: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        spineBackground,
                        spineBackground.copy(alpha = 0.95f),
                        spineShadow
                    )
                )
            )
            .border(
                1.dp,
                if (isCurrent) AmberTubeGlow else Color(0xFF4A443A),
                RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("cassette_item_${track.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Tape Index and Dolby/EQ badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cassette Spine notch / tape index
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF0F0E0C))
                        .border(0.8.dp, Color(0xFF383228), RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playing",
                            tint = AmberTubeGlow,
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Text(
                            text = "%02d".format(index),
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) AmberTubeGlow else CreamIvory
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Spine paper label with Title and Artist
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = track.title,
                            fontFamily = PlayfairFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CreamIvory,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(AmberTubeGlow)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "LOADED",
                                    fontFamily = CourierPrimeFontFamily,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14120C)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${track.artist} · ${track.codec}",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 9.sp,
                        color = MutedIvory.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Type II Chrome label + Duration
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF141210))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "TYPE II [CrO2]",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 7.sp,
                        color = Color(0xFFD6C4A2)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = track.formattedDuration,
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 10.sp,
                    color = CreamIvory
                )
            }
        }
    }
}
