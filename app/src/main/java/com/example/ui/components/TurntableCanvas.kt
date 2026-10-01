package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.example.data.model.AudioTrack
import com.example.ui.theme.AmberTubeGlow
import com.example.ui.theme.ChassisDark
import com.example.ui.theme.CreamIvory
import com.example.ui.theme.DarkMuted
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.VintageBrass
import com.example.ui.theme.VintageBrassLight
import com.example.ui.theme.VinylBlack
import com.example.ui.theme.VinylPlatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun TurntableCanvas(
    isPlaying: Boolean,
    progress: Float,
    speed: TurntableSpeed,
    track: AudioTrack?,
    modifier: Modifier = Modifier
) {
    // Rotation duration depends on selected RPM speed
    val targetRotationDurationMs = when (speed) {
        TurntableSpeed.RPM_33 -> 1800
        TurntableSpeed.RPM_45 -> 1333
        TurntableSpeed.RPM_78 -> 769
    }

    // Dynamic rotation angle with inertia
    var currentRotationAngle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying, targetRotationDurationMs) {
        if (isPlaying) {
            val stepTime = 16L
            while (true) {
                // Degrees per frame
                val degreesPerFrame = 360f / (targetRotationDurationMs / stepTime.toFloat())
                currentRotationAngle = (currentRotationAngle + degreesPerFrame) % 360f
                kotlinx.coroutines.delay(stepTime)
            }
        }
    }

    // Tonearm angle animation:
    // When stopped/paused: tonearm is at resting rest post (angle = -18f)
    // When playing: tonearm enters outer groove (angle = 2f) and sweeps to inner runout groove (angle = 24f) based on track progress
    val targetTonearmAngle = if (isPlaying) {
        2f + (progress.coerceIn(0f, 1f) * 22f)
    } else {
        -18f // Parked on armrest
    }

    val animatedTonearmAngle = remember { Animatable(-18f) }
    LaunchedEffect(targetTonearmAngle) {
        animatedTonearmAngle.animateTo(
            targetValue = targetTonearmAngle,
            animationSpec = tween(
                durationMillis = if (isPlaying) 1200 else 800,
                easing = FastOutSlowInEasing
            )
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .testTag("turntable_visualizer")
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableSize = min(maxWidth.value, maxHeight.value).dp

        Canvas(modifier = Modifier.size(availableSize)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth * 0.44f, canvasHeight * 0.52f)
            val recordRadius = canvasWidth * 0.36f

            // 1. Turntable Chassis Base Plate (Subtle brushed dark slate with rounded corners)
            drawRoundRect(
                color = ChassisDark,
                size = Size(canvasWidth, canvasHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = DarkMuted.copy(alpha = 0.4f),
                size = Size(canvasWidth, canvasHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                style = Stroke(width = 2f)
            )

            // 2. Turntable Platter Outer Ring & Strobe Rim
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(VinylPlatter, Color(0xFF0F0E0D)),
                    center = center,
                    radius = recordRadius * 1.08f
                ),
                radius = recordRadius * 1.08f,
                center = center
            )

            // Strobe rim dots (reflecting light)
            drawStrobeDots(center, recordRadius * 1.05f, currentRotationAngle)

            // Platter beveled edge
            drawCircle(
                color = VintageBrass.copy(alpha = 0.35f),
                radius = recordRadius * 1.02f,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // 3. Spinning Vinyl Record
            rotate(degrees = currentRotationAngle, pivot = center) {
                // Vinyl body
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E1C1A), VinylBlack, Color(0xFF0A0908)),
                        center = center,
                        radius = recordRadius
                    ),
                    radius = recordRadius,
                    center = center
                )

                // Vinyl microgrooves (concentric subtle grooves)
                val numGrooves = 18
                for (i in 0 until numGrooves) {
                    val r = recordRadius * (0.42f + (i.toFloat() / numGrooves) * 0.54f)
                    val grooveAlpha = if (i % 3 == 0) 0.28f else 0.14f
                    drawCircle(
                        color = Color.White.copy(alpha = grooveAlpha),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.0f)
                    )
                }

                // Vinyl sheen anisotropic reflection highlight (rotating light reflection)
                drawVinylLightSheen(center, recordRadius)

                // 4. Center Vinyl Label (Vintage Gold/Amber Matte Disc)
                val labelRadius = recordRadius * 0.35f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(VintageBrassLight, VintageBrass, Color(0xFF9A7420)),
                        center = center,
                        radius = labelRadius
                    ),
                    radius = labelRadius,
                    center = center
                )

                // Label outer ring
                drawCircle(
                    color = Color(0xFF5E4510),
                    radius = labelRadius * 0.92f,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Center Spindle Hole & Brass Spindle Bushing
                drawCircle(
                    color = Color(0xFFE8D090),
                    radius = labelRadius * 0.22f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF1A1815),
                    radius = labelRadius * 0.12f,
                    center = center
                )
            }

            // 5. Stylus Tonearm Assembly
            val pivotCenter = Offset(canvasWidth * 0.84f, canvasHeight * 0.22f)
            drawTonearm(
                pivot = pivotCenter,
                angle = animatedTonearmAngle.value,
                recordCenter = center,
                recordRadius = recordRadius
            )
        }
    }
}

