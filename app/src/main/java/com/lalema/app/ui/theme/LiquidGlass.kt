package com.lalema.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateDpAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow

/**
 * 玻璃组件背后的内容源。
 *
 * backdrop 库的工作方式是「把背景内容录制成图层 → 施加模糊与折射 → 回绘」，
 * 因此每个玻璃组件都需要一个 Backdrop 来告诉它「要模糊什么」。
 * 由 [rememberGlassBackdrop] 创建、通过 [LocalGlassBackdrop] 向下传递。
 *
 * 缺省值为 [emptyBackdrop]，此时组件退化为纯色表面，不会崩溃。
 */
val LocalGlassBackdrop = staticCompositionLocalOf<Backdrop> { emptyBackdrop() }

/**
 * 创建全屏背景的内容源。
 *
 * 用法：在背景层加 `.layerBackdrop(backdrop)` 把它绘制的内容录进图层，
 * 再用 [LocalGlassBackdrop] 提供给内容层的玻璃组件。
 *
 * ```kotlin
 * val backdrop = rememberGlassBackdrop()
 * CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
 *     Box(Modifier.fillMaxSize()) {
 *         GlassBackground(Modifier.layerBackdrop(backdrop))
 *         MainScreen()
 *     }
 * }
 * ```
 */
@Composable
fun rememberGlassBackdrop(): LayerBackdrop = rememberLayerBackdrop()

/**
 * 把当前组件绘制的内容录制为玻璃组件的背景源。
 */
fun Modifier.glassBackdrop(backdrop: LayerBackdrop): Modifier = this.layerBackdrop(backdrop)

/**
 * 提供玻璃背景源给子树。
 */
@Composable
fun ProvideGlassBackdrop(
    backdrop: Backdrop,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalGlassBackdrop provides backdrop, content = content)
}

/**
 * 全屏背景。玻璃组件模糊的正是这一层绘制的内容，
 * 因此这里保留丰富的色彩层次，玻璃才能透出有层次的颜色。
 */
@Composable
fun GlassBackground(modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkTheme.current
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
                                Color(0xFFE0DCF8),
                                Color(0xFFE8E0F8),
                                Color(0xFFD8D8FC),
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

/**
 * 根据着色色相返回对比文字色：亮色（金/银等高明度）用深色字，其余用白色。
 * 用于玻璃组件带上品牌色着色时保证可读性。
 */
fun glassContentColor(tint: Color): Color {
    val luminance = 0.2126f * tint.red + 0.7152f * tint.green + 0.0722f * tint.blue
    return if (luminance > 0.6f) Color(0xFF1A1A1A) else Color.White
}

/**
 * 液态玻璃卡片。
 *
 * 真实玻璃质感由四部分组成：背景模糊 + 边缘折射（lens）+ 表面高光描边 +
 * 内阴影，再叠一层极淡的表面色控制明暗。
 *
 * @param tint 可选着色色相。传入后表面以该色着色（提高 alpha 以保颜色辨识度），
 *             文字色由调用方用 [glassContentColor] 取对比色。默认 null = 白玻璃。
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    tint: Color? = null,
    contentPadding: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    val isDark = LocalIsDarkTheme.current
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "cardScale"
    )

    val baseColor = tint ?: Color.White
    val surfaceColor by animateColorState(
        dark = baseColor.copy(alpha = if (tint != null) 0.55f else 0.07f),
        light = baseColor.copy(alpha = if (tint != null) 0.42f else 0.26f),
        label = "cardSurface"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { onClick() }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(24f.dp.toPx())
                    lens(14f.dp.toPx(), 18f.dp.toPx())
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 18.dp,
                        offset = androidx.compose.ui.unit.DpOffset(0.dp, 6.dp),
                        color = Color.Black.copy(alpha = if (isDark) 0.50f else 0.13f)
                    )
                },
                innerShadow = {
                    InnerShadow(
                        radius = 20.dp,
                        color = Color.White.copy(alpha = if (isDark) 0.10f else 0.42f)
                    )
                },
                layerBlock = {
                    scaleX = pressScale
                    scaleY = pressScale
                },
                onDrawSurface = { drawRect(surfaceColor) }
            )
            .padding(contentPadding),
        content = content
    )
}

/**
 * 液态玻璃按钮。用主题主色染色，按压时轻微缩放。
 *
 * @param tint 可选着色色相。传入后表面以该色着色、文字自动取对比色（详见 [glassContentColor]）。
 *             默认 null = 用主题主色。
 */
@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color? = null
) {
    val backdrop = LocalGlassBackdrop.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDark = LocalIsDarkTheme.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val shape = remember { RoundedCornerShape(16.dp) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "buttonScale"
    )

    val baseColor = tint ?: primaryColor
    val surfaceColor by animateColorState(
        dark = baseColor.copy(alpha = if (tint != null) 0.55f else 0.22f),
        light = baseColor.copy(alpha = if (tint != null) 0.45f else 0.16f),
        label = "btnSurface"
    )

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(18f.dp.toPx())
                    lens(10f.dp.toPx(), 16f.dp.toPx())
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 10.dp,
                        offset = androidx.compose.ui.unit.DpOffset(0.dp, 4.dp),
                        color = primaryColor.copy(alpha = 0.22f)
                    )
                },
                innerShadow = {
                    InnerShadow(
                        radius = 14.dp,
                        color = Color.White.copy(alpha = if (isDark) 0.12f else 0.38f)
                    )
                },
                layerBlock = {
                    scaleX = pressScale
                    scaleY = pressScale
                },
                onDrawSurface = { drawRect(surfaceColor) }
            )
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
            color = if (!enabled) baseColor.copy(alpha = 0.4f)
            else if (tint != null) glassContentColor(tint) else primaryColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 液态玻璃统计卡片。
 */
