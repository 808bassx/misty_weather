package com.misty.weather.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ─── Misty Weather Color Tokens ──────────────────────────────────────────────
// Design principle: Atmospheric presence. No pure black (#000000 or #0B0D10)
// as UI backgrounds. Dark surfaces lean into deep atmospheric navy and blue-gray.
// ─────────────────────────────────────────────────────────────────────────────

object MistyColors {

    // ── Whites & Lights ───────────────────────────────────────────────────────
    val White        = Color(0xFFFFFFFF)
    val CloudWhite   = Color(0xFFF7F8FC)
    val MistBlue     = Color(0xFFE9F0FA)
    val SkyBlue      = Color(0xFFDDEBFF)
    val Lavender     = Color(0xFFE9E7FF)
    val PearlGray    = Color(0xFFEDF0F5)

    // ── Sky Gradients (Light) ─────────────────────────────────────────────────
    val SkyTop       = Color(0xFF6BAED6)   // Crisp zenith blue
    val SkyMid       = Color(0xFFADD8F7)   // Soft mid-sky
    val SkyHorizon   = Color(0xFFE8F4FE)   // Warm horizon haze

    // ── Sunny Palette ─────────────────────────────────────────────────────────
    val SunGlow      = Color(0xFFFFF4C2)   // Warm sun halo
    val SunWarm      = Color(0xFFFFE0A0)   // Amber warm light
    val SunCore      = Color(0xFFFFF9EC)   // Near-white sun core

    // ── Cloudy Palette ────────────────────────────────────────────────────────
    val CloudGray    = Color(0xFFB8C5D6)   // Overcast cloud body
    val CloudLight   = Color(0xFFD9E5F0)   // Bright cloud edge
    val CloudShadow  = Color(0xFF8FA3BB)   // Cloud underside

    // ── Rain Palette ──────────────────────────────────────────────────────────
    val RainSky      = Color(0xFF4A6080)   // Heavy rain sky top
    val RainMid      = Color(0xFF6B82A0)   // Rain mid atmosphere
    val RainAmbient  = Color(0xFF8FA8C0)   // Wet ambient light
    val RainDrop     = Color(0xAAB0CCE0)   // Semi-transparent raindrop

    // ── Dark / Atmospheric Night (Never Pure Black #000000) ───────────────────
    val DeepNavy     = Color(0xFF172033)   // Primary dark canvas background
    val NightNavy    = Color(0xFF131A29)   // Deep ambient floor (never pure black)
    val NightBlue    = Color(0xFF202A3D)   // Dark secondary background
    val BlueGray     = Color(0xFF354158)   // Dark surface
    val IndigoGray   = Color(0xFF2C3550)   // Dark card surface
    val ColdCharcoal = Color(0xFF3A4560)   // Dark elevated surface

    // Secondary screen backgrounds (settings, modal galleries)
    val SecondaryBg     = Color(0xFF1E2638) // Top gradient anchor
    val SecondaryBgMid  = Color(0xFF141A28) // Mid gradient anchor
    val SecondaryBgDark = Color(0xFF0F1420) // Bottom gradient anchor (deep atmospheric navy)

    // ── Glass Tokens ──────────────────────────────────────────────────────────
    val GlassWhite10 = Color(0x1AFFFFFF)   // 10% white — ultra-thin / subtle
    val GlassWhite15 = Color(0x26FFFFFF)   // 15% white — thin
    val GlassWhite20 = Color(0x33FFFFFF)   // 20% white — regular
    val GlassWhite30 = Color(0x4DFFFFFF)   // 30% white — thick
    val GlassWhite40 = Color(0x66FFFFFF)   // 40% white — floating
    val GlassBorder  = Color(0x1AFFFFFF)   // 10% white border (legacy alias)
    val GlassHighlight = Color(0x14FFFFFF) // 8% white top specular highlight

    // Glass Border Hierarchy
    val GlassBorderSubtle  = Color(0x14FFFFFF) // ~8% white hairline
    val GlassBorderRegular = Color(0x1FFFFFFF) // ~12% white standard
    val GlassBorderStrong  = Color(0x2EFFFFFF) // ~18% white interactive focus

    // Dark-mode glass
    val GlassDark10  = Color(0x1A2A3550)   // 10% navy — ultra-thin dark
    val GlassDark20  = Color(0x332A3550)   // 20% navy — thin dark
    val GlassDark30  = Color(0x4D2A3550)   // 30% navy — regular dark

