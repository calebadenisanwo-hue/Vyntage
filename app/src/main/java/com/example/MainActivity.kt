package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.DeckTheme
import com.example.audio.PlayerState
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AmberTubeGlow
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.CourierPrimeFontFamily
import com.example.ui.theme.CreamIvory
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.VintageBrass
import com.example.ui.theme.VintageBrassLight
import com.example.ui.theme.VinylBlack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VintageApp()
            }
        }
    }
}

@Composable
fun VintageApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isScanning by viewModel.isLibraryScanning.collectAsStateWithLifecycle()
    val notificationMessage by viewModel.notificationMessage.collectAsStateWithLifecycle()
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotification()
        }
    }

    // Permission launcher for device audio storage
    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanDeviceLibrary()
        } else {
            viewModel.showNotification("Storage permission needed to scan local files")
        }
    }

    fun requestStorageAndScan() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            permissionToRequest
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.scanDeviceLibrary()
        } else {
            permissionLauncher.launch(permissionToRequest)
        }
    }

    // BackHandler: Return to Turntable tab if pressed while on secondary tabs
    if (selectedTab != AppTab.TURNTABLE) {
        BackHandler {
            viewModel.selectTab(AppTab.TURNTABLE)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(VinylBlack),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Docked Mini-Player Bar when browsing other screens
                if (selectedTab != AppTab.TURNTABLE && playerState.currentTrack != null) {
                    MiniPlayerBar(
                        playerState = playerState,
                        onBarClick = { viewModel.selectTab(AppTab.TURNTABLE) },
                        onPlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.nextTrack() }
                    )
                }

                // Vintage Bottom Navigation Bar
                VintageNavigationBar(
                    selectedTab = selectedTab,
                    deckTheme = playerState.deckTheme,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VinylBlack)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp) // Maintain tight, focused Hi-Fi unit on tablets/foldables
            ) {
                when (selectedTab) {
                    AppTab.TURNTABLE -> NowPlayingScreen(
                        playerState = playerState,
                        sleepTimerRemaining = sleepTimerRemaining,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onSeek = { viewModel.seekTo(it) },
                        onNext = { viewModel.nextTrack() },
                        onPrevious = { viewModel.previousTrack() },
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onCycleRepeat = { viewModel.cycleRepeatMode() },
                        onSetSpeed = { viewModel.setSpeed(it) },
                        onSetPitchFine = { viewModel.setPitchFine(it) },
                        onToggleCrackle = { viewModel.toggleVinylCrackle(it) },
                        onSetCrackleVolume = { viewModel.setVinylCrackleVolume(it) },
                        onOpenQueue = { viewModel.selectTab(AppTab.LIBRARY) },
                        onOpenEqualizer = { viewModel.selectTab(AppTab.EQUALIZER) },
                        onSelectQueueTrack = { viewModel.playTrack(it) },
                        onSetDeckTheme = { viewModel.setDeckTheme(it) },
                        onToggleTapeEject = { viewModel.toggleTapeEject() },
                        onSelectCassetteTape = { viewModel.playTrackFromCassette(it) },
                        allTracks = allTracks
                    )

                    AppTab.LIBRARY -> LibraryScreen(
                        allTracks = allTracks,
                        favoriteTracks = favoriteTracks,
                        currentTrackId = playerState.currentTrack?.id,
                        isPlaying = playerState.isPlaying,
                        searchQuery = searchQuery,
                        isScanning = isScanning,
                        onSearchChanged = { viewModel.updateSearchQuery(it) },
                        onTrackSelected = { track, list ->
                            viewModel.playTrack(track, list)
                            viewModel.selectTab(AppTab.TURNTABLE)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onScanStorage = { requestStorageAndScan() },
                        onLoadSampler = { viewModel.loadVintageSampler() },
                        onImportUris = { viewModel.importAudioUris(it) },
                        onDeleteTrack = { viewModel.deleteTrack(it) }
                    )

                    AppTab.EQUALIZER -> EqualizerScreen(
                        isEqEnabled = playerState.isEqEnabled,
                        eqBands = playerState.eqBands,
                        selectedPreset = playerState.selectedPresetName,
                        availablePresets = viewModel.playbackEngine.availablePresets,
                        bassBoost = playerState.bassBoostStrength,
                        virtualizer = playerState.virtualizerStrength,
                        onToggleEq = { viewModel.toggleEq(it) },
                        onResetFlat = { viewModel.resetEqToFlat() },
                        onPresetSelected = { viewModel.applyPreset(it) },
                        onBandGainChanged = { band, db -> viewModel.setBandGain(band, db) },
                        onBassBoostChanged = { viewModel.setBassBoost(it) },
                        onVirtualizerChanged = { viewModel.setVirtualizer(it) }
                    )

                    AppTab.SETTINGS -> SettingsScreen(
                        isCrackleEnabled = playerState.isVinylCrackleEnabled,
                        crackleVolume = playerState.vinylCrackleVolume,
                        isHapticsEnabled = playerState.isTactileHapticsEnabled,
                        isNeedleSoundEnabled = playerState.isNeedleSoundEnabled,
                        sleepTimerRemaining = sleepTimerRemaining,
                        onToggleCrackle = { viewModel.toggleVinylCrackle(it) },
                        onSetCrackleVolume = { viewModel.setVinylCrackleVolume(it) },
                        onToggleHaptics = { viewModel.toggleTactileHaptics(it) },
                        onToggleNeedleSound = { viewModel.toggleNeedleSound(it) },
                        onStartSleepTimer = { viewModel.startSleepTimer(it) },
                        onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                        deckTheme = playerState.deckTheme,
                        onSetDeckTheme = { viewModel.setDeckTheme(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    playerState: PlayerState,
    onBarClick: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    val track = playerState.currentTrack ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBarClick() }
            .background(SurfaceCardElevated)
            .border(1.dp, VintageBrass.copy(alpha = 0.35f))
            .testTag("mini_player_bar")
    ) {
        // Subtle progress needle line
        LinearProgressIndicator(
            progress = { playerState.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = VintageBrass,
            trackColor = Color(0xFF2E2B25),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spinning disk icon
            Icon(
                imageVector = Icons.Default.Album,
                contentDescription = null,
                tint = if (playerState.isPlaying) AmberTubeGlow else DarkMuted,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Text (NO ALBUM ART!)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontFamily = PlayfairFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CreamIvory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} · ${track.codec}",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 10.sp,
                    color = VintageBrass,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("mini_play_pause")
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = VintageBrassLight,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("mini_next")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = CreamIvory,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun VintageNavigationBar(
    selectedTab: AppTab,
    deckTheme: DeckTheme,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = ChassisDark,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar")
    ) {
        val playerLabel = if (deckTheme == DeckTheme.CAR_CASSETTE) "Cassette" else "Turntable"
        val playerIcon = if (deckTheme == DeckTheme.CAR_CASSETTE) Icons.Default.Radio else Icons.Default.Album

        val items = listOf(
            Triple(AppTab.TURNTABLE, playerLabel, playerIcon),
            Triple(AppTab.LIBRARY, "Library", Icons.Default.LibraryMusic),
            Triple(AppTab.EQUALIZER, "Hi-Fi EQ", Icons.Default.GraphicEq),
            Triple(AppTab.SETTINGS, "Settings", Icons.Default.Settings)
        )

        items.forEach { (tab, label, icon) ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF14120C),
                    selectedTextColor = VintageBrassLight,
                    indicatorColor = VintageBrass,
                    unselectedIconColor = DarkMuted,
                    unselectedTextColor = DarkMuted
                ),
                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
            )
        }
    }
}
