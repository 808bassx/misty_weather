package com.misty.weather.weather.atmosphere

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime

// ─── Time of Day ──────────────────────────────────────────────────────────────
//
// Phase 3: Defines the temporal dimension of the Cinematic Weather Engine.
//
// Role: ONLY describes what temporal phase the current time belongs to.
// Does NOT manage colors, drawing, weather, glass, particles, or animations.
//
// Canonical Time Mapping:
//   05:00 ───── Dawn
//   06:30 ───── Morning
//   11:00 ───── Noon
//   17:30 ───── Sunset
//   19:00 ───── Evening
//   21:00 ───── Night
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The six temporal phases of the day used by the Ambient Time System.
 *
 * Each phase represents a distinct environmental lighting interval.
 */
enum class TimeOfDay {
    /** 05:00–06:30 — Dawn horizon glow, pre-daylight atmosphere. */
    Dawn,

    /** 06:30–11:00 — Clear morning light, soft dew atmosphere. */
    Morning,

    /** 11:00–17:30 — High sun daylight, neutral-high luminosity. */
    Noon,

    /** 17:30–19:00 — Amber golden hour, warm long shadows. */
    Sunset,

    /** 19:00–21:00 — Rose/purple dusk transition into night. */
    Evening,

    /** 21:00–05:00 — Dark navy, cool moonlight, stars visible. */
    Night;

    /**
     * Normalised position within the full 24h day cycle [0.0, 1.0).
     * Used for smooth cross-fades and astronomical calculations.
     */
    val dayFraction: Float get() = when (this) {
        Dawn    -> 0.24f   // ~05:45 midpoint
        Morning -> 0.36f   // ~08:45 midpoint
        Noon    -> 0.59f   // ~14:15 midpoint
        Sunset  -> 0.76f   // ~18:15 midpoint
        Evening -> 0.83f   // ~20:00 midpoint
        Night   -> 0.04f   // ~01:00 midpoint
    }

    companion object {
        /**
         * Derives [TimeOfDay] from 24-hour [hour] and [minute] components.
         * Safe across all Android API levels (API 24+).
         */
        fun from(hour: Int, minute: Int = 0): TimeOfDay {
            val h = hour + minute / 60f
            return when {
                h < 5.0f  -> Night
                h < 6.5f  -> Dawn
                h < 11.0f -> Morning
                h < 17.5f -> Noon
                h < 19.0f -> Sunset
                h < 21.0f -> Evening
                else      -> Night
            }
        }

        /**
         * Derives [TimeOfDay] from a [LocalTime] using the centralized phase boundary table.
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun from(time: LocalTime): TimeOfDay = from(time.hour, time.minute)

        /**
         * Computes progress within the current phase from 24-hour [hour] and [minute] components.
         * Safe across all Android API levels (API 24+).
         */
        fun progressWithin(hour: Int, minute: Int = 0): Float {
            val h = hour + minute / 60f
            return when {
                h < 5.0f  -> (h + 3.0f) / 8.0f                // 21:00 to 05:00 (8h total)
                h < 6.5f  -> (h - 5.0f) / 1.5f                // 05:00 to 06:30 (1.5h)
                h < 11.0f -> (h - 6.5f) / 4.5f                // 06:30 to 11:00 (4.5h)
                h < 17.5f -> (h - 11.0f) / 6.5f               // 11:00 to 17:30 (6.5h)
                h < 19.0f -> (h - 17.5f) / 1.5f               // 17:30 to 19:00 (1.5h)
                h < 21.0f -> (h - 19.0f) / 2.0f               // 19:00 to 21:00 (2.0h)
                else      -> (h - 21.0f) / 8.0f               // 21:00 to 24:00 (first part of 8h)
            }.coerceIn(0f, 1f)
        }

        /**
         * Computes a smooth continuous interpolation weight within the current phase [0.0, 1.0].
         */
        @RequiresApi(Build.VERSION_CODES.O)
        fun progressWithin(time: LocalTime): Float = progressWithin(time.hour, time.minute)

        /** The phase that follows a given [TimeOfDay] in the diurnal cycle. */
        fun TimeOfDay.next(): TimeOfDay = when (this) {
            Dawn    -> Morning
            Morning -> Noon
            Noon    -> Sunset
            Sunset  -> Evening
            Evening -> Night
            Night   -> Dawn
        }
    }
}
