package com.example.mistyweather.weather.scene

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.theme.MistyWeatherTheme
import com.example.mistyweather.weather.animation.rememberWeatherMotion
import com.example.mistyweather.weather.animation.rememberWeatherTransition

// ─── Cinematic Weather Scene ─────────────────────────────────────────────────
//
// Phase 2: Full-screen animated weather environment.
//
// Layer stack (bottom → top):
//   0. SkyLayer      — Vertical gradient sky (zenith → horizon)
//   1. LightLayer    — Sun corona + ambient tint
//   2. CloudLayer    — Three-tier parallax clouds
//   3. ParticleLayer — Rain / snow / fog
//
// All layers are coordinated by:
//   • rememberWeatherTransition() — spring-animated environment lerp
//   • rememberWeatherMotion()     — continuous animation clocks
//
// No BackdropFilter is used in this composable.
// The Glass surface (GlassSurface) sits above this in the parent Box,
// applying a single BackdropFilter at the UI layer — never nested.
//
// CPU/GPU budget:
//   • One InfiniteTransition (6 floats total).
//   • All drawing is Canvas + Brush — no RenderEffect.
//   • Approx 30-50 draw calls per frame — well within 16ms budget on mid-range.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen cinematic weather background.
 *
 * Drop-in replacement for the Phase 1 [WeatherScene]:
 * - Same `weatherType` parameter.
 * - Adds smooth scene transitions.
 * - Adds animated layers (clouds, particles, sun glow).
 *
 * The [modifier] should fill the screen. UI content (Glass cards, text)
 * should be placed *above* this in the same [Box].
 *
 * @param weatherType  Current [WeatherType] — drives which environment renders.
 * @param modifier     Should be [Modifier.fillMaxSize()].
 */
@Composable
fun CinematicWeatherScene(
    weatherType: WeatherType,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    // ── Environment: spring-animated interpolation between WeatherEnvironments ─
    val env by rememberWeatherTransition(weatherType)

    // ── Motion clocks: single InfiniteTransition shared by all layers ─────────
    val motion = rememberWeatherMotion()

    // ── Layer stack ────────────────────────────────────────────────────────────
    Box(modifier = modifier) {

        // Layer 0: Sky gradient
        SkyLayer(
            sky          = env.sky,
            breathAlpha  = motion.ambientBreath,
            modifier     = Modifier.fillMaxSize(),
        )

        // Layer 1: Sun glow + ambient tint
        LightLayer(
            light    = env.light,
            sunPulse = motion.sunPulse,
            modifier = Modifier.fillMaxSize(),
        )

        // Layer 2: Parallax clouds
        CloudLayer(
            clouds      = env.clouds,
            cloudDrift  = motion.cloudDrift,
            cloudShape  = motion.cloudShape,
            modifier    = Modifier.fillMaxSize(),
        )

        // Layer 3: Particles (rain / snow / fog)
        ParticleLayer(
            particles = env.particles,
            fallClock = motion.particleFall,
            fogDrift  = motion.fogDrift,
            modifier  = Modifier.fillMaxSize(),
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Cinematic — Sunny", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewSunny() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Sunny) }
}

@Preview(name = "Cinematic — PartlyCloudy", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewPartlyCloudy() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.PartlyCloudy) }
}

@Preview(name = "Cinematic — Cloudy", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewCloudy() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Cloudy) }
}

@Preview(name = "Cinematic — Rain", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewRain() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Rain) }
}

@Preview(name = "Cinematic — Storm", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewStorm() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Storm) }
}

@Preview(name = "Cinematic — Snow", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewSnow() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Snow) }
}

@Preview(name = "Cinematic — Fog", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewFog() {
    MistyWeatherTheme { CinematicWeatherScene(WeatherType.Fog) }
}
