package com.lalema.app.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // iOS 26 Liquid Glass 效果参数
    val glassBackground = if (isDark) {
        Color(0x15FFFFFF)
    } else {
        Color(0x25FFFFFF)
    }
    
    val glassBorder = if (isDark) {
        Color(0x25FFFFFF)
    } else {
        Color(0x35FFFFFF)
    }
    
    val glassHighlight = if (isDark) {
        Color(0x10FFFFFF)
    } else {
        Color(0x40FFFFFF)
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .then(clickableModifier)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = Color.Black.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glassBackground.copy(alpha = 0.6f),
                            glassBackground.copy(alpha = 0.3f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glassBorder.copy(alpha = 0.8f),
                            glassBorder.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(20.dp),
            content = content
        )
    }
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
        targetValue = if (isPressed) 0.96f else 1f,
        label = "buttonScale"
    )

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) {
        Color(0x20FFFFFF)
    } else {
        Color(0x30FFFFFF)
    }
    val borderColor = if (isDark) {
        Color(0x30FFFFFF)
    } else {
        Color(0x40FFFFFF)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.8f),
                        backgroundColor.copy(alpha = 0.4f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = text,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelLarge
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
    val glassBg = if (isDark) Color(0x18FFFFFF) else Color(0x28FFFFFF)
    val borderColor = if (isDark) Color(0x22FFFFFF) else Color(0x32FFFFFF)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glassBg.copy(alpha = 0.7f),
                            glassBg.copy(alpha = 0.3f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = borderColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                icon?.invoke()
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
                androidx.compose.material3.Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.material3.Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LiquidGlassDivider(
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val color = if (isDark) Color(0x15FFFFFF) else Color(0x20FFFFFF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
    val surfaceColor = if (isDark) {
        Color(0x12FFFFFF)
    } else {
        Color(0x22FFFFFF)
    }
    val borderColor = if (isDark) {
        Color(0x18FFFFFF)
    } else {
        Color(0x28FFFFFF)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        surfaceColor.copy(alpha = 0.5f),
                        surfaceColor.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp),
        content = content
    )
}