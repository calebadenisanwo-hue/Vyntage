package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eject
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import com.example.ui.theme.VinylBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Authentic 80s/90s Automotive Dashboard Cassette Player Deck.
 * Features:
 * - Smoked acrylic tape door with motorized ejection animation.
 * - Realistic rotating dual cassette spools (reels) with winding magnetic tape ribbon based on playback progress.
 * - Cassette label with track title, artist, and type II chrome branding.
 * - Backlit digital/mechanical tape counter display and Dolby NR status.
 * - Heavy automotive physical push buttons right on the deck faceplate (REW, PLAY, PAUSE, FF, EJECT)
 *   with mechanical depression animation, LED transport lights, and tactile sound.
 */
@Composable
fun CarCassetteDeck(
    track: AudioTrack?,
    isPlaying: Boolean,
    progress: Float,
    isTapeInserted: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onEjectToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Rotation degrees of tape hubs
    var spoolRotationDegrees by remember { mutableFloatStateOf(0f) }

    // Animate rotation when playing and tape is inside
    LaunchedEffect(isPlaying, isTapeInserted) {
        if (isPlaying && isTapeInserted) {
            val stepTime = 16L
            while (isActive) {
                spoolRotationDegrees = (spoolRotationDegrees + 2.8f) % 360f
                delay(stepTime)
            }
        }
    }

    // Tape insertion vertical slide offset (0 = fully inserted, -60 = ejected out of slot)
    val ejectAnim = remember { Animatable(if (isTapeInserted) 0f else -55f) }
    LaunchedEffect(isTapeInserted) {
        ejectAnim.animateTo(
            targetValue = if (isTapeInserted) 0f else -55f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    // Outer Car Dashboard Deck Console
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF282729), // Dashboard brushed graphite
                        Color(0xFF1B1A1C),
                        Color(0xFF131314)
                    )
                )
            )
            .border(2.dp, Color(0xFF38363A), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("car_cassette_deck")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Deck Header: Brand, Auto-Reverse Badge, and Backlit Tape Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BLAUPUNKT · HI-FI CASSETTE",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC7B695),
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "AUTO REVERSE · DOLBY B-C NR",
                        fontFamily = CourierPrimeFontFamily,
                        fontSize = 8.sp,
                        color = Color(0xFF88847F),
                        letterSpacing = 0.8.sp
                    )
                }

                // Backlit Tape Counter & Tape Direction LED
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Direction arrow LED
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPlaying && isTapeInserted) AmberTubeGlow.copy(alpha = 0.25f) else Color(0xFF161514))
                            .border(1.dp, if (isPlaying && isTapeInserted) AmberTubeGlow else Color(0xFF2C2A26), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "▶ TAPE",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying && isTapeInserted) AmberTubeGlow else DarkMuted
                        )
                    }

                    // Mechanical/VFD Counter numbers (e.g. "042")
                    val counterNumber = (progress * 199).toInt()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF0F0E0C))
                            .border(1.dp, Color(0xFF282520), RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "%03d".format(counterNumber),
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTapeInserted) AmberTubeGlow else Color(0xFF4A3816)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Smoked Acrylic Cassette Door & Tape Mechanism Window
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0C0B0A))
                    .border(1.5.dp, Color(0xFF2E2C29), RoundedCornerShape(8.dp))
                    .clickable { onEjectToggle() }
                    .testTag("cassette_door_window"),
                contentAlignment = Alignment.Center
            ) {
                // Background tape transport guides and pinch rollers
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawTapeWellBackground()
                }

                // The Cassette Tape (Animated insertion/ejection offset)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(x = 0, y = ejectAnim.value.dp.roundToPx()) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCassetteTape(
                            progress = progress,
                            rotationDegrees = spoolRotationDegrees,
                            isInserted = isTapeInserted
                        )
                    }

                    // Cassette Paper Label (Title, Artist, and Vintage Chrome Stamp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .align(Alignment.Center)
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Handwritten style J-Card Title
                        Text(
                            text = track?.title ?: "Vintage Acoustic Tape",
                            fontFamily = PlayfairFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1A16),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = track?.artist ?: "Authentic Tape Session",
                            fontFamily = CourierPrimeFontFamily,
                            fontSize = 9.sp,
                            color = Color(0xFF4E453A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Eject badge overlay when tape is pushed out
                    if (!isTapeInserted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AmberTubeGlow)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TAPE EJECTED · TAP TO INSERT",
                                fontFamily = CourierPrimeFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14120C)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Tactile Mechanical Car Stereo Push Buttons
            // Authentic heavy dashboard push-keys: REW, PLAY, PAUSE, FF, EJECT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF141315))
                    .border(1.dp, Color(0xFF262529), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // REW Key
                DeckMechanicalButton(
                    label = "REW",
                    icon = Icons.Default.FastRewind,
                    isPressedDown = false,
                    onClick = onPrevious,
                    tag = "deck_button_rew"
                )

                // PLAY Key (physically depresses when playing)
                DeckMechanicalButton(
                    label = "PLAY",
                    icon = Icons.Default.PlayArrow,
                    isPressedDown = isPlaying && isTapeInserted,
                    hasLed = true,
                    onClick = {
                        if (!isTapeInserted) onEjectToggle()
                        onPlay()
                    },
                    tag = "deck_button_play"
                )

                // PAUSE Key
                DeckMechanicalButton(
                    label = "STOP/PAUSE",
                    icon = Icons.Default.Pause,
                    isPressedDown = !isPlaying && isTapeInserted,
                    onClick = onPause,
                    tag = "deck_button_pause"
                )

                // FF Key
                DeckMechanicalButton(
                    label = "FF",
                    icon = Icons.Default.FastForward,
                    isPressedDown = false,
                    onClick = onNext,
                    tag = "deck_button_ff"
                )

                // EJECT Key
                DeckMechanicalButton(
                    label = "EJECT",
                    icon = Icons.Default.Eject,
                    isPressedDown = !isTapeInserted,
                    hasLed = !isTapeInserted,
                    onClick = onEjectToggle,
                    tag = "deck_button_eject"
                )
            }
        }
    }
}