private fun DrawScope.drawStrobeDots(center: Offset, radius: Float, rotationAngle: Float) {
    val numDots = 48
    val dotRadius = 1.8f
    for (i in 0 until numDots) {
        val angleDeg = (i * (360f / numDots) + rotationAngle) * (PI / 180f)
        val x = center.x + radius * cos(angleDeg).toFloat()
        val y = center.y + radius * sin(angleDeg).toFloat()
        val dotAlpha = if (i % 4 == 0) 0.7f else 0.3f
        drawCircle(
            color = Color(0xFFD6C5A2).copy(alpha = dotAlpha),
            radius = dotRadius,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawVinylLightSheen(center: Offset, radius: Float) {
    // Two soft light reflection wedges (anisotropic vinyl reflection)
    val path1 = Path().apply {
        moveTo(center.x, center.y)
        val r1 = radius * 0.98f
        val a1 = -25.0 * (PI / 180.0)
        val a2 = 25.0 * (PI / 180.0)
        lineTo((center.x + r1 * cos(a1)).toFloat(), (center.y + r1 * sin(a1)).toFloat())
        lineTo((center.x + r1 * cos(a2)).toFloat(), (center.y + r1 * sin(a2)).toFloat())
        close()
    }
    drawPath(path1, brush = Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
        center = center,
        radius = radius
    ))

    val path2 = Path().apply {
        moveTo(center.x, center.y)
        val r1 = radius * 0.98f
        val a1 = 155.0 * (PI / 180.0)
        val a2 = 205.0 * (PI / 180.0)
        lineTo((center.x + r1 * cos(a1)).toFloat(), (center.y + r1 * sin(a1)).toFloat())
        lineTo((center.x + r1 * cos(a2)).toFloat(), (center.y + r1 * sin(a2)).toFloat())
        close()
    }
    drawPath(path2, brush = Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
        center = center,
        radius = radius
    ))
}

private fun DrawScope.drawTonearm(
    pivot: Offset,
    angle: Float,
    recordCenter: Offset,
    recordRadius: Float
) {
    // 1. Gimbal Pivot Base (Brushed metal cylinder with shadow)
    drawCircle(
        color = Color(0xFF0F0E0D).copy(alpha = 0.6f),
        radius = 28f,
        center = pivot + Offset(4f, 6f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF5A544D), Color(0xFF282522)),
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

    // Counterweight cylinder behind pivot
    val counterAngleRad = (angle + 180f) * (PI / 180f)
    val counterOffset = Offset(
        (pivot.x + 36f * cos(counterAngleRad)).toFloat(),
        (pivot.y + 36f * sin(counterAngleRad)).toFloat()
    )
    drawCircle(
        color = Color(0xFF38342E),
        radius = 13f,
        center = counterOffset
    )
    drawCircle(
        color = VintageBrass,
        radius = 6f,
        center = counterOffset
    )

    // Armrest clip post (where tonearm parks)
    val armrestPos = Offset(pivot.x - 30f, pivot.y + 70f)
    drawCircle(
        color = Color(0xFF38342E),
        radius = 7f,
        center = armrestPos
    )

    // Tonearm Wand: S-shaped audiophile curved arm
    rotate(degrees = angle, pivot = pivot) {
        val wandLength = recordRadius * 1.55f

        val start = pivot
        val p1 = Offset(pivot.x - wandLength * 0.45f, pivot.y + wandLength * 0.45f)
        val p2 = Offset(pivot.x - wandLength * 0.70f, pivot.y + wandLength * 0.85f)
        val headshellPos = Offset(pivot.x - wandLength * 0.82f, pivot.y + wandLength * 1.08f)

        // Drop shadow under wand
        val shadowPath = Path().apply {
            moveTo(start.x + 5f, start.y + 6f)
            cubicTo(
                p1.x + 5f, p1.y + 6f,
                p2.x + 5f, p2.y + 6f,
                headshellPos.x + 5f, headshellPos.y + 6f
            )
        }
        drawPath(
            shadowPath,
            color = Color(0x33000000),
            style = Stroke(width = 5.5f, cap = StrokeCap.Round)
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

        // Cartridge / Headshell (Black & Gold angled block)
        val hsWidth = 22f
        val hsHeight = 36f
        val hsCenter = headshellPos

        drawRoundRect(
            color = Color(0xFF1E1C1A),
            topLeft = Offset(hsCenter.x - hsWidth / 2f, hsCenter.y),
            size = Size(hsWidth, hsHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Gold stripe on cartridge
        drawLine(
            color = VintageBrass,
            start = Offset(hsCenter.x - hsWidth / 2f, hsCenter.y + hsHeight * 0.6f),
            end = Offset(hsCenter.x + hsWidth / 2f, hsCenter.y + hsHeight * 0.6f),
            strokeWidth = 2.5f
        )
        // Diamond stylus needle point
        drawCircle(
            color = Color.White,
            radius = 2.2f,
            center = Offset(hsCenter.x, hsCenter.y + hsHeight)
        )
    }
}
