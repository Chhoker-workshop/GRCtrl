package com.vanguard.grc.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * GRC VANGUARD brand palette.
 *
 * Identity direction: enterprise-grade, executive-facing, technically serious.
 * Dark navy + graphite structure, muted gold for authority/achievement accents,
 * professional blue for interaction and data, plus a controlled semantic set
 * (risk severity, control status) that stays legible on dark surfaces.
 */

// --- Core brand -----------------------------------------------------------------

/** Deep navy used as the app background in dark mode. */
val Navy900 = Color(0xFF080D16)
val Navy850 = Color(0xFF0B1220)
val Navy800 = Color(0xFF0E1626)
val Navy700 = Color(0xFF132036)
val Navy600 = Color(0xFF1A2A44)

/** Graphite surfaces used for cards, rails and elevated containers. */
val Graphite900 = Color(0xFF111722)
val Graphite800 = Color(0xFF161D2A)
val Graphite700 = Color(0xFF1E2635)
val Graphite600 = Color(0xFF28313F)
val Graphite500 = Color(0xFF3A4553)

/** Muted gold - reserved for authority, milestones and executive-level emphasis. */
val Gold600 = Color(0xFF9C7B33)
val Gold500 = Color(0xFFBE9645)
val Gold400 = Color(0xFFD4AF60)
val Gold300 = Color(0xFFE6CB92)
val Gold900 = Color(0xFF2A2114)

/** Professional blue - interaction, data and navigation emphasis. */
val Blue600 = Color(0xFF2F6FB5)
val Blue500 = Color(0xFF3F87D4)
val Blue400 = Color(0xFF5CA3E8)
val Blue300 = Color(0xFF8CC3F2)
val Blue900 = Color(0xFF11243A)

/** Neutral text ramp for dark surfaces. */
val White = Color(0xFFF4F7FB)
val TextHigh = Color(0xFFE7EDF5)
val TextMid = Color(0xFFA7B4C6)
val TextLow = Color(0xFF73839A)

/** Light-mode neutrals. */
val Paper0 = Color(0xFFFFFFFF)
val Paper50 = Color(0xFFF7F9FC)
val Paper100 = Color(0xFFEDF1F7)
val Paper200 = Color(0xFFDCE3ED)
val Ink900 = Color(0xFF0B1220)
val Ink700 = Color(0xFF28313F)
val Ink500 = Color(0xFF52627A)

// --- Semantic: risk severity ----------------------------------------------------

/** Critical / unacceptable risk. */
val RiskCritical = Color(0xFFD2453F)
val RiskHigh = Color(0xFFE07A2F)
val RiskMedium = Color(0xFFD8A93B)
val RiskLow = Color(0xFF4FA36B)
val RiskInformational = Color(0xFF4C88C4)

// --- Semantic: control & compliance status -------------------------------------

val StatusEffective = Color(0xFF4FA36B)
val StatusPartial = Color(0xFFD8A93B)
val StatusIneffective = Color(0xFFD2453F)
val StatusNotTested = Color(0xFF73839A)
val StatusNa = Color(0xFF5B6B80)

// --- Semantic: competency bands --------------------------------------------------

val CompetencyExpert = Color(0xFFBE9645)
val CompetencyProficient = Color(0xFF3F87D4)
val CompetencyDeveloping = Color(0xFF4C88C4)
val CompetencyNovice = Color(0xFF73839A)
