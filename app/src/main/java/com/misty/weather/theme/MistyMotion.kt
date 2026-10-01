package com.misty.weather.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

// ─── Misty Weather Motion Tokens ─────────────────────────────────────────────
//
// Core Principle: "Weather feels alive, UI feels calm."
//
// No rapid bouncing, no elastic overshoot, no attention-seeking animations.
// Interfaces settle with serene inertial deceleration.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * CompositionLocal flag for reduced motion preferences (accessibility).
 * When true, animation durations are minimized or instant.
 */
val LocalMistyReduceMotion = compositionLocalOf { false }

object MistyMotion {

    // ─── Duration Tokens (milliseconds) ───────────────────────────────────────

    /** Instant / zero duration for immediate state cuts or reduced motion. */
    const val DurationInstant: Int = 0

    /** Fast (150ms) — micro-interactions, button presses, selection highlights. */
    const val DurationFast: Int = 150

    /** Normal (280ms) — card expands, dialog appearances, content state switches. */
    const val DurationNormal: Int = 280

    /** Slow (450ms) — navigation cross-fades, screen entrances, hero transitions. */
    const val DurationSlow: Int = 450

    /** Weather (800ms) — atmospheric lighting fades, diurnal shifts, clouds. */
    const val DurationWeather: Int = 800

    // ─── Easing Curves ────────────────────────────────────────────────────────

    /** Standard natural acceleration-deceleration curve. */
    val EasingStandard: Easing = FastOutSlowInEasing

    /** Emphasized deceleration with high damping: fast start, long graceful settle. */
    val EasingEmphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** Calm deceleration for entering elements. */
    val EasingDecelerate: Easing = LinearOutSlowInEasing

    /** Continuous linear pacing for celestial cycles and background particles. */
    val EasingWeather: Easing = LinearEasing

    // ─── Animation Spec Helpers ───────────────────────────────────────────────

    /** Returns standard fast tween spec. */
    fun <T> fastSpec(): TweenSpec<T> = tween(
        durationMillis = DurationFast,
        easing = EasingStandard,
    )

    /** Returns emphasized normal tween spec with calm deceleration. */
    fun <T> normalSpec(): TweenSpec<T> = tween(
        durationMillis = DurationNormal,
        easing = EasingEmphasized,
    )

    /** Returns emphasized slow tween spec for graceful entrance choreography. */
    fun <T> slowSpec(): TweenSpec<T> = tween(
        durationMillis = DurationSlow,
        easing = EasingEmphasized,
    )

    /** Returns atmospheric weather transition spec. */
    fun <T> weatherSpec(): TweenSpec<T> = tween(
        durationMillis = DurationWeather,
        easing = EasingWeather,
    )

    /**
     * Resolves duration based on reduced motion accessibility settings.
     * When [isReduced] is true, returns [DurationInstant].
     */
    fun resolveDuration(baseDuration: Int, isReduced: Boolean): Int {
        return if (isReduced) DurationInstant else baseDuration
    }

    /**
     * Composable helper resolving duration with [LocalMistyReduceMotion].
     */
    @Composable
    fun duration(baseDuration: Int): Int {
        return resolveDuration(baseDuration, LocalMistyReduceMotion.current)
    }
}
