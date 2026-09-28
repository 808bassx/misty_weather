package com.example.mistyweather.weather.model

import androidx.compose.ui.graphics.Color
import com.example.mistyweather.core.weather.WeatherType

// ─── Weather Environment Model ───────────────────────────────────────────────
//
// Phase 2: Defines the full visual environment for each WeatherType.
// These data classes drive every layer in the Cinematic Weather Engine:
//   SkyLayer, CloudLayer, LightLayer, ParticleLayer.
//
// No network, no location. All environments are defined statically.
// The API data layer (Phase 4) will map WeatherData → WeatherEnvironment.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Describes the full atmospheric visual state for a given weather condition.
 *
 * @param weatherType     The condition this environment belongs to.
 * @param sky             Gradient colours for the sky background layer.
 * @param clouds          Cloud layer appearance parameters.
 * @param light           Ambient light / sun position parameters.
 * @param particles       Optional particle system descriptor (rain, snow, fog).
 * @param glassAmbient    Tint the glass surface picks up from the environment.
 */
data class WeatherEnvironment(
    val weatherType: WeatherType,
    val sky: SkyEnvironment,
    val clouds: CloudEnvironment,
    val light: LightEnvironment,
    val particles: ParticleEnvironment?,
    val glassAmbient: Color,
)

// ─── Sky ──────────────────────────────────────────────────────────────────────

/**
 * Three-stop vertical gradient that paints the sky background.
 *
 * @param zenith    Colour at the very top of the screen (0%).
 * @param mid       Colour at ~50% height — atmospheric diffusion.
 * @param horizon   Colour at the bottom (100%) — warm/cool horizon haze.
 */
data class SkyEnvironment(
    val zenith: Color,
    val mid: Color,
    val horizon: Color,
)

// ─── Clouds ───────────────────────────────────────────────────────────────────

/**
 * Controls how the cloud layer is rendered and animated.
 *
 * @param density       0f = no clouds, 1f = fully overcast.
 * @param speed         Cloud horizontal drift speed (logical units/s, normalised).
 * @param blobColor     Base colour for cloud blobs.
 * @param shadowColor   Shadow underside colour (adds depth).
 * @param highAltitude  Whether thin cirrus streaks appear at the top.
 */
data class CloudEnvironment(
    val density: Float,
    val speed: Float,
    val blobColor: Color,
    val shadowColor: Color,
    val highAltitude: Boolean = false,
)

// ─── Light ────────────────────────────────────────────────────────────────────

/**
 * Ambient lighting and sun/moon glow parameters.
 *
 * @param sunVisible      Whether the sun disc & glow renders.
 * @param sunPosition     Normalised screen position (0,0)=top-left (1,1)=bottom-right.
 * @param glowColor       Primary radial glow colour emanating from the sun.
 * @param glowRadius      Glow radius as fraction of screen height (0–1).
 * @param ambientTint     Full-screen ambient tint overlay (very low alpha).
 * @param isDaytime       Drives Glass surface tint direction.
 */
data class LightEnvironment(
    val sunVisible: Boolean,
    val sunPosition: Pair<Float, Float>,
    val glowColor: Color,
    val glowRadius: Float,
    val ambientTint: Color,
    val isDaytime: Boolean = true,
)

// ─── Particles ────────────────────────────────────────────────────────────────

/** What kind of particle system to run. */
enum class ParticleKind { Rain, Snow, Fog }

/**
 * Particle system parameters.
 *
 * @param kind        The type of effect.
 * @param density     Particle count per 100dp² (normalised 0–1).
 * @param speed       Fall / drift speed normalised.
 * @param color       Particle stroke / fill colour.
 * @param angle       Particle angle in degrees from vertical (positive = right lean).
 * @param size        Particle length (rain) / radius (snow) in dp-equivalent.
 */
data class ParticleEnvironment(
    val kind: ParticleKind,
    val density: Float,
    val speed: Float,
    val color: Color,
    val angle: Float = 0f,
    val size: Float = 1f,
)

