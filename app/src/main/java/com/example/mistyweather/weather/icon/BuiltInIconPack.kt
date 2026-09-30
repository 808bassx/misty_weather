package com.example.mistyweather.weather.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

// ─── Misty Default Built-in Icon Pack & Safety Net ───────────────────────────
//
// Phase 4.4-B0: Immutable, always-available built-in weather icons.
//
// Provides:
//   1. BuiltInIconPack: 100% coverage of all WeatherIconKeys.
//   2. High-quality minimalist SVGs for all conditions.
//   3. drawBuiltInGeometricIcon: Pure Canvas geometric fallback when no file/asset is found.
//      Guarantees 0 crash, 0 blank, 0 red-screen under all conditions.
//
// ─────────────────────────────────────────────────────────────────────────────

object BuiltInIconPack {

    const val DEFAULT_PACK_ID = "misty-default"

    val instance: WeatherIconPack = WeatherIconPack(
        id                  = DEFAULT_PACK_ID,
        name                = "Misty Default",
        version             = 1,
        author              = "Misty Weather Team",
        license             = "Apache-2.0",
        description         = "Misty Weather 内置现代简约天气图标，专为液态玻璃与大气光照深度适配。",
        previewPath         = null,
        iconCount           = WeatherIconKey.entries.size,
        supportedKeys       = WeatherIconKey.entries.toSet(),
        source              = IconPackSource.BUILT_IN,
        rootDir             = null,
        iconRelativePaths   = emptyMap(),
        sourceUrl           = "https://github.com/misty-weather/icons",
        licenseUrl          = "https://www.apache.org/licenses/LICENSE-2.0",
    )

    /**
     * Resolves the built-in SVG vector content string for a given [WeatherIconKey].
     */
    fun getSvgContent(key: WeatherIconKey): String = when (key) {
        WeatherIconKey.CLEAR_DAY -> SVG_CLEAR_DAY
        WeatherIconKey.CLEAR_NIGHT -> SVG_CLEAR_NIGHT
        WeatherIconKey.PARTLY_CLOUDY_DAY -> SVG_PARTLY_CLOUDY_DAY
        WeatherIconKey.PARTLY_CLOUDY_NIGHT -> SVG_PARTLY_CLOUDY_NIGHT
        WeatherIconKey.CLOUDY -> SVG_CLOUDY
        WeatherIconKey.OVERCAST -> SVG_OVERCAST
        WeatherIconKey.DRIZZLE -> SVG_DRIZZLE
        WeatherIconKey.RAIN -> SVG_RAIN
        WeatherIconKey.HEAVY_RAIN -> SVG_HEAVY_RAIN
        WeatherIconKey.THUNDERSTORM -> SVG_THUNDERSTORM
        WeatherIconKey.SNOW -> SVG_SNOW
        WeatherIconKey.SLEET -> SVG_SLEET
        WeatherIconKey.FOG -> SVG_FOG
        WeatherIconKey.WIND -> SVG_WIND
        WeatherIconKey.UNKNOWN -> SVG_UNKNOWN
    }

