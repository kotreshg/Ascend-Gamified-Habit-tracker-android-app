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
    isAnimated: Boolean = false,
    isHabitQuit: Boolean = false,
    isHabitBuild: Boolean = false
) {
    val primaryColor = when {
        isHabitQuit -> Color(0xFFEF4444) // Ruby / Crimson Aegis for Habit Quit
        isHabitBuild -> Color(0xFF10B981) // Emerald Vitality for Habit Build
        activityId.lowercase() == "meditation" -> EmeraldMeditation
        activityId.lowercase() == "study" -> CyanStudy
        activityId.lowercase() == "workout" -> FlameWorkout
        activityId.lowercase() == "sleep" -> MoonlitSleep
        else -> GoldPrimary
    }

    val infiniteTransition = rememberInfiniteTransition(label = "emblem_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimated && level >= 8) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (level >= 12) 15000 else 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = if (isAnimated && level >= 10) 0f else 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (isAnimated && level >= 6) 1.04f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
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
                // Locked Silhouette / Deep Obsidian Plate with subtle metallic rim
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1C1C20), Color(0xFF0F0F12)),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                // Locked geometric padlock
                drawPolygon(center, 6, radius * 0.45f, Color.White.copy(alpha = 0.15f), fill = false, strokeWidth = 2.dp.toPx())
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = radius * 0.14f,
                    center = center
                )
                return@Canvas
            }

            // Radial ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = if (level >= 10) 0.5f else 0.3f),
                        GoldPrimary.copy(alpha = if (level >= 7) 0.15f else 0.0f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * (1.1f + (level * 0.03f)) * pulseScale
                ),
                radius = radius * 1.25f,
                center = center
            )

            // Special Habit Quit Sovereign Shield
            if (isHabitQuit) {
                drawSovereignShield(center, radius, level, primaryColor, rotation, pulseScale)
                return@Canvas
            }

            // Distinct geometry for each level
            when (level) {
                1 -> {
                    // Level 1: "Initiate" - Obsidian Disc + Fine Gold Rim + Triangle + Spark Core
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF1A1A1E), Color(0xFF0A0A0C)),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = radius * 0.88f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawPolygon(center, 3, radius * 0.55f, primaryColor, fill = false, strokeWidth = 2.dp.toPx())
                    drawCircle(
                        color = Color.White,
                        radius = radius * 0.15f,
                        center = center
                    )
                }
                2 -> {
                    // Level 2: Concentric Dual Rings + Precision Diamond Crest + 4 Quadrant Pips
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.4f),
                        radius = radius * 0.92f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = radius * 0.75f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawDiamond(center, radius * 0.50f, primaryColor, fill = true)
                    drawDiamond(center, radius * 0.28f, Color.White, fill = true)
                    // 4 Quadrant Pips
                    drawCardinalPips(center, radius * 0.84f, 4, GoldLight)
                }
                3 -> {
                    // Level 3: Hexagonal Bastion Shield + Corner Brackets + Diamond
                    drawPolygon(center, 6, radius * 0.95f, primaryColor.copy(alpha = 0.3f), fill = true)
                    drawPolygon(center, 6, radius * 0.95f, primaryColor, fill = false, strokeWidth = 2.dp.toPx())
                    drawCircle(
                        color = GoldPrimary,
                        radius = radius * 0.68f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawDiamond(center, radius * 0.44f, primaryColor, fill = true)
                    drawCircle(color = Color.White, radius = radius * 0.16f, center = center)
                    drawCardinalPips(center, radius * 0.68f, 6, GoldPrimary)
                }
                4 -> {
                    // Level 4: Octagonal Imperial Seal + Beveled Edges + Cardinal Cross-Stars
                    drawPolygon(center, 8, radius * 0.96f, Color(0xFF16161A), fill = true)
                    drawPolygon(center, 8, radius * 0.96f, GoldPrimary, fill = false, strokeWidth = 1.5.dp.toPx())
                    drawPolygon(center, 8, radius * 0.80f, primaryColor, fill = false, strokeWidth = 2.dp.toPx())
                    drawFacetedStar(center, 4, radius * 0.58f, radius * 0.32f, primaryColor, GoldLight)
                    drawCircle(color = Color.White, radius = radius * 0.15f, center = center)
                }
                5 -> {
                    // Level 5: Pentagonal Fortress Crest + Engraved Chevrons + Floating Core Gem
                    drawPolygon(center, 5, radius * 0.98f, primaryColor.copy(alpha = 0.25f), fill = true)
                    drawPolygon(center, 5, radius * 0.98f, GoldLight, fill = false, strokeWidth = 2.dp.toPx())
                    drawPolygon(center, 5, radius * 0.76f, primaryColor, fill = false, strokeWidth = 1.8.dp.toPx())
                    drawFacetedStar(center, 5, radius * 0.54f, radius * 0.28f, primaryColor, GoldPrimary)
                    drawDiamond(center, radius * 0.26f, Color.White, fill = true)
                }
                6 -> {
                    // Level 6: Sacred Hexagram Star + Gold Trimmed Outer Ring
                    drawCircle(
                        color = GoldPrimary,
                        radius = radius * 0.96f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawFacetedStar(center, 6, radius * 0.88f, radius * 0.50f, primaryColor, GoldLight)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = radius * 0.44f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawDiamond(center, radius * 0.30f, Color.White, fill = true)
                }
                7 -> {
                    // Level 7: Mystical Heptagram (7-Point Starburst) + Pulsing Halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent),
                            center = center,
                            radius = radius * 1.1f * pulseScale
                        ),
                        radius = radius * 0.98f,
                        center = center
                    )
                    drawFacetedStar(center, 7, radius * 0.95f, radius * 0.58f, GoldPrimary, primaryColor)
                    drawCircle(color = primaryColor, radius = radius * 0.62f, center = center, style = Stroke(width = 2.dp.toPx()))
                    drawDiamond(center, radius * 0.40f, GoldLight, fill = true)
                    drawCircle(color = Color.White, radius = radius * 0.16f, center = center)
                }
                8 -> {
                    // Level 8: Grand 8-Point Compass Rose + Bi-Color Petals + Solar Core
                    rotate(rotation, pivot = center) {
                        drawCircle(
                            color = GoldPrimary.copy(alpha = 0.5f),
                            radius = radius * 0.98f,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                        )
                    }
                    drawFacetedStar(center, 8, radius * 0.94f, radius * 0.60f, primaryColor, GoldPrimary)
                    drawCircle(color = Color(0xFF141416), radius = radius * 0.54f, center = center)
                    drawCircle(color = GoldLight, radius = radius * 0.54f, center = center, style = Stroke(width = 2.dp.toPx()))
                    drawDiamond(center, radius * 0.36f, Color.White, fill = true)
                }
                9 -> {
                    // Level 9: 9-Point Astral Mandala + Orbiting Planetary Nodes
                    rotate(rotation, pivot = center) {
                        drawCircle(color = primaryColor.copy(alpha = 0.7f), radius = radius * 0.98f, center = center, style = Stroke(width = 2.dp.toPx()))
                        drawCardinalPips(center, radius * 0.98f, 9, GoldPrimary)
                    }
                    drawFacetedStar(center, 9, radius * 0.88f, radius * 0.58f, GoldLight, primaryColor)
                    drawPolygon(center, 9, radius * 0.52f, primaryColor, fill = true)
                    drawDiamond(center, radius * 0.32f, Color.White, fill = true)
                }
                10 -> {
                    // Level 10: 10-Point Solar Crest + Ornamental Sunburst Rays
                    rotate(rotation, pivot = center) {
                        drawFacetedStar(center, 10, radius * 1.0f, radius * 0.72f, GoldPrimary, primaryColor)
                    }
                    rotate(counterRotation, pivot = center) {
                        drawCircle(
                            color = GoldLight,
                            radius = radius * 0.74f,
                            center = center,
                            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
                        )
                    }
                    drawCircle(color = Color.White.copy(alpha = 0.9f), radius = radius * 0.52f, center = center, style = Stroke(width = 2.dp.toPx()))
                    drawDiamond(center, radius * 0.40f, primaryColor, fill = true)
                    drawDiamond(center, radius * 0.22f, GoldLight, fill = true)
                }
                11 -> {
                    // Level 11: Zodiac Archon Crest + 12-Pointed Starburst
                    rotate(rotation, pivot = center) {
                        drawFacetedStar(center, 12, radius * 1.02f, radius * 0.74f, primaryColor, GoldPrimary)
                    }
                    drawCircle(
                        brush = Brush.sweepGradient(listOf(GoldPrimary, primaryColor, GoldLight, primaryColor, GoldPrimary), center = center),
                        radius = radius * 0.72f,
                        center = center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawFacetedStar(center, 6, radius * 0.58f, radius * 0.35f, Color.White, GoldLight)
                    drawCircle(color = Color.White, radius = radius * 0.16f, center = center)
                }
                12 -> {
                    // Level 12: Mythic Corona — Layered 16-Facet Star + Dual Counter-Rotating Astral Halos
                    rotate(rotation, pivot = center) {
                        drawFacetedStar(center, 16, radius * 1.05f, radius * 0.78f, GoldPrimary, Color.White)
                    }
                    rotate(counterRotation, pivot = center) {
                        drawCircle(
                            brush = Brush.sweepGradient(listOf(primaryColor, GoldLight, primaryColor), center = center),
                            radius = radius * 0.75f,
                            center = center,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCardinalPips(center, radius * 0.75f, 8, Color.White)
                    }
                    drawDiamond(center, radius * 0.54f, primaryColor, fill = true)
                    drawDiamond(center, radius * 0.34f, GoldLight, fill = true)
                    drawDiamond(center, radius * 0.18f, Color.White, fill = true)
                }
                else -> {
                    // Level 13: Supreme Celestial Crown / Omniscient Living Legend
                    // Triple rotating auric halos, 16-point faceted celestial nova, radiant prism!
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, GoldLight, primaryColor, Color.Transparent),
                            center = center,
                            radius = radius * 1.4f * pulseScale
                        ),
                        radius = radius * 1.15f,
                        center = center
                    )
                    rotate(rotation, pivot = center) {
                        drawFacetedStar(center, 16, radius * 1.08f, radius * 0.82f, GoldPrimary, Color.White)
                    }
                    rotate(counterRotation, pivot = center) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(GoldPrimary, primaryColor, GoldLight, Color.White, GoldPrimary),
                                center = center
                            ),
                            radius = radius * 0.78f,
                            center = center,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCardinalPips(center, radius * 0.78f, 12, GoldLight)
                    }
                    drawFacetedStar(center, 8, radius * 0.62f, radius * 0.40f, primaryColor, GoldLight)
                    drawDiamond(center, radius * 0.46f, Color.White, fill = true)
                    drawDiamond(center, radius * 0.28f, GoldPrimary, fill = true)
                    drawCircle(color = Color.White, radius = radius * 0.12f, center = center)
                }
            }
        }
    }
}