// ─── Canonical Environment Definitions ────────────────────────────────────────

object WeatherEnvironments {

    val Sunny = WeatherEnvironment(
        weatherType = WeatherType.Sunny,
        sky = SkyEnvironment(
            zenith  = Color(0xFF4A8FD4),   // Deep cerulean zenith
            mid     = Color(0xFF7BC4F0),   // Vibrant mid-sky blue
            horizon = Color(0xFFD6EEFF),   // Warm horizon haze
        ),
        clouds = CloudEnvironment(
            density     = 0.05f,
            speed       = 0.018f,
            blobColor   = Color(0xFFF8FBFF),
            shadowColor = Color(0xFFD0E8F8),
        ),
        light = LightEnvironment(
            sunVisible   = true,
            sunPosition  = 0.72f to 0.14f,   // Upper-right, natural angle
            glowColor    = Color(0xFFFFF0A0),
            glowRadius   = 0.55f,
            ambientTint  = Color(0x08FFF0A0), // Barely perceptible warm wash
            isDaytime    = true,
        ),
        particles    = null,
        glassAmbient = Color(0x14FFFFFF),
    )

    val PartlyCloudy = WeatherEnvironment(
        weatherType = WeatherType.PartlyCloudy,
        sky = SkyEnvironment(
            zenith  = Color(0xFF5A9ADA),
            mid     = Color(0xFF8ECBF5),
            horizon = Color(0xFFD9EDFF),
        ),
        clouds = CloudEnvironment(
            density     = 0.38f,
            speed       = 0.025f,
            blobColor   = Color(0xFFF5F9FF),
            shadowColor = Color(0xFFBDD5EE),
            highAltitude = true,
        ),
        light = LightEnvironment(
            sunVisible   = true,
            sunPosition  = 0.65f to 0.18f,
            glowColor    = Color(0xFFFFF8C0),
            glowRadius   = 0.42f,
            ambientTint  = Color(0x06FFF8C0),
            isDaytime    = true,
        ),
        particles    = null,
        glassAmbient = Color(0x18FFFFFF),
    )

    val Cloudy = WeatherEnvironment(
        weatherType = WeatherType.Cloudy,
        sky = SkyEnvironment(
            zenith  = Color(0xFF7A93AA),   // Muted steel-blue zenith
            mid     = Color(0xFF9DAFC0),   // Gray-blue atmosphere
            horizon = Color(0xFFBECDD8),   // Diffused horizon
        ),
        clouds = CloudEnvironment(
            density     = 0.75f,
            speed       = 0.035f,
            blobColor   = Color(0xFFD4DDE6),
            shadowColor = Color(0xFF8FA3BB),
            highAltitude = false,
        ),
        light = LightEnvironment(
            sunVisible   = false,
            sunPosition  = 0.60f to 0.12f,
            glowColor    = Color(0x00FFFFFF),
            glowRadius   = 0f,
            ambientTint  = Color(0x08B0C0D0),
            isDaytime    = true,
        ),
        particles    = null,
        glassAmbient = Color(0x20DDEEFF),
    )

    val Rain = WeatherEnvironment(
        weatherType = WeatherType.Rain,
        sky = SkyEnvironment(
            zenith  = Color(0xFF2E4060),   // Stormy dark zenith
            mid     = Color(0xFF4A6080),   // Heavy cloud atmosphere
            horizon = Color(0xFF6A8095),   // Wet grey horizon
        ),
        clouds = CloudEnvironment(
            density     = 0.92f,
            speed       = 0.048f,
            blobColor   = Color(0xFF607080),
            shadowColor = Color(0xFF374858),
        ),
        light = LightEnvironment(
            sunVisible   = false,
            sunPosition  = 0.50f to 0.05f,
            glowColor    = Color(0x00FFFFFF),
            glowRadius   = 0f,
            ambientTint  = Color(0x0C607090),
            isDaytime    = true,
        ),
        particles = ParticleEnvironment(
            kind    = ParticleKind.Rain,
            density = 0.70f,
            speed   = 0.85f,
            color   = Color(0x55B0CCE0),
            angle   = 8f,
            size    = 1.2f,
        ),
        glassAmbient = Color(0x26B0CCE0),
    )

