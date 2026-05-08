package com.lalema.app.ui.theme

import androidx.compose.ui.graphics.Color

data class ColorPreset(
    val name: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val secondaryLight: Color,
    val secondaryDark: Color,
    val tertiaryLight: Color,
    val tertiaryDark: Color
)

val colorPresets = listOf(
    ColorPreset(
        name = "蓝紫",
        primaryLight = Color(0xFF4A5CE0),
        primaryDark = Color(0xFF9DA5FF),
        secondaryLight = Color(0xFF00A58E),
        secondaryDark = Color(0xFF4ECDC4),
        tertiaryLight = Color(0xFFE06030),
        tertiaryDark = Color(0xFFFFAB91)
    ),
    ColorPreset(
        name = "樱花粉",
        primaryLight = Color(0xFFD81B60),
        primaryDark = Color(0xFFF48FB1),
        secondaryLight = Color(0xFF8E24AA),
        secondaryDark = Color(0xFFCE93D8),
        tertiaryLight = Color(0xFFEF6C00),
        tertiaryDark = Color(0xFFFFCC80)
    ),
    ColorPreset(
        name = "薄荷绿",
        primaryLight = Color(0xFF00796B),
        primaryDark = Color(0xFF4DB6AC),
        secondaryLight = Color(0xFF2E7D32),
        secondaryDark = Color(0xFF81C784),
        tertiaryLight = Color(0xFFF57F17),
        tertiaryDark = Color(0xFFFFD54F)
    ),
    ColorPreset(
        name = "琥珀橙",
        primaryLight = Color(0xFFE65100),
        primaryDark = Color(0xFFFFAB40),
        secondaryLight = Color(0xFFC62828),
        secondaryDark = Color(0xFFEF9A9A),
        tertiaryLight = Color(0xFF5E35B1),
        tertiaryDark = Color(0xFFB39DDB)
    ),
    ColorPreset(
        name = "靛蓝",
        primaryLight = Color(0xFF283593),
        primaryDark = Color(0xFF7986CB),
        secondaryLight = Color(0xFF1565C0),
        secondaryDark = Color(0xFF64B5F6),
        tertiaryLight = Color(0xFF00695C),
        tertiaryDark = Color(0xFF80CBC4)
    ),
    ColorPreset(
        name = "玫瑰红",
        primaryLight = Color(0xFFB71C50),
        primaryDark = Color(0xFFF06292),
        secondaryLight = Color(0xFF6A1B9A),
        secondaryDark = Color(0xFFBA68C8),
        tertiaryLight = Color(0xFFE65100),
        tertiaryDark = Color(0xFFFFCC80)
    )
)

val PrimaryLight = colorPresets[0].primaryLight
val PrimaryDark = colorPresets[0].primaryDark
val SecondaryLight = colorPresets[0].secondaryLight
val SecondaryDark = colorPresets[0].secondaryDark
val TertiaryLight = colorPresets[0].tertiaryLight
val TertiaryDark = colorPresets[0].tertiaryDark

val SurfaceLight = Color(0xFFF8F9FF)
val SurfaceDark = Color(0xFF1A1C2E)
val BackgroundLight = Color(0xFFF5F6FF)
val BackgroundDark = Color(0xFF121420)

val OnSurfaceLight = Color(0xFF1A1C2E)
val OnSurfaceDark = Color(0xFFE8E8F0)

val OnSurfaceVariantLight = Color(0xFF4A4E68)
val OnSurfaceVariantDark = Color(0xFFB8B8CC)

val SuccessLight = Color(0xFF2E7D32)
val SuccessDark = Color(0xFF66BB6A)
val WarningLight = Color(0xFFEF6C00)
val WarningDark = Color(0xFFFFB74D)

val PrimaryContainerLight = Color(0xFFE0E0FF)
val PrimaryContainerDark = Color(0xFF2D3060)
val OnPrimaryContainerLight = Color(0xFF1A1C50)
val OnPrimaryContainerDark = Color(0xFFE0E0FF)

val ErrorLight = Color(0xFFD32F2F)
val ErrorDark = Color(0xFFEF5350)

val Brown500 = Color(0xFF8D6E63)
val Brown700 = Color(0xFF4E342E)
val Green500 = Color(0xFF4CAF50)

val GlassBgLight1 = Color(0xFFD4DEFF)
val GlassBgLight2 = Color(0xFFC8B6E0)
val GlassBgLight3 = Color(0xFFB8D0F0)
val GlassBgLight4 = Color(0xFFE0D0F0)

val GlassBgDark1 = Color(0xFF0A0E1A)
val GlassBgDark2 = Color(0xFF14102A)
val GlassBgDark3 = Color(0xFF0E1628)
val GlassBgDark4 = Color(0xFF1A1030)
