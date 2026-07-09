package com.oussama_chatri.productivityx.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.R

import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.text.font.SystemFontFamily

val Nunito = FontFamily(
    Font(R.font.nunito_light,    FontWeight.Light),
    Font(R.font.nunito_regular,  FontWeight.Normal),
    Font(R.font.nunito_medium,   FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold,     FontWeight.Bold)
)

object TypographyTokens {
    val HeadlineHero    = "displayLarge"
    val HeadlineMedium  = "displayMedium"
    val HeadlineSmall   = "displaySmall"
    val SectionHeader   = "headlineSmall"
    val CardTitle       = "titleSmall"
    val BodyPrimary     = "bodyLarge"
    val BodySecondary   = "bodyMedium"
    val BodyTertiary    = "bodySmall"
    val LabelProminent  = "labelLarge"
    val CaptionMuted    = "labelSmall"
}

fun getPxTypography(fontFamilyName: String): Typography {
    val family = when (fontFamilyName) {
        "System Default" -> FontFamily.Default
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        else -> Nunito // Default to Nunito
    }

    return Typography(
        displayLarge   = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.Bold,
            fontSize     = 36.sp,
            lineHeight   = 36.sp * 1.1f,
            letterSpacing = (-0.5).sp
        ),
        displayMedium  = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.Bold,
            fontSize     = 32.sp,
            lineHeight   = 32.sp * 1.15f,
            letterSpacing = (-0.25).sp
        ),
        displaySmall   = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 28.sp,
            lineHeight   = 28.sp * 1.2f,
            letterSpacing = 0.sp
        ),
        headlineLarge  = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 26.sp,
            lineHeight   = 26.sp * 1.2f
        ),
        headlineMedium = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 24.sp,
            lineHeight   = 24.sp * 1.2f
        ),
        headlineSmall  = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 20.sp,
            lineHeight   = 20.sp * 1.25f
        ),
        titleLarge     = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 20.sp,
            lineHeight   = 20.sp * 1.3f
        ),
        titleMedium    = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.Medium,
            fontSize     = 16.sp,
            lineHeight   = 16.sp * 1.3f
        ),
        titleSmall     = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.Medium,
            fontSize     = 14.sp,
            lineHeight   = 14.sp * 1.3f
        ),
        bodyLarge      = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.Normal,
            fontSize     = 16.sp,
            lineHeight   = 16.sp * 1.5f
        ),
        bodyMedium     = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.Normal,
            fontSize     = 14.sp,
            lineHeight   = 14.sp * 1.5f
        ),
        bodySmall      = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.Normal,
            fontSize     = 12.sp,
            lineHeight   = 12.sp * 1.5f
        ),
        labelLarge     = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = 14.sp,
            lineHeight   = 14.sp * 1.2f,
            letterSpacing = 0.25.sp
        ),
        labelMedium    = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.Medium,
            fontSize     = 12.sp,
            lineHeight   = 12.sp * 1.2f
        ),
        labelSmall     = TextStyle(
            fontFamily   = family,
            fontWeight   = FontWeight.Medium,
            fontSize     = 11.sp,
            lineHeight   = 11.sp * 1.2f,
            letterSpacing = 0.5.sp
        )
    )
}

fun getScaledTypography(scale: Float, fontFamilyName: String = "Nunito"): Typography {
    val baseTypography = getPxTypography(fontFamilyName)
    if (scale == 1f) return baseTypography
    return Typography(
        displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * scale, lineHeight = baseTypography.displayLarge.lineHeight * scale),
        displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * scale, lineHeight = baseTypography.displayMedium.lineHeight * scale),
        displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * scale, lineHeight = baseTypography.displaySmall.lineHeight * scale),
        headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * scale, lineHeight = baseTypography.headlineLarge.lineHeight * scale),
        headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * scale, lineHeight = baseTypography.headlineMedium.lineHeight * scale),
        headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * scale, lineHeight = baseTypography.headlineSmall.lineHeight * scale),
        titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * scale, lineHeight = baseTypography.titleLarge.lineHeight * scale),
        titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * scale, lineHeight = baseTypography.titleMedium.lineHeight * scale),
        titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * scale, lineHeight = baseTypography.titleSmall.lineHeight * scale),
        bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * scale, lineHeight = baseTypography.bodyLarge.lineHeight * scale),
        bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * scale, lineHeight = baseTypography.bodyMedium.lineHeight * scale),
        bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * scale, lineHeight = baseTypography.bodySmall.lineHeight * scale),
        labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * scale, lineHeight = baseTypography.labelLarge.lineHeight * scale),
        labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * scale, lineHeight = baseTypography.labelMedium.lineHeight * scale),
        labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * scale, lineHeight = baseTypography.labelSmall.lineHeight * scale)
    )
}