    val Storm = WeatherEnvironment(
        weatherType = WeatherType.Storm,
        sky = SkyEnvironment(
            zenith  = Color(0xFF1A2535),
            mid     = Color(0xFF2E3E55),
            horizon = Color(0xFF445568),
        ),
        clouds = CloudEnvironment(
            density     = 1.00f,
            speed       = 0.085f,
            blobColor   = Color(0xFF405060),
            shadowColor = Color(0xFF1A2535),
        ),
        light = LightEnvironment(
            sunVisible   = false,
            sunPosition  = 0.50f to 0.0f,
            glowColor    = Color(0x00FFFFFF),
            glowRadius   = 0f,
            ambientTint  = Color(0x12304050),
            isDaytime    = true,
        ),
        particles = ParticleEnvironment(
            kind    = ParticleKind.Rain,
            density = 1.00f,
            speed   = 1.20f,
            color   = Color(0x70A0C0D8),
            angle   = 18f,
            size    = 1.8f,
        ),
        glassAmbient = Color(0x38A0B8CC),
    )

    val Snow = WeatherEnvironment(
        weatherType = WeatherType.Snow,
        sky = SkyEnvironment(
            zenith  = Color(0xFF8898AA),
            mid     = Color(0xFFADBBC8),
            horizon = Color(0xFFDDE6EE),
        ),
        clouds = CloudEnvironment(
            density     = 0.60f,
            speed       = 0.020f,
            blobColor   = Color(0xFFEAEEF3),
            shadowColor = Color(0xFFBCC8D4),
        ),
        light = LightEnvironment(
            sunVisible   = false,
            sunPosition  = 0.50f to 0.10f,
            glowColor    = Color(0x00FFFFFF),
            glowRadius   = 0f,
            ambientTint  = Color(0x08DDEEFF),
            isDaytime    = true,
        ),
        particles = ParticleEnvironment(
            kind    = ParticleKind.Snow,
            density = 0.55f,
            speed   = 0.30f,
            color   = Color(0xCCEEF4FF),
            angle   = 3f,
            size    = 3.0f,
        ),
        glassAmbient = Color(0x22EEF4FF),
    )

    val Fog = WeatherEnvironment(
        weatherType = WeatherType.Fog,
        sky = SkyEnvironment(
            zenith  = Color(0xFF9DAAB5),
            mid     = Color(0xFFBEC8D0),
            horizon = Color(0xFFDDE4E8),
        ),
        clouds = CloudEnvironment(
            density     = 0.30f,
            speed       = 0.008f,
            blobColor   = Color(0xFFE0E6EA),
            shadowColor = Color(0xFFB8C4CC),
        ),
        light = LightEnvironment(
            sunVisible   = false,
            sunPosition  = 0.50f to 0.20f,
            glowColor    = Color(0x20E8F0F8),
            glowRadius   = 0.65f,
            ambientTint  = Color(0x14C8D4DC),
            isDaytime    = true,
        ),
        particles = ParticleEnvironment(
            kind    = ParticleKind.Fog,
            density = 0.80f,
            speed   = 0.06f,
            color   = Color(0x30D0DCE4),
            angle   = 0f,
            size    = 80f,
        ),
        glassAmbient = Color(0x28D8E4EC),
    )

    /** Resolve the environment for any [WeatherType]. */
    fun forType(type: WeatherType): WeatherEnvironment = when (type) {
        WeatherType.Sunny        -> Sunny
        WeatherType.PartlyCloudy -> PartlyCloudy
        WeatherType.Cloudy       -> Cloudy
        WeatherType.Rain         -> Rain
        WeatherType.Storm        -> Storm
        WeatherType.Snow         -> Snow
        WeatherType.Fog          -> Fog
    }
}