    // ── Semantic Status & Action Colors (Restrained, Soft Tones) ──────────────
    val AccentSky      = Color(0xFF64B5F6) // Calm sky cyan for primary CTAs and active states
    val StatusSuccess  = Color(0xFF81C784) // Soft sage green for active badges and healthy sync
    val StatusWarning  = Color(0xFFFFB74D) // Solar warm amber for fallbacks and notices
    val StatusDanger   = Color(0xFFE57373) // Muted coral red for delete and connection failures

    // ── Scrim Tokens (Atmospheric Darkening, Never Pure Black) ─────────────────
    val ScrimDark      = Color(0xB30E1422) // ~70% deep navy scrim for modal focus
    val ScrimLight     = Color(0x33FFFFFF) // ~20% light mist scrim

    // ── Weather Atmospheric Accents (Subtle Tints & Glows) ───────────────────
    val AtmosphereClear   = Color(0xFFFFD54F) // Warm solar gold
    val AtmosphereCloudy  = Color(0xFFB0BEC5) // Slate cloud silver
    val AtmosphereRain    = Color(0xFF4FC3F7) // Crisp rain cyan
    val AtmosphereSnow    = Color(0xFFE0F7FA) // Chilled crystal white
    val AtmosphereNight   = Color(0xFF7986CB) // Starlight violet-indigo
    val AtmosphereSunset  = Color(0xFFFF8A65) // Rose-amber glow

    // ── Text Tokens ───────────────────────────────────────────────────────────
    val TextPrimary          = Color(0xFF1A2030)   // Deep navy text (not pure black)
    val TextSecondary        = Color(0xFF4A5568)   // Medium gray-blue text
    val TextTertiary         = Color(0xFF7A8CA0)   // Light supporting text
    val TextOnGlass          = Color(0xFFFFFFFF)   // White text on glass
    val TextOnGlassSecondary = Color(0xCCFFFFFF)   // 80% white secondary text
    val TextOnGlassTertiary  = Color(0x99FFFFFF)   // 60% white tertiary text

    // Dark-mode text
    val TextPrimaryDark   = Color(0xFFE8EDF5)   // Near-white for dark backgrounds
    val TextSecondaryDark = Color(0xFFB0BFD0)   // Muted light for dark backgrounds

    // ── Material 3 Color Scheme Mappings ─────────────────────────────────────
    // Light scheme
    val LightPrimary         = Color(0xFF4A7FBB)
    val LightOnPrimary       = Color(0xFFFFFFFF)
    val LightBackground      = CloudWhite
    val LightOnBackground    = TextPrimary
    val LightSurface         = Color(0xFFF0F4FB)
    val LightOnSurface       = TextPrimary

    // Dark scheme
    val DarkPrimary          = Color(0xFF90C3F0)
    val DarkOnPrimary        = DeepNavy
    val DarkBackground       = DeepNavy
    val DarkOnBackground     = TextPrimaryDark
    val DarkSurface          = NightBlue
    val DarkOnSurface        = TextPrimaryDark
}

// ─── Semantic Palette Contract ───────────────────────────────────────────────

@Immutable
data class MistyColorPalette(
    val background: Color,
    val backgroundElevated: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val glassTint: Color,
    val glassHighlight: Color,
    val glassBorder: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val isDark: Boolean,
)

val MistyLightPalette = MistyColorPalette(
    background         = MistyColors.CloudWhite,
    backgroundElevated = MistyColors.MistBlue,
    surface            = MistyColors.LightSurface,
    surfaceElevated    = MistyColors.White,
    primaryText        = MistyColors.TextPrimary,
    secondaryText      = MistyColors.TextSecondary,
    tertiaryText       = MistyColors.TextTertiary,
    glassTint          = MistyColors.White,
    glassHighlight     = MistyColors.GlassHighlight,
    glassBorder        = MistyColors.GlassBorderSubtle,
    accent             = MistyColors.AccentSky,
    success            = MistyColors.StatusSuccess,
    warning            = MistyColors.StatusWarning,
    error              = MistyColors.StatusDanger,
    isDark             = false,
)

val MistyDarkPalette = MistyColorPalette(
    background         = MistyColors.DeepNavy,
    backgroundElevated = MistyColors.NightNavy,
    surface            = MistyColors.NightBlue,
    surfaceElevated    = MistyColors.BlueGray,
    primaryText        = MistyColors.TextPrimaryDark,
    secondaryText      = MistyColors.TextSecondaryDark,
    tertiaryText       = MistyColors.ColdCharcoal,
    glassTint          = MistyColors.NightBlue,
    glassHighlight     = MistyColors.GlassHighlight,
    glassBorder        = MistyColors.GlassBorderSubtle,
    accent             = MistyColors.AccentSky,
    success            = MistyColors.StatusSuccess,
    warning            = MistyColors.StatusWarning,
    error              = MistyColors.StatusDanger,
    isDark             = true,
)
