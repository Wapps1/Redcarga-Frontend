package com.wapps1.redcarga.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wapps1.redcarga.R

private val Montserrat = FontFamily(
    Font(resId = R.font.montserrat_regular,  weight = FontWeight.Normal),
    Font(resId = R.font.montserrat_medium,   weight = FontWeight.Medium),
    Font(resId = R.font.montserrat_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.montserrat_bold,     weight = FontWeight.Bold),
)

val Typography = Typography(
    displayLarge   = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Bold,     fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Bold,     fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall   = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Bold,     fontSize = 36.sp, lineHeight = 44.sp),

    headlineLarge  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Medium,   fontSize = 24.sp, lineHeight = 32.sp),

    titleLarge     = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium    = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),

    bodyLarge      = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),

    labelLarge     = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp),
)