private fun DrawScope.drawSovereignShield(
    center: Offset,
    radius: Float,
    level: Int,
    color: Color,
    rotation: Float,
    pulseScale: Float
) {
    // Sovereign Aegis Shield for Habit Quit
    val shieldPath = Path().apply {
        moveTo(center.x, center.y - radius * 0.95f)
        cubicTo(
            center.x + radius * 0.85f, center.y - radius * 0.90f,
            center.x + radius * 0.90f, center.y - radius * 0.20f,
            center.x + radius * 0.80f, center.y + radius * 0.30f
        )
        cubicTo(
            center.x + radius * 0.50f, center.y + radius * 0.75f,
            center.x, center.y + radius * 1.02f,
            center.x, center.y + radius * 1.02f
        )
        cubicTo(
            center.x, center.y + radius * 1.02f,
            center.x - radius * 0.50f, center.y + radius * 0.75f,
            center.x - radius * 0.80f, center.y + radius * 0.30f
        )
        cubicTo(
            center.x - radius * 0.90f, center.y - radius * 0.20f,
            center.x - radius * 0.85f, center.y - radius * 0.90f,
            center.x, center.y - radius * 0.95f
        )
        close()
    }

    // Shield background
    drawPath(
        path = shieldPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF221518), Color(0xFF0F080A)),
            startY = center.y - radius,
            endY = center.y + radius
        )
    )

    // Shield border
    drawPath(
        path = shieldPath,
        brush = Brush.verticalGradient(
            colors = listOf(GoldLight, color, Color(0xFF991B1B)),
            startY = center.y - radius,
            endY = center.y + radius
        ),
        style = Stroke(width = (2.5f + (level * 0.15f)).dp.toPx())
    )

    // Inner heraldic crest: Sword / Cross / Star
    drawFacetedStar(center, 4, radius * 0.55f, radius * 0.25f, GoldLight, color)
    drawDiamond(center, radius * 0.25f, Color.White, fill = true)

    if (level >= 6) {
        rotate(rotation, pivot = center) {
            drawCircle(
                color = GoldPrimary.copy(alpha = 0.4f),
                radius = radius * 0.78f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
            )
        }
    }
}

