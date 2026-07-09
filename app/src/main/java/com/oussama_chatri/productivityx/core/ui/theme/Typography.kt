package com.oussama_chatri.productivityx.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oussama_chatri.productivityx.R

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

val PxTypography = Typography(
    displayLarge   = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.Bold,
        fontSize     = 36.sp,
        lineHeight   = 36.sp * 1.1f,
        letterSpacing = (-0.5).sp
    ),
    displayMedium  = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.Bold,
        fontSize     = 32.sp,
        lineHeight   = 32.sp * 1.15f,
        letterSpacing = (-0.25).sp
    ),
    displaySmall   = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 28.sp,
        lineHeight   = 28.sp * 1.2f,
        letterSpacing = 0.sp
    ),
    headlineLarge  = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 26.sp,
        lineHeight   = 26.sp * 1.2f
    ),
    headlineMedium = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 24.sp,
        lineHeight   = 24.sp * 1.2f
    ),
    headlineSmall  = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 20.sp,
        lineHeight   = 20.sp * 1.25f
    ),
    titleLarge     = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 20.sp,
        lineHeight   = 20.sp * 1.3f
    ),
    titleMedium    = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.Medium,
        fontSize     = 16.sp,
        lineHeight   = 16.sp * 1.3f
    ),
    titleSmall     = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 14.sp * 1.3f
    ),
    bodyLarge      = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 16.sp * 1.5f
    ),
    bodyMedium     = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 14.sp * 1.5f
    ),
    bodySmall      = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 12.sp * 1.5f
    ),
    labelLarge     = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 14.sp,
        lineHeight   = 14.sp * 1.2f,
        letterSpacing = 0.25.sp
    ),
    labelMedium    = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 12.sp * 1.2f
    ),
    labelSmall     = TextStyle(
        fontFamily   = Nunito,
        fontWeight   = FontWeight.Medium,
        fontSize     = 11.sp,
        lineHeight   = 11.sp * 1.2f,
        letterSpacing = 0.5.sp
    ),
)

fun getScaledTypography(scale: Float): Typography {
    if (scale == 1f) return PxTypography
    return Typography(
        displayLarge = PxTypography.displayLarge.copy(fontSize = PxTypography.displayLarge.fontSize * scale, lineHeight = PxTypography.displayLarge.lineHeight * scale),
        displayMedium = PxTypography.displayMedium.copy(fontSize = PxTypography.displayMedium.fontSize * scale, lineHeight = PxTypography.displayMedium.lineHeight * scale),
        displaySmall = PxTypography.displaySmall.copy(fontSize = PxTypography.displaySmall.fontSize * scale, lineHeight = PxTypography.displaySmall.lineHeight * scale),
        headlineLarge = PxTypography.headlineLarge.copy(fontSize = PxTypography.headlineLarge.fontSize * scale, lineHeight = PxTypography.headlineLarge.lineHeight * scale),
        headlineMedium = PxTypography.headlineMedium.copy(fontSize = PxTypography.headlineMedium.fontSize * scale, lineHeight = PxTypography.headlineMedium.lineHeight * scale),
        headlineSmall = PxTypography.headlineSmall.copy(fontSize = PxTypography.headlineSmall.fontSize * scale, lineHeight = PxTypography.headlineSmall.lineHeight * scale),
        titleLarge = PxTypography.titleLarge.copy(fontSize = PxTypography.titleLarge.fontSize * scale, lineHeight = PxTypography.titleLarge.lineHeight * scale),
        titleMedium = PxTypography.titleMedium.copy(fontSize = PxTypography.titleMedium.fontSize * scale, lineHeight = PxTypography.titleMedium.lineHeight * scale),
        titleSmall = PxTypography.titleSmall.copy(fontSize = PxTypography.titleSmall.fontSize * scale, lineHeight = PxTypography.titleSmall.lineHeight * scale),
        bodyLarge = PxTypography.bodyLarge.copy(fontSize = PxTypography.bodyLarge.fontSize * scale, lineHeight = PxTypography.bodyLarge.lineHeight * scale),
        bodyMedium = PxTypography.bodyMedium.copy(fontSize = PxTypography.bodyMedium.fontSize * scale, lineHeight = PxTypography.bodyMedium.lineHeight * scale),
        bodySmall = PxTypography.bodySmall.copy(fontSize = PxTypography.bodySmall.fontSize * scale, lineHeight = PxTypography.bodySmall.lineHeight * scale),
        labelLarge = PxTypography.labelLarge.copy(fontSize = PxTypography.labelLarge.fontSize * scale, lineHeight = PxTypography.labelLarge.lineHeight * scale),
        labelMedium = PxTypography.labelMedium.copy(fontSize = PxTypography.labelMedium.fontSize * scale, lineHeight = PxTypography.labelMedium.lineHeight * scale),
        labelSmall = PxTypography.labelSmall.copy(fontSize = PxTypography.labelSmall.fontSize * scale, lineHeight = PxTypography.labelSmall.lineHeight * scale)
    )
}