@Composable
fun LiquidGlassStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val backdrop = LocalGlassBackdrop.current
    val isDark = LocalIsDarkTheme.current
    val shape = remember { RoundedCornerShape(20.dp) }

    val surfaceColor by animateColorState(
        dark = Color.White.copy(alpha = 0.06f),
        light = Color.White.copy(alpha = 0.24f),
        label = "statSurface"
    )

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(20f.dp.toPx())
                    lens(12f.dp.toPx(), 16f.dp.toPx())
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 14.dp,
                        offset = androidx.compose.ui.unit.DpOffset(0.dp, 5.dp),
                        color = Color.Black.copy(alpha = if (isDark) 0.42f else 0.11f)
                    )
                },
                innerShadow = {
                    InnerShadow(
                        radius = 16.dp,
                        color = Color.White.copy(alpha = if (isDark) 0.08f else 0.40f)
                    )
                },
                onDrawSurface = { drawRect(surfaceColor) }
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon?.invoke()
            Spacer(modifier = Modifier.padding(2.dp))
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

/**
 * 分隔线。高度不足 1dp，模糊与折射在如此薄的区域内没有可见效果，
 * 因此沿用渐变绘制，不套玻璃图层。
 */
@Composable
fun LiquidGlassDivider(modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkTheme.current
    val color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.30f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = color.alpha * 0.5f),
                        color,
                        color.copy(alpha = color.alpha * 0.5f),
                        Color.Transparent
                    )
                )
            )
            .padding(vertical = 8.dp)
    )
}

/**
 * 液态玻璃表面容器，比卡片更淡，用于包裹次要内容。
 *
 * @param tint 可选着色色相，默认 null = 白玻璃。
 * @param contentPadding 内容内边距，chip 等小元素可传小值。
 * @param contentAlignment 内容对齐方式。
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    tint: Color? = null,
    contentPadding: Dp = 16.dp,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    val isDark = LocalIsDarkTheme.current
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }

    val baseColor = tint ?: Color.White
    val surfaceColor by animateColorState(
        dark = baseColor.copy(alpha = if (tint != null) 0.55f else 0.05f),
        light = baseColor.copy(alpha = if (tint != null) 0.42f else 0.22f),
        label = "surfaceTint"
    )

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(18f.dp.toPx())
                    lens(10f.dp.toPx(), 14f.dp.toPx())
                },
                highlight = { Highlight.Default },
                shadow = {
                    Shadow(
                        radius = 12.dp,
                        offset = androidx.compose.ui.unit.DpOffset(0.dp, 4.dp),
                        color = Color.Black.copy(alpha = if (isDark) 0.34f else 0.09f)
                    )
                },
                innerShadow = {
                    InnerShadow(
                        radius = 16.dp,
                        color = Color.White.copy(alpha = if (isDark) 0.07f else 0.36f)
                    )
                },
                onDrawSurface = { drawRect(surfaceColor) }
            )
            .padding(contentPadding),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * 玻璃风格内联时间选择器。
 */
@Composable
fun GlassInlineTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    val isDark = LocalIsDarkTheme.current
    val shape = remember { RoundedCornerShape(16.dp) }

    val containerColor by animateColorState(
        dark = Color.White.copy(alpha = 0.05f),
        light = Color.White.copy(alpha = 0.20f),
        label = "pickerSurface"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    blur(16f.dp.toPx())
                    lens(8f.dp.toPx(), 12f.dp.toPx())
                },
                highlight = { Highlight.Default },
                innerShadow = {
                    InnerShadow(
                        radius = 14.dp,
                        color = Color.White.copy(alpha = if (isDark) 0.08f else 0.34f)
                    )
                },
                onDrawSurface = { drawRect(containerColor) }
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GlassArrowButton(isDark = isDark) { onHourChange((hour + 1) % 24) }
                Spacer(modifier = Modifier.height(4.dp))
                GlassDigitBox(text = String.format("%02d", hour))
                Spacer(modifier = Modifier.height(4.dp))
                GlassArrowButton(isDark = isDark, isUp = false) { onHourChange((hour - 1 + 24) % 24) }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = ":",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GlassArrowButton(isDark = isDark) { onMinuteChange((minute + 1) % 60) }
                Spacer(modifier = Modifier.height(4.dp))
                GlassDigitBox(text = String.format("%02d", minute))
                Spacer(modifier = Modifier.height(4.dp))
                GlassArrowButton(isDark = isDark, isUp = false) { onMinuteChange((minute - 1 + 60) % 60) }
            }
        }
    }
}

