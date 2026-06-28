package com.oussama_chatri.productivityx.core.ui.theme

import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
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
    Primary        = Color(0xFF6366F1),
    PrimaryVariant = Color(0xFF4F46E5),
    Secondary      = Color(0xFF8B5CF6),
    Background     = Color(0xFF0F0F14),
    Surface        = Color(0xFF1A1A24),
    SurfaceVariant = Color(0xFF252533),
    OnPrimary      = Color(0xFFFFFFFF),
    OnBackground   = Color(0xFFEEEEF5),
    OnSurface      = Color(0xFFCCCCD8),
    OnSurfaceDim   = Color(0xFF888899),
    Outline        = Color(0xFF3A3A50),
    Error          = Color(0xFFEF4444),
    ErrorVariant   = Color(0xFF7F1D1D),
    Success        = Color(0xFF22C55E),
    SuccessVariant = Color(0xFF14532D),
    Warning        = Color(0xFFF59E0B),
    WarningVariant = Color(0xFF78350F),
    Info           = Color(0xFF3B82F6),
)

private val LightScheme = PxColorScheme(
    Primary        = Color(0xFF4F46E5),
    PrimaryVariant = Color(0xFF3730A3),
    Secondary      = Color(0xFF7C3AED),
    Background     = Color(0xFFF4F4F8),
    Surface        = Color(0xFFFFFFFF),
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

private val OceanScheme = PxColorScheme(
    Primary        = Color(0xFF06B6D4),
    PrimaryVariant = Color(0xFF0891B2),
    Secondary      = Color(0xFF38BDF8),
    Background     = Color(0xFF060D14),
    Surface        = Color(0xFF0C1A26),
    SurfaceVariant = Color(0xFF152433),
    OnPrimary      = Color(0xFF000D14),
    OnBackground   = Color(0xFFE0F2FE),
    OnSurface      = Color(0xFFBAE6FD),
    OnSurfaceDim   = Color(0xFF5CA7C4),
    Outline        = Color(0xFF1E3A4F),
    Error          = Color(0xFFEF4444),
    ErrorVariant   = Color(0xFF7F1D1D),
    Success        = Color(0xFF10B981),
    SuccessVariant = Color(0xFF064E3B),
    Warning        = Color(0xFFF59E0B),
    WarningVariant = Color(0xFF78350F),
    Info           = Color(0xFF818CF8),
)

private val AmberScheme = PxColorScheme(
    Primary        = Color(0xFFF59E0B),
    PrimaryVariant = Color(0xFFD97706),
    Secondary      = Color(0xFFFBBF24),
    Background     = Color(0xFF100C00),
    Surface        = Color(0xFF1A1400),
    SurfaceVariant = Color(0xFF261E00),
    OnPrimary      = Color(0xFF100C00),
    OnBackground   = Color(0xFFFEF3C7),
    OnSurface      = Color(0xFFFDE68A),
    OnSurfaceDim   = Color(0xFFB8962A),
    Outline        = Color(0xFF3D3000),
    Error          = Color(0xFFEF4444),
    ErrorVariant   = Color(0xFF7F1D1D),
    Success        = Color(0xFF22C55E),
    SuccessVariant = Color(0xFF14532D),
    Warning        = Color(0xFFFCA5A5),
    WarningVariant = Color(0xFF7F1D1D),
    Info           = Color(0xFF60A5FA),
)

private val ForestScheme = PxColorScheme(
    Primary        = Color(0xFF22C55E),
    PrimaryVariant = Color(0xFF16A34A),
    Secondary      = Color(0xFF4ADE80),
    Background     = Color(0xFF060E08),
    Surface        = Color(0xFF0D1A10),
    SurfaceVariant = Color(0xFF142419),
    OnPrimary      = Color(0xFF060E08),
    OnBackground   = Color(0xFFDCFCE7),
    OnSurface      = Color(0xFFBBF7D0),
    OnSurfaceDim   = Color(0xFF4D9B67),
    Outline        = Color(0xFF1C3D24),
    Error          = Color(0xFFEF4444),
    ErrorVariant   = Color(0xFF7F1D1D),
    Success        = Color(0xFF86EFAC),
    SuccessVariant = Color(0xFF14532D),
    Warning        = Color(0xFFF59E0B),
    WarningVariant = Color(0xFF78350F),
    Info           = Color(0xFF38BDF8),
)

private val RoseScheme = PxColorScheme(
    Primary        = Color(0xFFF43F5E),
    PrimaryVariant = Color(0xFFE11D48),
    Secondary      = Color(0xFFFB7185),
    Background     = Color(0xFF100509),
    Surface        = Color(0xFF1C0A10),
    SurfaceVariant = Color(0xFF280F17),
    OnPrimary      = Color(0xFFFFFFFF),
    OnBackground   = Color(0xFFFFE4E6),
    OnSurface      = Color(0xFFFECDD3),
    OnSurfaceDim   = Color(0xFFA85068),
    Outline        = Color(0xFF3D1522),
    Error          = Color(0xFFFF6B6B),
    ErrorVariant   = Color(0xFF7F1D1D),
    Success        = Color(0xFF22C55E),
    SuccessVariant = Color(0xFF14532D),
    Warning        = Color(0xFFF59E0B),
    WarningVariant = Color(0xFF78350F),
    Info           = Color(0xFF60A5FA),
)

private val MidnightScheme = PxColorScheme(
    Primary        = Color(0xFF6366F1),
    PrimaryVariant = Color(0xFF4F46E5),
    Secondary      = Color(0xFF818CF8),
    Background     = Color(0xFF07070E),
    Surface        = Color(0xFF10101F),
    SurfaceVariant = Color(0xFF191930),
    OnPrimary      = Color(0xFFFFFFFF),
    OnBackground   = Color(0xFFE0E0F5),
    OnSurface      = Color(0xFFC0C0DD),
    OnSurfaceDim   = Color(0xFF7070AA),
    Outline        = Color(0xFF2A2A4A),
    Error          = Color(0xFFEF4444),
    ErrorVariant   = Color(0xFF7F1D1D),
    Success        = Color(0xFF22C55E),
    SuccessVariant = Color(0xFF14532D),
    Warning        = Color(0xFFF59E0B),
    WarningVariant = Color(0xFF78350F),
    Info           = Color(0xFF38BDF8),
)

fun appColorScheme(theme: AppTheme, isSystemInDarkTheme: Boolean): PxColorScheme = when (theme) {
    AppTheme.DARK     -> DarkScheme
    AppTheme.LIGHT    -> LightScheme
    AppTheme.SYSTEM   -> if (isSystemInDarkTheme) DarkScheme else LightScheme
    AppTheme.OCEAN    -> OceanScheme
    AppTheme.AMBER    -> AmberScheme
    AppTheme.FOREST   -> ForestScheme
    AppTheme.ROSE     -> RoseScheme
    AppTheme.MIDNIGHT -> MidnightScheme
    AppTheme.DYNAMIC  -> DarkScheme
}

@Composable
fun dynamicPxColorScheme(isDark: Boolean): PxColorScheme {
    val context = LocalContext.current
    val m3Dynamic = if (isDark) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
    return PxColorScheme(
        Primary        = m3Dynamic.primary.harmonize(BrandPrimary),
        PrimaryVariant = m3Dynamic.primary.harmonize(BrandPrimary).let {
            if (isDark) it.copy(red = it.red * 0.85f, green = it.green * 0.85f, blue = it.blue * 0.85f)
            else it.copy(red = it.red * 1.15f.coerceAtMost(1f), green = it.green * 1.15f.coerceAtMost(1f), blue = it.blue * 1.15f.coerceAtMost(1f))
        },
        Secondary      = m3Dynamic.secondary.harmonize(BrandSecondary),
        Background     = if (isDark) Color(0xFF0F0F14) else m3Dynamic.background,
        Surface        = if (isDark) Color(0xFF1A1A24) else m3Dynamic.surface,
        SurfaceVariant = if (isDark) Color(0xFF252533) else m3Dynamic.surfaceVariant,
        OnPrimary      = m3Dynamic.onPrimary,
        OnBackground   = m3Dynamic.onBackground,
        OnSurface      = m3Dynamic.onSurface,
        OnSurfaceDim   = m3Dynamic.onSurfaceVariant,
        Outline        = m3Dynamic.outline,
        Error          = m3Dynamic.error,
        ErrorVariant   = m3Dynamic.errorContainer,
        Success        = Color(0xFF22C55E),
        SuccessVariant = Color(0xFF14532D),
        Warning        = Color(0xFFF59E0B),
        WarningVariant = Color(0xFF78350F),
        Info           = Color(0xFF3B82F6),
    )
}

object PxColors {
    var Primary:        Color = DarkScheme.Primary;        internal set
    var PrimaryVariant: Color = DarkScheme.PrimaryVariant; internal set
    var Secondary:      Color = DarkScheme.Secondary;      internal set
    var Background:     Color = DarkScheme.Background;     internal set
    var Surface:        Color = DarkScheme.Surface;        internal set
    var SurfaceVariant: Color = DarkScheme.SurfaceVariant; internal set
    var OnPrimary:      Color = DarkScheme.OnPrimary;      internal set
    var OnBackground:   Color = DarkScheme.OnBackground;   internal set
    var OnSurface:      Color = DarkScheme.OnSurface;      internal set
    var OnSurfaceDim:   Color = DarkScheme.OnSurfaceDim;   internal set
    var Outline:        Color = DarkScheme.Outline;        internal set
    var Error:          Color = DarkScheme.Error;          internal set
    var ErrorVariant:   Color = DarkScheme.ErrorVariant;   internal set
    var Success:        Color = DarkScheme.Success;        internal set
    var SuccessVariant: Color = DarkScheme.SuccessVariant; internal set
    var Warning:        Color = DarkScheme.Warning;        internal set
    var WarningVariant: Color = DarkScheme.WarningVariant; internal set
    var Info:           Color = DarkScheme.Info;           internal set

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
