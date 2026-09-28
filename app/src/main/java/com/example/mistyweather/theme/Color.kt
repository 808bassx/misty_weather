package com.example.mistyweather.theme

import androidx.compose.ui.graphics.Color

// ─── Misty Weather Color Tokens ──────────────────────────────────────────────
// Design principle: No pure black. Backgrounds are always luminous or deep-navy.
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

    // ── Dark / Night ──────────────────────────────────────────────────────────
    val DeepNavy     = Color(0xFF172033)   // Dark background (never pure black)
    val NightBlue    = Color(0xFF202A3D)   // Dark secondary background
    val BlueGray     = Color(0xFF354158)   // Dark surface
    val IndigoGray   = Color(0xFF2C3550)   // Dark card surface
    val ColdCharcoal = Color(0xFF3A4560)   // Dark elevated surface

    // ── Glass Tokens ──────────────────────────────────────────────────────────
    val GlassWhite10 = Color(0x1AFFFFFF)   // 10% white — ultra-thin
    val GlassWhite15 = Color(0x26FFFFFF)   // 15% white — thin
    val GlassWhite20 = Color(0x33FFFFFF)   // 20% white — regular
    val GlassWhite30 = Color(0x4DFFFFFF)   // 30% white — thick
    val GlassWhite40 = Color(0x66FFFFFF)   // 40% white — floating
    val GlassBorder  = Color(0x1AFFFFFF)   // 10% white border
    val GlassHighlight = Color(0x14FFFFFF) // 8% white top highlight

    // Dark-mode glass
    val GlassDark10  = Color(0x1A2A3550)   // 10% navy — ultra-thin dark
    val GlassDark20  = Color(0x332A3550)   // 20% navy — thin dark
    val GlassDark30  = Color(0x4D2A3550)   // 30% navy — regular dark

    // ── Text ─────────────────────────────────────────────────────────────────
    val TextPrimary       = Color(0xFF1A2030)   // Deep navy text (not black)
    val TextSecondary     = Color(0xFF4A5568)   // Medium gray-blue text
    val TextTertiary      = Color(0xFF7A8CA0)   // Light supporting text
    val TextOnGlass       = Color(0xFFFFFFFF)   // White text on glass
    val TextOnGlassSecondary = Color(0xCCFFFFFF) // 80% white secondary text
    val TextOnGlassTertiary  = Color(0x99FFFFFF) // 60% white tertiary text

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
