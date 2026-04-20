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

val PxTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge     = TextStyle(fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium    = TextStyle(fontFamily = Nunito,
        fontWeight = FontWeight.Medium,   fontSize = 16.sp),
    bodyLarge      = TextStyle(fontFamily = Nunito,
        fontWeight = FontWeight.Normal,   fontSize = 16.sp),
    bodyMedium     = TextStyle(fontFamily = Nunito,
        fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    bodySmall      = TextStyle(fontFamily = Nunito,
        fontWeight = FontWeight.Normal,   fontSize = 12.sp),
    labelMedium    = TextStyle(fontFamily = Nunito,
        fontWeight = FontWeight.Medium,   fontSize = 12.sp),
    labelSmall     = TextStyle(fontFamily = Nunito,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp, letterSpacing = 0.5.sp)
)