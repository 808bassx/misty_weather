package com.example.mistyweather.weather.atmosphere

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ─── Color Temperature ────────────────────────────────────────────────────────
//
// Phase 3: Defines the colour temperature and ambient tint for each TimeOfDay.
//
// Diurnal Temperature Cycle:
//   Dawn    (~2800K)  warm golden-peach
//   Morning (~4500K)  neutral-warm daylight
//   Noon    (~6500K)  clean neutral daylight
//   Sunset  (~2200K)  warm amber-orange & lavender
//   Evening (~3200K)  purple-blue dusk
//   Night   (~7500K)  cold deep-blue moonlight
//
// These values modulate the weather environment by tinting sky, light, clouds,
// and glass surfaces additively without flattening the underlying weather cues.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Ambient colour temperature data for a single [TimeOfDay].
 *
 * @param skyTint         Low-alpha overlay tint applied to the sky gradient.
 * @param lightColor      Colour of the sun/moon glow and specular corona.
 * @param shadowColor     Shadow & cloud underside tint (adds atmospheric depth).
 * @param glassAmbient    Adaptive tint for liquid glass surfaces (always translucent).
 * @param starOpacity     Star layer alpha (0 by day, fades in at dusk/night).
 * @param luminosity      Overall scene illumination multiplier (0.5–1.0).
 */
data class ColorTemperature(
    val skyTint: Color,
    val lightColor: Color,
    val shadowColor: Color,
    val glassAmbient: Color,
    val starOpacity: Float,
    val luminosity: Float,
)

/**
 * Canonical colour temperature profiles for each [TimeOfDay].
 */
object ColorTemperatures {

    // ── Dawn (05:00–06:30) — golden-peach horizon, soft pre-daylight ──────────
    val Dawn = ColorTemperature(
        skyTint      = Color(0x1AFFCCA0),   // Warm peach-gold wash
        lightColor   = Color(0xFFFFC880),   // Rising golden sun
        shadowColor  = Color(0xFF606095),   // Soft indigo shadow
        glassAmbient = Color(0x18FFD4A0),   // Warm morning glow on glass
        starOpacity  = 0.30f,               // Stars fading
        luminosity   = 0.75f,
    )

    // ── Morning (06:30–11:00) — neutral-warm, crisp and luminous ─────────────
    val Morning = ColorTemperature(
        skyTint      = Color(0x0CFFF6E0),   // Subtle warm daylight veil
        lightColor   = Color(0xFFFFF4B0),   // Warm morning sunlight
        shadowColor  = Color(0xFF788AA0),   // Soft blue-gray shadows
        glassAmbient = Color(0x14FFFFFF),   // Crisp translucent glass
        starOpacity  = 0f,
        luminosity   = 0.92f,
    )

    // ── Noon (11:00–17:30) — clean neutral daylight, maximum clarity ─────────
    val Noon = ColorTemperature(
        skyTint      = Color(0x00FFFFFF),   // Pristine sky tones without tint
        lightColor   = Color(0xFFFFFFF2),   // Radiant white sunlight
        shadowColor  = Color(0xFF607090),   // Neutral cool shadows
        glassAmbient = Color(0x12FFFFFF),   // Subtle warm white glass
        starOpacity  = 0f,
        luminosity   = 1.0f,
    )

    // ── Sunset (17:30–19:00) — amber-orange & soft lavender ──────────────────
    val Sunset = ColorTemperature(
        skyTint      = Color(0x28FF8030),   // Warm amber-orange wash
        lightColor   = Color(0xFFFF8240),   // Deep golden hour sun
        shadowColor  = Color(0xFF7A3860),   // Warm violet shadow
        glassAmbient = Color(0x22F4A088),   // Subtle warm lavender/amber glass
        starOpacity  = 0f,
        luminosity   = 0.82f,
    )

    // ── Evening (19:00–21:00) — purple-blue dusk, fading twilight ────────────
    val Evening = ColorTemperature(
        skyTint      = Color(0x207860B8),   // Purple-blue dusk tint
        lightColor   = Color(0xFFD88068),   // Fading rose light
        shadowColor  = Color(0xFF283870),   // Deep blue-violet shadows
        glassAmbient = Color(0x1E8870A8),   // Cool purple-gray glass
        starOpacity  = 0.35f,               // Stars appearing
        luminosity   = 0.65f,
    )

    // ── Night (21:00–05:00) — deep cold navy, silvery moonlight ──────────────
    val Night = ColorTemperature(
        skyTint      = Color(0x24101C38),   // Deep navy cool overlay
        lightColor   = Color(0xFFD4E8FF),   // Silvery-blue moonlight
        shadowColor  = Color(0xFF080E1C),   // Deep midnight shadows
        glassAmbient = Color(0x241C2844),   // Deep translucent blue glass
        starOpacity  = 1.0f,                // Brilliant star field
        luminosity   = 0.50f,
    )

    /** Resolve canonical [ColorTemperature] for a [TimeOfDay]. */
    fun forTimeOfDay(time: TimeOfDay): ColorTemperature = when (time) {
        TimeOfDay.Dawn    -> Dawn
        TimeOfDay.Morning -> Morning
        TimeOfDay.Noon    -> Noon
        TimeOfDay.Sunset  -> Sunset
        TimeOfDay.Evening -> Evening
        TimeOfDay.Night   -> Night
    }

    /** Linear interpolation between two [ColorTemperature]s for smooth time blending. */
    fun lerp(a: ColorTemperature, b: ColorTemperature, t: Float): ColorTemperature {
        fun lerpF(va: Float, vb: Float, factor: Float) = va + (vb - va) * factor
        return ColorTemperature(
            skyTint      = lerp(a.skyTint, b.skyTint, t),
            lightColor   = lerp(a.lightColor, b.lightColor, t),
            shadowColor  = lerp(a.shadowColor, b.shadowColor, t),
            glassAmbient = lerp(a.glassAmbient, b.glassAmbient, t),
            starOpacity  = lerpF(a.starOpacity, b.starOpacity, t),
            luminosity   = lerpF(a.luminosity, b.luminosity, t),
        )
    }
}
