package com.lalema.app.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

@Composable
fun GlassBackground(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "glassShimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height
                if (isDark) {
                    drawRect(Color(0xFF080A14))
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1A1040).copy(alpha = 0.6f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.3f + shimmerOffset * w * 0.2f, h * 0.2f),
                            radius = w * 0.8f
                        )
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0A2040).copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.7f - shimmerOffset * w * 0.15f, h * 0.6f),
                            radius = w * 0.7f
                        )
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF201030).copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.9f + shimmerOffset * h * 0.1f),
                            radius = w * 0.5f
                        )
                    )
                } else {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFD8E0FF),
                                Color(0xFFE8E0F8),
                                Color(0xFFD0D8FF)
                            )
                        )
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFC0D0FF).copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.2f + shimmerOffset * w * 0.3f, h * 0.15f),
                            radius = w * 0.7f
                        )
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE0C8F0).copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.8f - shimmerOffset * w * 0.2f, h * 0.5f),
                            radius = w * 0.6f
                        )
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFB8D0F8).copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.85f),
                            radius = w * 0.5f
                        )
                    )
                }
            }
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(cornerRadius)

    val glassBg = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }

    val glassBorder = if (isDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.70f)
    }

    val shadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.4f)
    } else {
        Color(0xFF6080C0).copy(alpha = 0.10f)
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                spotColor = shadowColor,
                ambientColor = shadowColor
            )
            .then(clickableModifier)
            .clip(shape)
            .background(glassBg)
            .border(width = 1.dp, color = glassBorder, shape = shape)
            .drawBehind {
                val w = size.width
                val h = size.height
                // Top highlight - simulates light reflection on glass
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.10f else 0.50f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.25f
                    )
                )
                // Left edge highlight
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.06f else 0.30f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = w * 0.15f
                    )
                )
            }
            .padding(20.dp),
        content = content
    )
}

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "buttonScale"
    )

    val isDark = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    val glassBg = if (isDark) {
        primaryColor.copy(alpha = 0.20f)
    } else {
        primaryColor.copy(alpha = 0.12f)
    }

    val borderColor = if (isDark) {
        primaryColor.copy(alpha = 0.40f)
    } else {
        primaryColor.copy(alpha = 0.30f)
    }

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                spotColor = primaryColor.copy(alpha = 0.15f)
            )
            .clip(shape)
            .background(glassBg)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .drawBehind {
                val h = size.height
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.08f else 0.35f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.4f
                    )
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) primaryColor else primaryColor.copy(alpha = 0.4f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LiquidGlassStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(20.dp)

    val glassBg = if (isDark) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.White.copy(alpha = 0.50f)
    }

    val glassBorder = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.65f)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                spotColor = if (isDark) Color.Black.copy(alpha = 0.3f) else Color(0xFF6080C0).copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(glassBg)
            .border(width = 1.dp, color = glassBorder, shape = shape)
            .drawBehind {
                val w = size.width
                val h = size.height
                // Top highlight
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.08f else 0.40f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.30f
                    )
                )
                // Left edge highlight
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.05f else 0.25f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = w * 0.12f
                    )
                )
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon?.invoke()
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LiquidGlassDivider(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.30f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color,
                        Color.Transparent
                    )
                )
            )
            .padding(vertical = 8.dp)
    )
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(cornerRadius)

    val glassBg = if (isDark) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.45f)
    }

    val borderColor = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.60f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(glassBg)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .drawBehind {
                val h = size.height
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.06f else 0.35f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.25f
                    )
                )
            }
            .padding(16.dp),
        content = content
    )
}
