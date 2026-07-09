package com.oussama_chatri.productivityx.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.oussama_chatri.productivityx.core.enums.AppTheme

private fun PxColorScheme.toM3Dark() = darkColorScheme(
    primary            = Primary,
    onPrimary          = OnPrimary,
    secondary          = Secondary,
    onSecondary        = OnPrimary,
    background         = Background,
    onBackground       = OnBackground,
    surface            = Surface,
    onSurface          = OnSurface,
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = OnSurfaceDim,
    error              = Error,
    onError            = OnPrimary,
    outline            = Outline,
)

private fun PxColorScheme.toM3Light() = lightColorScheme(
    primary            = Primary,
    onPrimary          = OnPrimary,
    secondary          = Secondary,
    onSecondary        = OnPrimary,
    background         = Background,
    onBackground       = OnBackground,
    surface            = Surface,
    onSurface          = OnSurface,
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = OnSurfaceDim,
    error              = Error,
    onError            = OnPrimary,
    outline            = Outline,
)

@Composable
fun ProductivityXTheme(
    appTheme: AppTheme = AppTheme.DARK,
    fontScale: Float = 1f,
    fontFamily: String = "Nunito",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val scheme = appColorScheme(appTheme, isSystemDark)

    SideEffect { PxColors.applyScheme(scheme) }
    
    val isDark = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemDark
        else -> true
    }

    val m3Colors = if (isDark) scheme.toM3Dark() else scheme.toM3Light()

    val scaledTypography = getScaledTypography(fontScale, fontFamily)

    MaterialTheme(
        colorScheme = m3Colors,
        typography  = scaledTypography,
        shapes      = PxShapes,
        content     = content
    )
}
