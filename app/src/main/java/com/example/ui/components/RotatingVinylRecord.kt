package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.audio.TurntableSpeed
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.VintageBrass
import com.example.ui.theme.VintageBrassLight
import com.example.ui.theme.VinylBlack
import com.example.ui.theme.VinylPlatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom Compose component that renders a rotating vinyl record animation that syncs
 * with the playback state of the music player.
 *
 * Features:
 * - Rotational velocity driven by turntable speed (33 ⅓, 45, 78 RPM).
 * - Smooth physics-based inertia: spins up when playback starts and decelerates smoothly when paused.
 * - Anisotropic specular light reflections that rotate with the vinyl.
 * - Concentric sound microgrooves.
 * - Center label with vintage gold badge and spindle hole.
 * - Realistic tonearm tracking synchronized with song progress and playback state.
 */
@Composable
fun RotatingVinylRecord(
    isPlaying: Boolean,
    progress: Float,
    speed: TurntableSpeed = TurntableSpeed.RPM_33,
    title: String = "",
    artist: String = "",
    showTonearm: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Determine target rotation period (ms per full 360-degree rotation)
    val rotationPeriodMs = when (speed) {
        TurntableSpeed.RPM_33 -> 1800f // ~33.3 RPM
        TurntableSpeed.RPM_45 -> 1333f // ~45 RPM
        TurntableSpeed.RPM_78 -> 769f  // ~78 RPM
    }

    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var currentSpeedFactor by remember { mutableFloatStateOf(0f) } // 0.0f = stopped, 1.0f = full speed

    // Physics-based inertia loop for smooth start and spin-down deceleration
    LaunchedEffect(isPlaying, rotationPeriodMs) {
        val frameDurationMs = 16L
        val maxSpeedPerFrame = 360f / (rotationPeriodMs / frameDurationMs)

        while (isActive) {
            if (isPlaying) {
                // Smooth acceleration up to full rotational speed
                if (currentSpeedFactor < 1.0f) {
                    currentSpeedFactor = (currentSpeedFactor + 0.04f).coerceAtMost(1.0f)
                }
            } else {
                // Smooth deceleration (turntable platter inertia)
                if (currentSpeedFactor > 0f) {
                    currentSpeedFactor = (currentSpeedFactor - 0.025f).coerceAtLeast(0f)
                }
            }

            if (currentSpeedFactor > 0f) {
                val step = maxSpeedPerFrame * currentSpeedFactor
                rotationDegrees = (rotationDegrees + step) % 360f
            }

            delay(frameDurationMs)
        }
    }

    // Tonearm tracking animation:
    // Parked angle (-18 deg) -> Lead-in groove (2 deg) -> Inner run-out groove (24 deg)
    val targetTonearmAngle = if (isPlaying || currentSpeedFactor > 0.1f) {
        2f + (progress.coerceIn(0f, 1f) * 22f)
    } else {
        -18f // Resting on armrest
    }

    val animatedTonearmAngle = remember { Animatable(-18f) }
    LaunchedEffect(targetTonearmAngle) {
        animatedTonearmAngle.animateTo(
            targetValue = targetTonearmAngle,
            animationSpec = tween(
                durationMillis = if (isPlaying) 1100 else 750,
                easing = FastOutSlowInEasing
            )
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .testTag("rotating_vinyl_record_component")
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        val sizeDp = min(maxWidth.value, maxHeight.value).dp

        Canvas(modifier = Modifier.size(sizeDp)) {
            val width = size.width
            val height = size.height
            val center = if (showTonearm) Offset(width * 0.44f, height * 0.52f) else Offset(width * 0.5f, height * 0.5f)
            val recordRadius = if (showTonearm) width * 0.36f else width * 0.44f

            // 1. Outer Turntable Deck & Platter (Metallic rim and strobe markings)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(VinylPlatter, Color(0xFF100F0E), Color(0xFF0A0908)),
                    center = center,
                    radius = recordRadius * 1.09f
                ),
                radius = recordRadius * 1.09f,
                center = center
            )

            // Platter rim strobe notches
            val numStrobeDots = 48
            for (i in 0 until numStrobeDots) {
                val angleDeg = (i * (360f / numStrobeDots) + rotationDegrees) * (PI / 180f)
                val dotCenter = Offset(
                    (center.x + recordRadius * 1.05f * cos(angleDeg)).toFloat(),
                    (center.y + recordRadius * 1.05f * sin(angleDeg)).toFloat()
                )
                drawCircle(
                    color = Color(0xFFE2D4B7).copy(alpha = if (i % 4 == 0) 0.75f else 0.30f),
                    radius = 1.8f,
                    center = dotCenter
                )
            }

            // Platter brass bevel edge
            drawCircle(
                color = VintageBrass.copy(alpha = 0.45f),
                radius = recordRadius * 1.02f,
                center = center,
                style = Stroke(width = 2.2f)
            )

            // 2. Spinning Vinyl Record Disc
            rotate(degrees = rotationDegrees, pivot = center) {
                // Vinyl black base
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF22201D), VinylBlack, Color(0xFF090807)),
                        center = center,
                        radius = recordRadius
                    ),
                    radius = recordRadius,
                    center = center
                )

                // Concentric Sound Grooves
                val grooveCount = 20
                for (i in 0 until grooveCount) {
                    val r = recordRadius * (0.42f + (i.toFloat() / grooveCount) * 0.55f)
                    val isAccent = i % 4 == 0
                    drawCircle(
                        color = Color.White.copy(alpha = if (isAccent) 0.22f else 0.10f),
                        radius = r,
                        center = center,
                        style = Stroke(width = if (isAccent) 1.2f else 0.8f)
                    )
                }

                // Anisotropic specular reflection sheen
                drawSpecularVinylSheen(center, recordRadius)

                // 3. Vintage Matte Center Label
                val labelRadius = recordRadius * 0.35f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(VintageBrassLight, VintageBrass, Color(0xFF916A16)),
                        center = center,
                        radius = labelRadius
                    ),
                    radius = labelRadius,
                    center = center
                )

                // Center Label Inner Ring & Typography Guide
                drawCircle(
                    color = Color(0xFF5E4510),
                    radius = labelRadius * 0.88f,
                    center = center,
                    style = Stroke(width = 1.4f)
                )

                // Spindle Bushing & Pin Hole
                drawCircle(
                    color = Color(0xFFE6D6AA),
                    radius = labelRadius * 0.22f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF171512),
                    radius = labelRadius * 0.11f,
                    center = center
                )
            }

            // 4. Stylus Tonearm Assembly
            if (showTonearm) {
                val pivotCenter = Offset(width * 0.84f, height * 0.22f)
                drawTonearmAssembly(
                    pivot = pivotCenter,
                    angle = animatedTonearmAngle.value,
                    recordRadius = recordRadius
                )
            }
        }
    }
}

