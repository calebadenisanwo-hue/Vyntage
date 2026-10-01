package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGlowDim
import com.example.ui.theme.AmberTubeGlow
import com.example.ui.theme.CourierPrimeFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.VUMeterAmber
import com.example.ui.theme.VUMeterGreen
import com.example.ui.theme.VUMeterRed
import com.example.ui.theme.VintageBrass
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DualVuMeterView(
    leftLevel: Float,
    rightLevel: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("dual_vu_meter")
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(1.dp, VintageBrass.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VU LEVEL - DECIBELS",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 9.sp,
                    color = VintageBrass.copy(alpha = 0.8f),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "ANALOG BALLISTICS",
                    fontFamily = CourierPrimeFontFamily,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SingleVuMeter(
                    channelLabel = "CH 1 · L",
                    level = leftLevel,
                    modifier = Modifier.weight(1f)
                )
                SingleVuMeter(
                    channelLabel = "CH 2 · R",
                    level = rightLevel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SingleVuMeter(
    channelLabel: String,
    level: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2B261D), // Warm amber tube backlight
                        Color(0xFF1A1713)
                    )
                )
            )
            .border(1.dp, Color(0xFF3D372E), RoundedCornerShape(6.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawVuScaleAndNeedle(level)
        }

        Text(
            text = channelLabel,
            fontFamily = CourierPrimeFontFamily,
            fontSize = 8.sp,
            color = Color(0xFFD6C5A2).copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 3.dp)
        )

        // Peak indicator LED
        val isPeak = level > 0.85f
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 6.dp)
                .width(5.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (isPeak) VUMeterRed else Color(0xFF4A1818))
        )
    }
}

private fun DrawScope.drawVuScaleAndNeedle(level: Float) {
    val w = size.width
    val h = size.height

    val pivot = Offset(w * 0.5f, h * 1.35f)
    val needleLength = h * 1.05f

    // Arc scale line
    val scaleRadius = h * 0.92f
    val startAngleDeg = 215f
    val endAngleDeg = 325f
    val sweepDeg = endAngleDeg - startAngleDeg

    // Scale tick marks
    val numTicks = 9
    for (i in 0 until numTicks) {
        val frac = i.toFloat() / (numTicks - 1)
        val tickAngleDeg = startAngleDeg + frac * sweepDeg
        val angleRad = tickAngleDeg * (PI / 180f)

        val rOuter = scaleRadius
        val isMajor = i % 2 == 0 || i >= 7
        val rInner = if (isMajor) scaleRadius - 7f else scaleRadius - 4f

        val start = Offset(
            (pivot.x + rInner * cos(angleRad)).toFloat(),
            (pivot.y + rInner * sin(angleRad)).toFloat()
        )
        val end = Offset(
            (pivot.x + rOuter * cos(angleRad)).toFloat(),
            (pivot.y + rOuter * sin(angleRad)).toFloat()
        )

        // Red zone for last 2 ticks (> 0 dB)
        val tickColor = when {
            i >= 7 -> VUMeterRed
            i >= 5 -> VUMeterAmber
            else -> Color(0xFFC7BBAA)
        }

        drawLine(
            color = tickColor,
            start = start,
            end = end,
            strokeWidth = if (isMajor) 1.8f else 1.0f
        )
    }

    // Dynamic Needle
    val clampedLevel = level.coerceIn(0f, 1f)
    val needleAngleDeg = startAngleDeg + clampedLevel * sweepDeg
    val needleAngleRad = needleAngleDeg * (PI / 180f)

    val needleTip = Offset(
        (pivot.x + needleLength * cos(needleAngleRad)).toFloat(),
        (pivot.y + needleLength * sin(needleAngleRad)).toFloat()
    )

    // Needle shadow
    drawLine(
        color = Color(0x33000000),
        start = pivot + Offset(2f, 2f),
        end = needleTip + Offset(2f, 2f),
        strokeWidth = 1.5f,
        cap = StrokeCap.Round
    )

    // Thin vintage needle
    drawLine(
        color = if (clampedLevel > 0.85f) VUMeterRed else Color(0xFFF0E5D3),
        start = pivot,
        end = needleTip,
        strokeWidth = 1.6f,
        cap = StrokeCap.Round
    )
}