    /**
     * Ultimate safety net: pure Canvas geometric drawing.
     * Invoked when SVG parsing fails or third-tier fallback is triggered.
     */
    fun DrawScope.drawBuiltInGeometricIcon(key: WeatherIconKey, color: Color) {
        val w = size.width
        val h = size.height
        val strokeWidth = (w * 0.08f).coerceAtLeast(1.5f)

        when (key) {
            WeatherIconKey.CLEAR_DAY -> {
                // Sun disc + radial rays
                drawCircle(color = color, radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
                for (angle in 0 until 8) {
                    val rad = Math.toRadians((angle * 45.0)).toFloat()
                    val r1 = w * 0.35f
                    val r2 = w * 0.46f
                    val cx = w * 0.5f
                    val cy = h * 0.5f
                    drawLine(
                        color = color,
                        start = Offset(cx + kotlin.math.cos(rad) * r1, cy + kotlin.math.sin(rad) * r1),
                        end = Offset(cx + kotlin.math.cos(rad) * r2, cy + kotlin.math.sin(rad) * r2),
                        strokeWidth = strokeWidth * 0.7f,
                        cap = StrokeCap.Round,
                    )
                }
            }
            WeatherIconKey.CLEAR_NIGHT -> {
                // Crescent moon
                val path = Path().apply {
                    moveTo(w * 0.65f, h * 0.15f)
                    cubicTo(w * 0.25f, h * 0.20f, w * 0.25f, h * 0.80f, w * 0.65f, h * 0.85f)
                    cubicTo(w * 0.45f, h * 0.70f, w * 0.45f, h * 0.30f, w * 0.65f, h * 0.15f)
                    close()
                }
                drawPath(path = path, color = color)
            }
            WeatherIconKey.PARTLY_CLOUDY_DAY -> {
                // Sun behind cloud
                drawCircle(color = color.copy(alpha = 0.85f), radius = w * 0.20f, center = Offset(w * 0.68f, h * 0.35f))
                drawCloudPuff(w * 0.45f, h * 0.65f, w * 0.40f, h * 0.24f, color)
            }
            WeatherIconKey.PARTLY_CLOUDY_NIGHT -> {
                // Moon behind cloud
                drawCircle(color = color.copy(alpha = 0.75f), radius = w * 0.18f, center = Offset(w * 0.70f, h * 0.34f))
                drawCloudPuff(w * 0.45f, h * 0.65f, w * 0.40f, h * 0.24f, color)
            }
            WeatherIconKey.CLOUDY, WeatherIconKey.OVERCAST -> {
                drawCloudPuff(w * 0.42f, h * 0.48f, w * 0.35f, h * 0.22f, color.copy(alpha = 0.70f))
                drawCloudPuff(w * 0.52f, h * 0.60f, w * 0.42f, h * 0.26f, color)
            }
            WeatherIconKey.DRIZZLE, WeatherIconKey.RAIN, WeatherIconKey.HEAVY_RAIN -> {
                drawCloudPuff(w * 0.50f, h * 0.42f, w * 0.42f, h * 0.24f, color)
                val drops = if (key == WeatherIconKey.HEAVY_RAIN) 3 else 2
                for (i in 0 until drops) {
                    val x = w * (0.35f + i * 0.15f)
                    drawLine(
                        color = color.copy(alpha = 0.85f),
                        start = Offset(x, h * 0.68f),
                        end = Offset(x - w * 0.06f, h * 0.86f),
                        strokeWidth = strokeWidth * 0.8f,
                        cap = StrokeCap.Round,
                    )
                }
            }
            WeatherIconKey.THUNDERSTORM -> {
                drawCloudPuff(w * 0.50f, h * 0.40f, w * 0.42f, h * 0.24f, color)
                val bolt = Path().apply {
                    moveTo(w * 0.52f, h * 0.58f)
                    lineTo(w * 0.42f, h * 0.74f)
                    lineTo(w * 0.52f, h * 0.74f)
                    lineTo(w * 0.46f, h * 0.92f)
                }
                drawPath(path = bolt, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
            WeatherIconKey.SNOW, WeatherIconKey.SLEET -> {
                drawCloudPuff(w * 0.50f, h * 0.42f, w * 0.42f, h * 0.24f, color)
                drawCircle(color = color, radius = strokeWidth * 0.8f, center = Offset(w * 0.38f, h * 0.75f))
                drawCircle(color = color, radius = strokeWidth * 0.8f, center = Offset(w * 0.52f, h * 0.84f))
                drawCircle(color = color, radius = strokeWidth * 0.8f, center = Offset(w * 0.66f, h * 0.75f))
            }
            WeatherIconKey.FOG -> {
                for (i in 0..2) {
                    val y = h * (0.38f + i * 0.16f)
                    drawLine(
                        color = color.copy(alpha = 0.80f - i * 0.15f),
                        start = Offset(w * 0.18f, y),
                        end = Offset(w * 0.82f, y),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            }
            WeatherIconKey.WIND -> {
                val windPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.40f)
                    lineTo(w * 0.70f, h * 0.40f)
                    cubicTo(w * 0.82f, h * 0.40f, w * 0.82f, h * 0.25f, w * 0.70f, h * 0.25f)
                    moveTo(w * 0.22f, h * 0.60f)
                    lineTo(w * 0.60f, h * 0.60f)
                    cubicTo(w * 0.72f, h * 0.60f, w * 0.72f, h * 0.75f, w * 0.60f, h * 0.75f)
                }
                drawPath(path = windPath, color = color, style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round))
            }
            WeatherIconKey.UNKNOWN -> {
                drawCircle(color = color.copy(alpha = 0.5f), radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = strokeWidth))
            }
        }
    }

    private fun DrawScope.drawCloudPuff(cx: Float, cy: Float, rx: Float, ry: Float, color: Color) {
        drawOval(
            color = color,
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2f, ry * 2f),
        )
    }

    // ── High Quality Minimalist SVG Definitions ──────────────────────────────
    private const val SVG_CLEAR_DAY = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="4" stroke-linecap="round"><circle cx="32" cy="32" r="14" fill="currentColor"/><line x1="32" y1="6" x2="32" y2="12"/><line x1="32" y1="52" x2="32" y2="58"/><line x1="6" y1="32" x2="12" y2="32"/><line x1="52" y1="32" x2="58" y2="32"/><line x1="13.6" y1="13.6" x2="17.8" y2="17.8"/><line x1="46.2" y1="46.2" x2="50.4" y2="50.4"/><line x1="13.6" y1="50.4" x2="17.8" y2="46.2"/><line x1="46.2" y1="17.8" x2="50.4" y2="13.6"/></svg>"""

    private const val SVG_CLEAR_NIGHT = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="currentColor"><path d="M42 10C24.3 10 10 24.3 10 42c0 8.1 3 15.5 8 21.1C14 56.4 12 49.5 12 42c0-15.5 12.5-28 28-28 6.5 0 12.6 2.2 17.5 5.9C45.8 12.6 42.1 10 42 10z"/></svg>"""