private fun DrawScope.drawDiamond(center: Offset, size: Float, color: Color, fill: Boolean) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size * 0.82f, center.y)
        lineTo(center.x, center.y + size)
        lineTo(center.x - size * 0.82f, center.y)
        close()
    }
    if (fill) {
        drawPath(path, color = color)
    } else {
        drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))
    }
}

private fun DrawScope.drawPolygon(
    center: Offset,
    sides: Int,
    radius: Float,
    color: Color,
    fill: Boolean,
    strokeWidth: Float = 2f
) {
    val angleStep = (Math.PI * 2 / sides).toFloat()
    val path = Path()

    for (i in 0 until sides) {
        val angle = i * angleStep - (Math.PI / 2).toFloat()
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    if (fill) {
        drawPath(path, color = color)
    } else {
        drawPath(path, color = color, style = Stroke(width = strokeWidth))
    }
}

private fun DrawScope.drawCardinalPips(center: Offset, radius: Float, count: Int, color: Color) {
    val angleStep = (Math.PI * 2 / count).toFloat()
    for (i in 0 until count) {
        val angle = i * angleStep - (Math.PI / 2).toFloat()
        val x = center.x + radius * cos(angle)
        val y = center.y + radius * sin(angle)
        drawCircle(color = color, radius = 2.5.dp.toPx(), center = Offset(x, y))
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