/**
 * Individual physical rectangular mechanical push button on the car stereo faceplate.
 */
@Composable
private fun DeckMechanicalButton(
    label: String,
    icon: ImageVector,
    isPressedDown: Boolean,
    hasLed: Boolean = false,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 2.dp)
            .testTag(tag)
    ) {
        // Small indicator LED above key (green/amber when active)
        if (hasLed) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isPressedDown) AmberTubeGlow else Color(0xFF332A1C))
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Physical beveled button block
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(34.dp)
                .offset(y = if (isPressedDown) 2.dp else 0.dp) // Depresses physically
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isPressedDown) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E1D1F), Color(0xFF141315))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF3E3D40), Color(0xFF262528))
                        )
                    }
                )
                .border(
                    1.dp,
                    if (isPressedDown) VintageBrass.copy(alpha = 0.5f) else Color(0xFF555358),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isPressedDown) AmberTubeGlow else CreamIvory,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontFamily = CourierPrimeFontFamily,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressedDown) AmberTubeGlow else MutedIvory.copy(alpha = 0.7f)
        )
    }
}

/**
 * Draws the interior of the tape compartment (drive capstans, pinch rollers, tape head).
 */
private fun DrawScope.drawTapeWellBackground() {
    val w = size.width
    val h = size.height

    // Dual motor drive spindle holes
    val leftSpindle = Offset(w * 0.32f, h * 0.50f)
    val rightSpindle = Offset(w * 0.68f, h * 0.50f)

    drawCircle(color = Color(0xFF1A1816), radius = 32f, center = leftSpindle)
    drawCircle(color = Color(0xFF1A1816), radius = 32f, center = rightSpindle)

    // Center tape read head & pinch rollers at the bottom
    val headCenter = Offset(w * 0.50f, h * 0.88f)
    drawRoundRect(
        color = Color(0xFF33302B),
        topLeft = Offset(headCenter.x - 30f, headCenter.y - 12f),
        size = Size(60f, 24f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawCircle(color = Color(0xFFE8D4A2), radius = 5f, center = headCenter) // Ferrite head core
}

/**
 * Draws the Cassette Tape body, magnetic tape packs, rotating gears, and center sticker.
 */
private fun DrawScope.drawCassetteTape(
    progress: Float,
    rotationDegrees: Float,
    isInserted: Boolean
) {
    val w = size.width
    val h = size.height

    // 1. Cassette Outer Shell (Smoked transparent acrylic body with beveled corners)
    val shellRect = Size(w, h)
    drawRoundRect(
        color = Color(0xFF22201E),
        topLeft = Offset(0f, 0f),
        size = shellRect,
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = Color(0xFF4A4640),
        topLeft = Offset(0f, 0f),
        size = shellRect,
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 2.2f)
    )

    // Cassette corner assembly screws
    val screwColor = Color(0xFF7A756D)
    drawCircle(screwColor, radius = 2.8f, center = Offset(12f, 12f))
    drawCircle(screwColor, radius = 2.8f, center = Offset(w - 12f, 12f))
    drawCircle(screwColor, radius = 2.8f, center = Offset(12f, h - 12f))
    drawCircle(screwColor, radius = 2.8f, center = Offset(w - 12f, h - 12f))
    drawCircle(screwColor, radius = 2.8f, center = Offset(w * 0.5f, 12f))

    // 2. Vintage Paper Sticker Label (Cream Ivory / Gold)
    val labelWidth = w * 0.84f
    val labelHeight = h * 0.68f
    val labelTopLeft = Offset((w - labelWidth) / 2f, h * 0.12f)

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFEFE8D8), Color(0xFFDDD2BA))
        ),
        topLeft = labelTopLeft,
        size = Size(labelWidth, labelHeight),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Label Red/Blue Retro Stripe
    drawLine(
        color = Color(0xFFC0392B), // Classic cassette red stripe
        start = Offset(labelTopLeft.x + 8f, labelTopLeft.y + 16f),
        end = Offset(labelTopLeft.x + labelWidth - 8f, labelTopLeft.y + 16f),
        strokeWidth = 2f
    )
    drawLine(
        color = Color(0xFF2980B9), // Blue stripe
        start = Offset(labelTopLeft.x + 8f, labelTopLeft.y + 20f),
        end = Offset(labelTopLeft.x + labelWidth - 8f, labelTopLeft.y + 20f),
        strokeWidth = 1.2f
    )

    // "A" / "Side 1" stamp
    drawCircle(
        color = Color(0xFF1E1A16),
        radius = 7f,
        center = Offset(labelTopLeft.x + 16f, labelTopLeft.y + 36f)
    )

    // 3. Clear Center Viewing Window
    val windowWidth = w * 0.56f
    val windowHeight = h * 0.38f
    val windowCenter = Offset(w * 0.50f, h * 0.52f)
    val windowTopLeft = Offset(windowCenter.x - windowWidth / 2f, windowCenter.y - windowHeight / 2f)

    drawRoundRect(
        color = Color(0xFF100F0E),
        topLeft = windowTopLeft,
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color(0xFF5A544C),
        topLeft = windowTopLeft,
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.4f)
    )

    // 4. Magnetic Tape Ribbon and Dual Rotating Spools
    val leftSpoolCenter = Offset(w * 0.35f, h * 0.52f)
    val rightSpoolCenter = Offset(w * 0.65f, h * 0.52f)

    // Dynamic Tape Pack Radii:
    // Left tape starts full (~30f) and winds down to min (~15f)
    // Right tape starts empty (~15f) and winds up to max (~30f)
    val clampedProgress = progress.coerceIn(0f, 1f)
    val leftTapeRadius = 15f + (1.0f - clampedProgress) * 16f
    val rightTapeRadius = 15f + clampedProgress * 16f
    val tapeBrown = Color(0xFF382314) // Dark magnetic ferric oxide brown

    // Left magnetic tape pack
    drawCircle(color = tapeBrown, radius = leftTapeRadius, center = leftSpoolCenter)
    // Right magnetic tape pack
    drawCircle(color = tapeBrown, radius = rightTapeRadius, center = rightSpoolCenter)

    // Tape ribbon bridge between spools
    drawLine(
        color = tapeBrown,
        start = Offset(leftSpoolCenter.x, leftSpoolCenter.y + leftTapeRadius * 0.9f),
        end = Offset(rightSpoolCenter.x, rightSpoolCenter.y + rightTapeRadius * 0.9f),
        strokeWidth = 3f
    )

    // Left 6-Tooth White Gear Hub (Spins!)
    rotate(degrees = rotationDegrees, pivot = leftSpoolCenter) {
        drawSpoolHub(leftSpoolCenter)
    }

    // Right 6-Tooth White Gear Hub (Spins!)
    rotate(degrees = rotationDegrees, pivot = rightSpoolCenter) {
        drawSpoolHub(rightSpoolCenter)
    }

    // 5. Tape Guide Rollers at bottom corners
    val rollerLeft = Offset(w * 0.16f, h * 0.82f)
    val rollerRight = Offset(w * 0.84f, h * 0.82f)
    drawCircle(color = Color(0xFFC7BBAA), radius = 5.5f, center = rollerLeft)
    drawCircle(color = Color(0xFF282522), radius = 2.5f, center = rollerLeft)
    drawCircle(color = Color(0xFFC7BBAA), radius = 5.5f, center = rollerRight)
    drawCircle(color = Color(0xFF282522), radius = 2.5f, center = rollerRight)
}

/**
 * Draws the white 6-tooth plastic tape hub found in standard C-60 / C-90 audio cassettes.
 */
private fun DrawScope.drawSpoolHub(center: Offset) {
    // White hub circle
    drawCircle(color = Color(0xFFF3ECE0), radius = 13f, center = center)
    // Inner spindle center hole
    drawCircle(color = Color(0xFF141312), radius = 6.5f, center = center)

    // 6 gear drive teeth
    for (i in 0 until 6) {
        val angleRad = (i * (360f / 6f)) * (PI / 180f)
        val toothInner = Offset(
            (center.x + 6.5f * cos(angleRad)).toFloat(),
            (center.y + 6.5f * sin(angleRad)).toFloat()
        )
        val toothOuter = Offset(
            (center.x + 11.5f * cos(angleRad)).toFloat(),
            (center.y + 11.5f * sin(angleRad)).toFloat()
        )
        drawLine(
            color = Color(0xFF2B2824),
            start = toothInner,
            end = toothOuter,
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }
}
