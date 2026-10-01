package com.example.mistyweather.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mistyweather.R

// ─── Misty Weather Typography System ─────────────────────────────────────────
//
// Core Typeface: Plus Jakarta Sans (SIL Open Font License 1.1)
// CJK Fallback: Android System CJK / Noto Sans SC (harmonized optical baseline)
//
// Design Principle: Airy, poetic, architectural numerals. Temperature Hero
// uses negative letter tracking (-3sp) to prevent wide digit splay.
// ─────────────────────────────────────────────────────────────────────────────

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_light, FontWeight.Light),
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

object MistyType {

    // ── Numeric Typography (Temperature Scales - Design Hypothesis) ───────────

    /** Primary hero temperature display: "24°" — architectural, balanced, airy. */
    val TemperatureHero = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 68.sp,
        lineHeight    = 68.sp,
        letterSpacing = (-2.5).sp,
    )

    /** Large secondary temperature or prominent metric. */
    val TemperatureLarge = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 56.sp,
        lineHeight    = 60.sp,
        letterSpacing = (-2).sp,
    )

    /** Compact temperature (e.g. min/max range, small card summaries). */
    val TemperatureCompact = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 28.sp,
        lineHeight    = 32.sp,
        letterSpacing = (-1).sp,
    )

    // Legacy alias to preserve backward compatibility with existing composables
    val TemperatureDisplay = TemperatureHero

    // ── Content Typography (Location, Weather Condition, Details) ─────────────

    /** Wide-tracked atmospheric location title: "T O K Y O" */
    val LocationTitleWide = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = 4.5.sp,
    )

    /** Primary location title: "东京", "Paris" — confident without shouting. */
    val LocationPrimary = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = (-0.3).sp,
    )

    /** Secondary location subtitle: "Current location", "日本" */
    val LocationSecondary = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.2.sp,
    )

    /** Literary weather phrase: "晨光清澈 · 空气微冽" */
    val LiteraryPhrase = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.8.sp,
    )

    // Legacy alias
    val LocationDisplay = LocationPrimary

    /** Weather condition description: "晴空晚霞", "连绵细雨" */
    val ConditionPrimary = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 17.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp,
    )

    // Legacy alias
    val ConditionLabel = ConditionPrimary

    /** Inline metric row: "体感 22° · 湿度 71%" */
    val DetailRow = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.15.sp,
    )

    // Legacy alias
    val DetailLabel = DetailRow

    // ── Forecast & Temporal Typography ────────────────────────────────────────

    /** Hourly forecast time label: "16:00", "现在" */
    val ForecastTime = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.1.sp,
    )

    /** Hourly forecast temperature: "23°" */
    val ForecastTemp = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
        letterSpacing = (-0.2).sp,
    )

    // ── Navigation & Section Headers ──────────────────────────────────────────

    /** Section header in glass cards: "今天", "已安装图标包" */
    val CardHeader = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.8.sp,
    )

    // Legacy alias
    val SectionHeader = CardHeader

    /** Top bar screen title: "天气图标管理", "城市管理" */
    val ScreenTitle = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = (-0.2).sp,
    )

    /** Button label on glass pills */
    val ButtonLabel = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.1.sp,
    )

    /** Floating navigation bar label: "天气", "地点", "设置" */
    val NavLabel = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.3.sp,
    )

    /** Micro captions, metadata timestamps, license text */
    val CaptionMicro = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.2.sp,
    )
}

/** Material 3 Typography harmonized with Plus Jakarta Sans scale. */
val MistyM3Typography = Typography(
    displayLarge = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 57.sp,
        lineHeight    = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Light,
        fontSize      = 45.sp,
        lineHeight    = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily    = PlusJakartaSansFamily,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// Legacy alias so existing code compiles unmodified.
val Typography = MistyM3Typography