private fun DrawScope.drawSpecularVinylSheen(center: Offset, radius: Float) {
    // Wedge 1
    val path1 = Path().apply {
        moveTo(center.x, center.y)
        val r = radius * 0.98f
        val a1 = -24.0 * (PI / 180.0)
        val a2 = 24.0 * (PI / 180.0)
        lineTo((center.x + r * cos(a1)).toFloat(), (center.y + r * sin(a1)).toFloat())
        lineTo((center.x + r * cos(a2)).toFloat(), (center.y + r * sin(a2)).toFloat())
        close()
    }
    drawPath(
        path1,
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.09f), Color.Transparent),
            center = center,
            radius = radius
        )
    )

    // Wedge 2 (Opposite sheen)
    val path2 = Path().apply {
        moveTo(center.x, center.y)
        val r = radius * 0.98f
        val a1 = 156.0 * (PI / 180.0)
        val a2 = 204.0 * (PI / 180.0)
        lineTo((center.x + r * cos(a1)).toFloat(), (center.y + r * sin(a1)).toFloat())
        lineTo((center.x + r * cos(a2)).toFloat(), (center.y + r * sin(a2)).toFloat())
        close()
    }
    drawPath(
        path2,
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.09f), Color.Transparent),
            center = center,
            radius = radius
        )
    )
}

