package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class ActivityRingData(
    val id: String,
    val name: String,
    val color: Color,
    val progress: Float // 0.0 to 1.0+
)

@Composable
fun MasterRadialProgress(
    overallPercentage: Float, // 0.0 to 1.0+
    activityRings: List<ActivityRingData>,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val animatedOverall by animateFloatAsState(
        targetValue = overallPercentage.coerceAtLeast(0f),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "overall_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 8.dp.toPx()
            val spacing = 10.dp.toPx()
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val maxRadius = (this.size.minDimension / 2) - (strokeWidth / 2) - 4.dp.toPx()

            // Draw concentric rings for up to 4 core activities
            activityRings.take(4).forEachIndexed { index, ring ->
                val radius = maxRadius - (index * spacing)
                val diameter = radius * 2
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(diameter, diameter)

                // Background track
                drawArc(
                    color = ring.color.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round)
                )

                // Active progress arc
                val sweep = (ring.progress * 360f).coerceAtMost(360f)
                if (sweep > 0f) {
                    drawArc(
                        color = ring.color,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(animatedOverall * 100).toInt()}%",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "DAY MASTERY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CircularActivityRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 5.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "activity_ring"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val sw = strokeWidth.toPx()
            val radius = (this.size.minDimension / 2) - (sw / 2)
            val topLeft = Offset(sw / 2, sw / 2)
            val arcSize = Size(radius * 2, radius * 2)

            // Background track
            drawArc(
                color = color.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Round)
            )

            // Foreground progress
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(color.copy(alpha = 0.85f), color, color),
                        center = Offset(this.size.width / 2, this.size.height / 2)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