/**
 * 时间选择器里的两位数字框，比外层再亮一档，形成层次。
 */
@Composable
private fun GlassDigitBox(text: String) {
    val backdrop = LocalGlassBackdrop.current
    val isDark = LocalIsDarkTheme.current
    val shape = remember { RoundedCornerShape(14.dp) }

    val surfaceColor by animateColorState(
        dark = Color.White.copy(alpha = 0.10f),
        light = Color.White.copy(alpha = 0.34f),
        label = "digitSurface"
    )

    Box(
        modifier = Modifier
            .size(56.dp, 48.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    blur(12f.dp.toPx())
                    lens(6f.dp.toPx(), 10f.dp.toPx())
                },
                highlight = { Highlight.Default },
                innerShadow = {
                    InnerShadow(
                        radius = 10.dp,
                        color = Color.White.copy(alpha = if (isDark) 0.10f else 0.42f)
                    )
                },
                onDrawSurface = { drawRect(surfaceColor) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 玻璃箭头按钮。
 */
@Composable
fun GlassArrowButton(
    isDark: Boolean,
    isUp: Boolean = true,
    onClick: () -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    val shape = remember { RoundedCornerShape(12.dp) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "arrowScale"
    )

    val surfaceColor by animateColorState(
        dark = Color.White.copy(alpha = 0.08f),
        light = Color.White.copy(alpha = 0.26f),
        label = "arrowSurface"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    blur(10f.dp.toPx())
                    lens(5f.dp.toPx(), 8f.dp.toPx())
                },
                highlight = { Highlight.Default },
                innerShadow = {
                    InnerShadow(
                        radius = 8.dp,
                        color = Color.White.copy(alpha = if (isDark) 0.10f else 0.40f)
                    )
                },
                layerBlock = {
                    scaleX = pressScale
                    scaleY = pressScale
                },
                onDrawSurface = { drawRect(surfaceColor) }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isUp) "▲" else "▼",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 液态玻璃开关。轨道为着色玻璃（选中用主色），滑块在轨道内滑动。
 * 全 App 统一使用此组件，取代原生 Switch。
 */
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val thumbColor by animateColorAsState(
        targetValue = if (checked) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "switchThumb"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "switchThumbOffset"
    )

    LiquidGlassSurface(
        modifier = modifier
            .width(52.dp)
            .height(28.dp)
            .clickable { onCheckedChange(!checked) },
        cornerRadius = 14.dp,
        tint = if (checked) primaryColor else null,
        contentPadding = 3.dp,
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .offset(x = thumbOffset)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

/**
 * 液态玻璃图标按钮，用于返回 / 关闭 / 删除 / 月份切换等图标操作，
 * 取代原生 IconButton 以统一玻璃观感。
 *
 * @param tint 传值时按钮整体着色（危险操作可传 error），图标色自动取对比色。
 */
@Composable
fun LiquidGlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    cornerRadius: Dp = 20.dp,
    tint: Color? = null,
    iconTint: Color = Color.Unspecified
) {
    val resolvedIconTint = if (iconTint != Color.Unspecified) {
        iconTint
    } else if (tint != null) {
        glassContentColor(tint)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    LiquidGlassSurface(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick),
        cornerRadius = cornerRadius,
        tint = tint,
        contentPadding = 0.dp,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = resolvedIconTint,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

/**
 * 液态玻璃单选按钮。选中时着色主色并显示内点，取代原生 RadioButton。
 */
@Composable
fun LiquidGlassRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    LiquidGlassSurface(
        modifier = modifier
            .size(24.dp)
            .clickable(onClick = onClick),
        cornerRadius = 12.dp,
        tint = if (selected) primaryColor else null,
        contentPadding = 0.dp,
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(glassContentColor(primaryColor))
            )
        }
    }
}

/**
 * 液态玻璃进度条。轨道为玻璃，进度用主色实心填充，取代原生 LinearProgressIndicator。
 */
@Composable
fun LiquidGlassProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        cornerRadius = 4.dp,
        contentPadding = 0.dp,
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

/**
 * 主题相关的玻璃表面色，明暗切换时平滑过渡。
 */
@Composable
private fun animateColorState(
    dark: Color,
    light: Color,
    label: String
): androidx.compose.runtime.State<Color> {
    val isDark = LocalIsDarkTheme.current
    return androidx.compose.animation.animateColorAsState(
        targetValue = if (isDark) dark else light,
        animationSpec = tween(500),
        label = label
    )
}
