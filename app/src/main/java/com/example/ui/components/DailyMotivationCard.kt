package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.Quote
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun DailyMotivationCard(
    quote: Quote,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141414)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // Sophisticated Ambient Glow Background
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
            ) {
                val w = size.width
                val h = size.height

                // Dark gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF0D0D0D),
                            Color(0xFF050505)
                        ),
                        startY = 0f,
                        endY = h
                    )
                )

                // Subtle champagne radial glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = 0.14f),
                            GoldPrimary.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.9f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.5f, h * 0.9f)
                )
            }

            // Quote Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tradition Badge
                Surface(
                    color = Color(0x1AD4AF37),
                    shape = RoundedCornerShape(100.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = quote.tradition.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 9.sp
                        ),
                        color = GoldLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "“${quote.quote}”",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 24.sp,
                        fontSize = 15.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = Color(0xFFE5E5E0),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = quote.author.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )
            }
        }
    }
}
