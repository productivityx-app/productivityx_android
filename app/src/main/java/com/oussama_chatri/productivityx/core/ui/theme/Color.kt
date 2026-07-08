package com.oussama_chatri.productivityx.core.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.oussama_chatri.productivityx.core.enums.AppTheme

object PriorityColors {
    val Low    = Color(0xFF6B7280)
    val Medium = Color(0xFF3B82F6)
    val High   = Color(0xFFF59E0B)
    val Urgent = Color(0xFFEF4444)
}

data class PxColorScheme(
    val Primary:        Color,
    val PrimaryVariant: Color,
    val Secondary:      Color,
    val Background:     Color,
    val Surface:        Color,
    val SurfaceVariant: Color,
    val OnPrimary:      Color,
    val OnBackground:   Color,
    val OnSurface:      Color,
    val OnSurfaceDim:   Color,
    val Outline:        Color,
    val Error:          Color,
    val ErrorVariant:   Color,
    val Success:        Color,
    val SuccessVariant: Color,
    val Warning:        Color,
    val WarningVariant: Color,
    val Info:           Color,
)

private fun Color.blendWith(other: Color, fraction: Float = 0.3f): Color {
    val r = (this.red * (1 - fraction) + other.red * fraction)
    val g = (this.green * (1 - fraction) + other.green * fraction)
    val b = (this.blue * (1 - fraction) + other.blue * fraction)
    return Color(r, g, b, this.alpha)
}

fun Color.harmonize(target: Color, brandPrimary: Color = Color(0xFF6366F1)): Color {
    val blendAmount = 0.15f
    return this.blendWith(brandPrimary, blendAmount)
}

private val BrandPrimary = Color(0xFF6366F1)
private val BrandSecondary = Color(0xFF8B5CF6)

private val DarkScheme = PxColorScheme(
    Primary        = Color(0xFF6366F1), // Vibrant Indigo
    PrimaryVariant = Color(0xFF4F46E5),
    Secondary      = Color(0xFF8B5CF6),
    Background     = Color(0xFF05050A), // OLED Deep Black
    Surface        = Color(0xFF111116), // Dark Gray
    SurfaceVariant = Color(0xFF1A1A22),
    OnPrimary      = Color(0xFFFFFFFF),
    OnBackground   = Color(0xFFEEEEF5),
    OnSurface      = Color(0xFFCCCCD8),
    OnSurfaceDim   = Color(0xFF888899),
    Outline        = Color(0xFF2A2A35),
    Error          = Color(0xFFEF4444),
    ErrorVariant   = Color(0xFF7F1D1D),
    Success        = Color(0xFF22C55E),
    SuccessVariant = Color(0xFF14532D),
    Warning        = Color(0xFFF59E0B),
    WarningVariant = Color(0xFF78350F),
    Info           = Color(0xFF3B82F6),
)

private val LightScheme = PxColorScheme(
    Primary        = Color(0xFF4F46E5), // Deep Indigo
    PrimaryVariant = Color(0xFF3730A3),
    Secondary      = Color(0xFF7C3AED),
    Background     = Color(0xFFFFFFFF), // Pure White
    Surface        = Color(0xFFF8F9FA), // Off-white
    SurfaceVariant = Color(0xFFEEEEF4),
    OnPrimary      = Color(0xFFFFFFFF),
    OnBackground   = Color(0xFF111118),
    OnSurface      = Color(0xFF333340),
    OnSurfaceDim   = Color(0xFF888899),
    Outline        = Color(0xFFD0D0E0),
    Error          = Color(0xFFDC2626),
    ErrorVariant   = Color(0xFFFEE2E2),
    Success        = Color(0xFF16A34A),
    SuccessVariant = Color(0xFFDCFCE7),
    Warning        = Color(0xFFD97706),
    WarningVariant = Color(0xFFFEF3C7),
    Info           = Color(0xFF2563EB),
)

fun appColorScheme(theme: AppTheme, isSystemInDarkTheme: Boolean): PxColorScheme = when (theme) {
    AppTheme.DARK   -> DarkScheme
    AppTheme.LIGHT  -> LightScheme
    AppTheme.SYSTEM -> if (isSystemInDarkTheme) DarkScheme else LightScheme
}

object PxColors {
    var Primary:        Color by mutableStateOf(DarkScheme.Primary);        internal set
    var PrimaryVariant: Color by mutableStateOf(DarkScheme.PrimaryVariant); internal set
    var Secondary:      Color by mutableStateOf(DarkScheme.Secondary);      internal set
    var Background:     Color by mutableStateOf(DarkScheme.Background);     internal set
    var Surface:        Color by mutableStateOf(DarkScheme.Surface);        internal set
    var SurfaceVariant: Color by mutableStateOf(DarkScheme.SurfaceVariant); internal set
    var OnPrimary:      Color by mutableStateOf(DarkScheme.OnPrimary);      internal set
    var OnBackground:   Color by mutableStateOf(DarkScheme.OnBackground);   internal set
    var OnSurface:      Color by mutableStateOf(DarkScheme.OnSurface);      internal set
    var OnSurfaceDim:   Color by mutableStateOf(DarkScheme.OnSurfaceDim);   internal set
    var Outline:        Color by mutableStateOf(DarkScheme.Outline);        internal set
    var Error:          Color by mutableStateOf(DarkScheme.Error);          internal set
    var ErrorVariant:   Color by mutableStateOf(DarkScheme.ErrorVariant);   internal set
    var Success:        Color by mutableStateOf(DarkScheme.Success);        internal set
    var SuccessVariant: Color by mutableStateOf(DarkScheme.SuccessVariant); internal set
    var Warning:        Color by mutableStateOf(DarkScheme.Warning);        internal set
    var WarningVariant: Color by mutableStateOf(DarkScheme.WarningVariant); internal set
    var Info:           Color by mutableStateOf(DarkScheme.Info);           internal set

    internal fun applyScheme(scheme: PxColorScheme) {
        Primary        = scheme.Primary
        PrimaryVariant = scheme.PrimaryVariant
        Secondary      = scheme.Secondary
        Background     = scheme.Background
        Surface        = scheme.Surface
        SurfaceVariant = scheme.SurfaceVariant
        OnPrimary      = scheme.OnPrimary
        OnBackground   = scheme.OnBackground
        OnSurface      = scheme.OnSurface
        OnSurfaceDim   = scheme.OnSurfaceDim
        Outline        = scheme.Outline
        Error          = scheme.Error
        ErrorVariant   = scheme.ErrorVariant
        Success        = scheme.Success
        SuccessVariant = scheme.SuccessVariant
        Warning        = scheme.Warning
        WarningVariant = scheme.WarningVariant
        Info           = scheme.Info
    }
}