private fun DrawScope.drawTonearmAssembly(
    pivot: Offset,
    angle: Float,
    recordRadius: Float
) {
    // Pivot gimbal base
    drawCircle(
        color = Color(0x44000000),
        radius = 28f,
        center = pivot + Offset(4f, 6f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF5A544D), Color(0xFF262320)),
            center = pivot,
            radius = 26f
        ),
        radius = 26f,
        center = pivot
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(VintageBrassLight, VintageBrass),
            center = pivot,
            radius = 16f
        ),
        radius = 16f,
        center = pivot
    )

    // Rear counterweight
    val counterAngleRad = (angle + 180f) * (PI / 180f)
    val counterPos = Offset(
        (pivot.x + 36f * cos(counterAngleRad)).toFloat(),
        (pivot.y + 36f * sin(counterAngleRad)).toFloat()
    )
    drawCircle(
        color = Color(0xFF38342E),
        radius = 13f,
        center = counterPos
    )
    drawCircle(
        color = VintageBrass,
        radius = 6f,
        center = counterPos
    )

    // Resting clip post
    val armrestPos = Offset(pivot.x - 30f, pivot.y + 70f)
    drawCircle(
        color = Color(0xFF38342E),
        radius = 7f,
        center = armrestPos
    )

    // S-curved tonearm wand & stylus headshell
    rotate(degrees = angle, pivot = pivot) {
        val wandLength = recordRadius * 1.55f
        val start = pivot
        val p1 = Offset(pivot.x - wandLength * 0.45f, pivot.y + wandLength * 0.45f)
        val p2 = Offset(pivot.x - wandLength * 0.70f, pivot.y + wandLength * 0.85f)
        val headshellPos = Offset(pivot.x - wandLength * 0.82f, pivot.y + wandLength * 1.08f)

        // Drop shadow
        val shadowPath = Path().apply {
            moveTo(start.x + 4f, start.y + 6f)
            cubicTo(
                p1.x + 4f, p1.y + 6f,
                p2.x + 4f, p2.y + 6f,
                headshellPos.x + 4f, headshellPos.y + 6f
            )
        }
        drawPath(
            shadowPath,
            color = Color(0x33000000),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        // Metallic Wand
        val wandPath = Path().apply {
            moveTo(start.x, start.y)
            cubicTo(
                p1.x, p1.y,
                p2.x, p2.y,
                headshellPos.x, headshellPos.y
            )
        }
        drawPath(
            wandPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFE8DCC0), VintageBrassLight, Color(0xFFA8956F)),
                start = start,
                end = headshellPos
            ),
            style = Stroke(width = 4.2f, cap = StrokeCap.Round)
        )

        // Headshell Cartridge
        val hsWidth = 22f
        val hsHeight = 36f
        val hsCenter = headshellPos

        drawRoundRect(
            color = Color(0xFF1E1C1A),
            topLeft = Offset(hsCenter.x - hsWidth / 2f, hsCenter.y),
            size = Size(hsWidth, hsHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Gold stripe
        drawLine(
            color = VintageBrass,
            start = Offset(hsCenter.x - hsWidth / 2f, hsCenter.y + hsHeight * 0.6f),
            end = Offset(hsCenter.x + hsWidth / 2f, hsCenter.y + hsHeight * 0.6f),
            strokeWidth = 2.5f
        )
        // Diamond stylus needle tip
        drawCircle(
            color = Color.White,
            radius = 2.2f,
            center = Offset(hsCenter.x, hsCenter.y + hsHeight)
        )
    }
}