    private const val SVG_PARTLY_CLOUDY_DAY = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"><circle cx="40" cy="22" r="10" fill="currentColor"/><path d="M46 48H20a12 12 0 01-1.3-23.9A15 15 0 0147 28.5 11 11 0 0146 48z" fill="currentColor" stroke-linejoin="round"/></svg>"""

    private const val SVG_PARTLY_CLOUDY_NIGHT = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"><path d="M44 14a10 10 0 018 10 11 11 0 01-10-10z" fill="currentColor"/><path d="M46 48H20a12 12 0 01-1.3-23.9A15 15 0 0147 28.5 11 11 0 0146 48z" fill="currentColor" stroke-linejoin="round"/></svg>"""

    private const val SVG_CLOUDY = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="currentColor"><path d="M46 44H20a12 12 0 01-1.3-23.9A15 15 0 0147 24.5 11 11 0 0146 44z"/></svg>"""

    private const val SVG_OVERCAST = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="currentColor"><path d="M38 28H18a9 9 0 01-1-17.9A12 12 0 0139 13a8.5 8.5 0 01-1 15z" opacity="0.6"/><path d="M48 50H22a12 12 0 01-1.3-23.9A15 15 0 0149 30.5 11 11 0 0148 50z"/></svg>"""

    private const val SVG_DRIZZLE = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"><path d="M46 38H20a12 12 0 01-1.3-23.9A15 15 0 0147 18.5 11 11 0 0146 38z" fill="currentColor"/><line x1="26" y1="46" x2="22" y2="54"/><line x1="38" y1="46" x2="34" y2="54"/></svg>"""

    private const val SVG_RAIN = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="3.5" stroke-linecap="round"><path d="M46 36H20a12 12 0 01-1.3-23.9A15 15 0 0147 16.5 11 11 0 0146 36z" fill="currentColor"/><line x1="24" y1="44" x2="18" y2="56"/><line x1="34" y1="44" x2="28" y2="56"/><line x1="44" y1="44" x2="38" y2="56"/></svg>"""

    private const val SVG_HEAVY_RAIN = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="4" stroke-linecap="round"><path d="M46 34H20a12 12 0 01-1.3-23.9A15 15 0 0147 14.5 11 11 0 0146 34z" fill="currentColor"/><line x1="22" y1="42" x2="16" y2="58"/><line x1="32" y1="42" x2="26" y2="58"/><line x1="42" y1="42" x2="36" y2="58"/></svg>"""

    private const val SVG_THUNDERSTORM = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"><path d="M46 34H20a12 12 0 01-1.3-23.9A15 15 0 0147 14.5 11 11 0 0146 34z" fill="currentColor"/><polygon points="34,38 24,50 32,50 28,62 42,46 34,46" fill="currentColor"/></svg>"""

    private const val SVG_SNOW = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"><path d="M46 36H20a12 12 0 01-1.3-23.9A15 15 0 0147 16.5 11 11 0 0146 36z" fill="currentColor"/><circle cx="24" cy="48" r="2.5" fill="currentColor"/><circle cx="34" cy="54" r="2.5" fill="currentColor"/><circle cx="44" cy="48" r="2.5" fill="currentColor"/></svg>"""

    private const val SVG_SLEET = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"><path d="M46 36H20a12 12 0 01-1.3-23.9A15 15 0 0147 16.5 11 11 0 0146 36z" fill="currentColor"/><line x1="24" y1="46" x2="20" y2="54"/><circle cx="38" cy="50" r="2.5" fill="currentColor"/></svg>"""

    private const val SVG_FOG = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="4" stroke-linecap="round"><line x1="14" y1="24" x2="50" y2="24"/><line x1="10" y1="34" x2="54" y2="34"/><line x1="16" y1="44" x2="48" y2="44"/></svg>"""

    private const val SVG_WIND = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="4" stroke-linecap="round"><path d="M10 26h32a6 6 0 10-6-6"/><path d="M14 38h38a6 6 0 11-6 6"/></svg>"""

    private const val SVG_UNKNOWN = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="4" stroke-linecap="round"><circle cx="32" cy="32" r="20"/><line x1="32" y1="20" x2="32" y2="34"/><circle cx="32" cy="44" r="2" fill="currentColor"/></svg>"""
}
