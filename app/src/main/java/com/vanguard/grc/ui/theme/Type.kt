package com.vanguard.grc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

private val Alignment = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = Alignment
)

/**
 * Typography is deliberately restrained: a single family, tight tracking on
 * headings, generous line height on body copy for long-form study material.
 */
val VanguardTypography = Typography(
    displayLarge = style(40, 48, FontWeight.Bold, (-0.5)),
    displayMedium = style(34, 42, FontWeight.Bold, (-0.4)),
    displaySmall = style(30, 38, FontWeight.Bold, (-0.3)),
    headlineLarge = style(26, 34, FontWeight.SemiBold, (-0.2)),
    headlineMedium = style(22, 30, FontWeight.SemiBold, (-0.1)),
    headlineSmall = style(19, 26, FontWeight.SemiBold),
    titleLarge = style(17, 24, FontWeight.SemiBold),
    titleMedium = style(15, 21, FontWeight.Medium, 0.1),
    titleSmall = style(13, 18, FontWeight.Medium, 0.1),
    bodyLarge = style(15, 24, FontWeight.Normal, 0.15),
    bodyMedium = style(14, 21, FontWeight.Normal, 0.15),
    bodySmall = style(12, 18, FontWeight.Normal, 0.2),
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.1),
    labelMedium = style(12, 16, FontWeight.Medium, 0.4),
    labelSmall = style(11, 15, FontWeight.Medium, 0.5)
)
