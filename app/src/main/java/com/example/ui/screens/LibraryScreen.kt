package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioTrack
import com.example.ui.components.CassetteShelfView
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

enum class LibraryViewMode(val label: String) {
    TRACK_LIST("Track List"),
    CASSETTE_SHELF("Tape Shelf")
}

enum class LibrarySortOrder(val label: String) {
    DEFAULT("Default"),
    TITLE("Title"),
    ARTIST("Artist"),
    DURATION("Duration")
}

@Composable
fun LibraryScreen(
    allTracks: List<AudioTrack>,
    favoriteTracks: List<AudioTrack>,
    currentTrackId: Long?,
    isPlaying: Boolean,
    searchQuery: String,
    isScanning: Boolean,
    onSearchChanged: (String) -> Unit,
    onTrackSelected: (AudioTrack, List<AudioTrack>) -> Unit,
    onToggleFavorite: (AudioTrack) -> Unit,
    onScanStorage: () -> Unit,
    onLoadSampler: () -> Unit,
    onImportUris: (List<Uri>) -> Unit,
    onDeleteTrack: (AudioTrack) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0: All, 1: Favorites
    var sortOrder by remember { mutableStateOf(LibrarySortOrder.DEFAULT) }
    var viewMode by remember { mutableStateOf(LibraryViewMode.TRACK_LIST) }

    val rawTracks: List<AudioTrack> = if (selectedFilterIndex == 1) {
        if (searchQuery.isBlank()) favoriteTracks else {
            val q = searchQuery.lowercase()
            favoriteTracks.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) }
        }
    } else {
        allTracks
    }

    val displayTracks: List<AudioTrack> = remember(rawTracks, sortOrder) {
        when (sortOrder) {
            LibrarySortOrder.DEFAULT -> rawTracks
            LibrarySortOrder.TITLE -> rawTracks.sortedBy { it.title.lowercase() }
            LibrarySortOrder.ARTIST -> rawTracks.sortedBy { it.artist.lowercase() }
            LibrarySortOrder.DURATION -> rawTracks.sortedByDescending { it.durationMs }
        }
    }

    val totalDurationMs = remember(displayTracks) {
        displayTracks.sumOf { it.durationMs }
    }

    // Document / Audio picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportUris(uris)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VinylBlack)
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
                    text = "LOCAL LIBRARY",
                    fontFamily = PlayfairFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = VintageBrass,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "NO STREAMING · STRICTLY HIGH-FIDELITY LOCAL AUDIO",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 8.sp,
                    color = MutedIvory,
                    letterSpacing = 0.8.sp
                )
            }

            if (isScanning) {
                CircularProgressIndicator(
                    color = VintageBrass,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            placeholder = {
                Text(
                    text = "Search by song or artist...",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 12.sp,
                    color = DarkMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = VintageBrass,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MutedIvory,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VintageBrass,
                unfocusedBorderColor = Color(0xFF2E2B26),
                focusedTextColor = CreamIvory,
                unfocusedTextColor = CreamIvory,
                cursorColor = VintageBrass
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Actions Bar: Scan Device, Import Files, Load Audiophile Sampler
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCard)
                    .border(1.dp, Color(0xFF332F28), RoundedCornerShape(6.dp))
                    .clickable { onScanStorage() }
                    .padding(vertical = 7.dp, horizontal = 6.dp)
                    .testTag("scan_storage_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan",
                        tint = VintageBrass,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Scan Device",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        color = CreamIvory
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCard)
                    .border(1.dp, Color(0xFF332F28), RoundedCornerShape(6.dp))
                    .clickable {
                        filePickerLauncher.launch(
                            arrayOf("audio/*", "application/ogg", "application/x-flac")
                        )
                    }
                    .padding(vertical = 7.dp, horizontal = 6.dp)
                    .testTag("import_files_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Import",
                        tint = VintageBrass,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Import Files",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        color = CreamIvory
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCard)
                    .border(1.dp, Color(0xFF332F28), RoundedCornerShape(6.dp))
                    .clickable { onLoadSampler() }
                    .padding(vertical = 7.dp, horizontal = 6.dp)
                    .testTag("load_sampler_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = "Sampler",
                        tint = AmberTubeGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sampler",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        color = AmberTubeGlow
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills and View Mode Switcher: "All Tracks" | "Favorites" & "List" | "📼 Tape Shelf"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filters
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("All (${allTracks.size})", "Favorites (${favoriteTracks.size})").forEachIndexed { idx, title ->
                    val isSelected = selectedFilterIndex == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) VintageBrass else SurfaceCard)
                            .clickable { selectedFilterIndex = idx }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("filter_tab_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF14120C) else MutedIvory
                        )
                    }
                }
            }

            // View Mode Switcher
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCard)
                    .border(1.dp, Color(0xFF2E2B25), RoundedCornerShape(6.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                LibraryViewMode.values().forEach { mode ->
                    val isCurrentMode = viewMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCurrentMode) AmberTubeGlow else Color.Transparent)
                            .clickable { viewMode = mode }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("view_mode_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (mode == LibraryViewMode.TRACK_LIST) "☰ List" else "📼 Tape Shelf",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 9.sp,
                            fontWeight = if (isCurrentMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrentMode) Color(0xFF14120C) else CreamIvory
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Collection Stats & Sort Order Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceCard)
                .border(1.dp, Color(0xFF2E2B25), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${displayTracks.size} RECORDINGS · ${AudioTrack.formatDuration(totalDurationMs)} TOTAL",
                fontFamily = CourierPrimeFontFamily,
                fontSize = 9.sp,
                color = VintageBrassLight,
                letterSpacing = 0.8.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        sortOrder = when (sortOrder) {
                            LibrarySortOrder.DEFAULT -> LibrarySortOrder.TITLE
                            LibrarySortOrder.TITLE -> LibrarySortOrder.ARTIST
                            LibrarySortOrder.ARTIST -> LibrarySortOrder.DURATION
                            LibrarySortOrder.DURATION -> LibrarySortOrder.DEFAULT
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("library_sort_order_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort Order",
                    tint = AmberTubeGlow,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SORT: ${sortOrder.label.uppercase()}",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberTubeGlow
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Display: Either Cassette Shelf Rack OR Pristine Track List
        if (displayTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = DarkMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedFilterIndex == 1) "No Favorite Tracks Yet" else "No Local Music Found",
                        fontFamily = PlayfairFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CreamIvory
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap 'Scan Device' to index device audio, 'Import Files' to pick audio files, or 'Sampler' to load lossless acoustic masters.",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 11.sp,
                        color = MutedIvory,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else if (viewMode == LibraryViewMode.CASSETTE_SHELF) {
            // Authentic Retro Cassette Tape Rack Shelf
            CassetteShelfView(
                tracks = displayTracks,
                currentTrackId = currentTrackId,
                isPlaying = isPlaying,
                onSelectTape = { onTrackSelected(it, displayTracks) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("tracks_lazy_column"),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(displayTracks, key = { _, track -> track.id }) { index, track ->
                    val isCurrent = track.id == currentTrackId

                    TrackListItem(
                        index = index + 1,
                        track = track,
                        isCurrent = isCurrent,
                        isPlaying = isPlaying && isCurrent,
                        onClick = { onTrackSelected(track, displayTracks) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onDelete = { onDeleteTrack(track) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackListItem(
    index: Int,
    track: AudioTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrent) SurfaceCardElevated else SurfaceCard)
            .border(
                1.dp,
                if (isCurrent) VintageBrass.copy(alpha = 0.6f) else Color(0xFF282520),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("track_item_${track.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track number or playing indicator
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Playing",
                        tint = AmberTubeGlow,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "%02d".format(index),
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 11.sp,
                        color = if (isCurrent) VintageBrass else DarkMuted
                    )
                }
            }

            // Track Title and Artist (NO ALBUM ART!)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontFamily = PlayfairFontFamily,
                    fontSize = 15.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) VintageBrassLight else CreamIvory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 11.sp,
                        color = MutedIvory,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Codec chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF191715))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = track.codec.substringBefore(' '),
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 8.sp,
                            color = VintageBrass.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Duration
            Text(
                text = track.formattedDuration,
                fontFamily = CourierPrimeFontFamily,
                fontSize = 11.sp,
                color = MutedIvory
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("fav_button_${track.id}")
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (track.isFavorite) AmberTubeGlow else DarkMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (!track.isSample) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_button_${track.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove from Library",
                        tint = DarkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
