package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PrestigeEmblem(
    activityId: String,
    level: Int,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    isUnlocked: Boolean = true,
    isAnimated: Boolean = false
) {
    val primaryColor = when (activityId.lowercase()) {
        "meditation" -> EmeraldMeditation
        "study" -> CyanStudy
        "workout" -> FlameWorkout
        "sleep" -> MoonlitSleep
        else -> GoldPrimary
    }

    val infiniteTransition = rememberInfiniteTransition(label = "emblem_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimated && level >= 10) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isAnimated && level >= 7) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.minDimension / 2 * 0.88f

            if (!isUnlocked) {
                // Locked Silhouette / Dim Emblem
                drawCircle(
                    color = Color(0xFF1E2433),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF334155),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Draw locked padlock / dot symbol
                drawCircle(
                    color = Color(0xFF64748B),
                    radius = radius * 0.25f,
                    center = center
                )
                return@Canvas
            }

            // Draw level-specific prestigious layers
            when {
                level <= 3 -> {
                    // Tier 1 (Simple Refined): Single ring + Diamond + Inner core
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                            center = center,
                            radius = radius * 1.2f
                        ),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = radius * 0.85f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawDiamond(center, radius * 0.55f, primaryColor, fill = true)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = radius * 0.18f,
                        center = center
                    )
                }
                level <= 6 -> {
                    // Tier 2 (Rare / Disciplined): Double ring + Faceted Crest + Core
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent),
                            center = center,
                            radius = radius * 1.3f
                        ),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = GoldLight.copy(alpha = 0.6f),
                        radius = radius * 0.95f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = radius * 0.75f,
                        center = center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawFacetedStar(center, 4, radius * 0.55f, radius * 0.3f, primaryColor, GoldLight)
                    drawCircle(
                        color = Color.White,
                        radius = radius * 0.16f,
                        center = center
                    )
                }
                level <= 9 -> {
                    // Tier 3 (Elite / Master): 8-Point Starburst + Radiant Outer Ring + Dual Tone
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.65f), GoldPrimary.copy(alpha = 0.2f), Color.Transparent),
                            center = center,
                            radius = radius * 1.4f
                        ),
                        radius = radius * pulseScale,
                        center = center
                    )
                    // Outer star burst rays
                    drawFacetedStar(center, 8, radius * 0.95f, radius * 0.65f, GoldPrimary.copy(alpha = 0.7f), primaryColor)
                    drawCircle(
                        color = primaryColor,
                        radius = radius * 0.60f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawDiamond(center, radius * 0.40f, GoldLight, fill = true)
                    drawCircle(
                        color = Color.White,
                        radius = radius * 0.14f,
                        center = center
                    )
                }
                level <= 12 -> {
                    // Tier 4 (Mythic / Grandmaster): Intricate Multi-Layer Corona + Diamond Spire
                    rotate(rotation, pivot = center) {
                        drawFacetedStar(center, 12, radius * 1.0f, radius * 0.75f, GoldPrimary, primaryColor)
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = radius * 0.70f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawDiamond(center, radius * 0.52f, primaryColor, fill = true)
                    drawDiamond(center, radius * 0.32f, GoldLight, fill = true)
                    drawCircle(
                        color = Color.White,
                        radius = radius * 0.15f,
                        center = center
                    )
                }
                else -> {
                    // Tier 5 (Level 13: Supreme Transcendent / Living Legend): Celestial Crown Artifact
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, GoldLight, primaryColor, Color.Transparent),
                            center = center,
                            radius = radius * 1.5f
                        ),
                        radius = radius * 1.1f * pulseScale,
                        center = center
                    )
                    rotate(rotation, pivot = center) {
                        drawFacetedStar(center, 16, radius * 1.05f, radius * 0.78f, GoldPrimary, Color.White)
                    }
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(GoldPrimary, primaryColor, GoldLight, primaryColor, GoldPrimary),
                            center = center
                        ),
                        radius = radius * 0.72f,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawDiamond(center, radius * 0.56f, primaryColor, fill = true)
                    drawDiamond(center, radius * 0.38f, Color.White, fill = true)
                    drawDiamond(center, radius * 0.20f, GoldPrimary, fill = true)
                }
            }
        }
    }
}

private fun DrawScope.drawDiamond(center: Offset, size: Float, color: Color, fill: Boolean) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size * 0.8f, center.y)
        lineTo(center.x, center.y + size)
        lineTo(center.x - size * 0.8f, center.y)
        close()
    }
    if (fill) {
        drawPath(path, color = color)
    } else {
        drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))
    }
}

private fun DrawScope.drawFacetedStar(
    center: Offset,
    points: Int,
    outerRadius: Float,
    innerRadius: Float,
    primaryColor: Color,
    accentColor: Color
) {
    val angleStep = (Math.PI * 2 / (points * 2)).toFloat()
    val path = Path()

    for (i in 0 until (points * 2)) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * angleStep - (Math.PI / 2).toFloat()
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(primaryColor, accentColor),
            start = Offset(center.x - outerRadius, center.y - outerRadius),
            end = Offset(center.x + outerRadius, center.y + outerRadius)
        )
    )
}
