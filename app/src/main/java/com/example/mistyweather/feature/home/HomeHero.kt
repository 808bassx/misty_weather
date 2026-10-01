package com.example.mistyweather.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mistyweather.core.weather.WeatherPreview
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.theme.GlassLevel
import com.example.mistyweather.theme.GlassSurface
import com.example.mistyweather.theme.LocalMistyReduceMotion
import com.example.mistyweather.theme.MistyColors
import com.example.mistyweather.theme.MistyDimensions
import com.example.mistyweather.theme.MistyMotion
import com.example.mistyweather.theme.MistyShapes
import com.example.mistyweather.theme.MistyType
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Formatters & Pure Logic (Fully Unit Testable) ───────────────────────────

object HomeHeroFormatters {
    /**
     * Formats secondary location label, converting raw technical identifiers like
     * "Current_location" into friendly human-readable text.
     */
    fun formatLocationSubtitle(cityLatin: String): String {
        val trimmed = cityLatin.trim()
        return when {
            trimmed.equals("Current_location", ignoreCase = true) ||
            trimmed.equals("current location", ignoreCase = true) -> "GPS 定位"
            trimmed.isEmpty() -> ""
            else -> trimmed
        }
    }

    /** Formats wide-tracked Latin city title for atmospheric presence. */
    fun formatLocationTitleWide(cityLatin: String, city: String): String {
        val trimmedLatin = cityLatin.trim()
        if (trimmedLatin.isNotEmpty() &&
            !trimmedLatin.equals("Current_location", ignoreCase = true) &&
            !trimmedLatin.equals("current location", ignoreCase = true)
        ) {
            val primaryLatin = trimmedLatin.split(",").first().trim()
            return primaryLatin.uppercase()
        }
        val trimmedCity = city.trim()
        return if (trimmedCity.isNotEmpty()) trimmedCity.uppercase() else "MISTY"
    }

    /** Formats poetic / literary weather phrase keyed to condition, time, and temperature. */
    fun formatLiteraryPhrase(
        condition: String,
        weatherType: WeatherType,
        timeOfDay: TimeOfDay? = null,
    ): String {
        val isNight = timeOfDay == TimeOfDay.Night || timeOfDay == TimeOfDay.Evening
        val isDawnOrMorning = timeOfDay == TimeOfDay.Dawn || timeOfDay == TimeOfDay.Morning
        val isSunset = timeOfDay == TimeOfDay.Sunset

        return when (weatherType) {
            WeatherType.Sunny -> when {
                isNight -> "星汉静谧 · 夜色澄澈"
                isDawnOrMorning -> "晨光清澈 · 空气微冽"
                isSunset -> "晚霞微醺 · 残照温软"
                else -> "明媚日光 · 万物朗阔"
            }
            WeatherType.PartlyCloudy, WeatherType.Cloudy -> when {
                isNight -> "云翳隐星 · 晚风徐徐"
                isDawnOrMorning -> "晨岚渐聚 · 云影漫卷"
                else -> "层云微遮 · 天光温润"
            }
            WeatherType.Rain -> when {
                isNight -> "夜雨淅沥 · 窗凝水汽"
                isDawnOrMorning -> "清晨带露 · 细雨初歇"
                else -> "暮雨轻敲 · 水汽氤氲"
            }
            WeatherType.Storm -> "雷声隐隐 · 骤雨倾盆"
            WeatherType.Snow -> "素雪纷扬 · 银粟初落"
            WeatherType.Fog -> "晓雾弥蒙 · 远黛空濛"
        }
    }

    /**
     * Resolves atmospheric temperature tint color, preventing harsh pure white #FFFFFF
     * and harmonizing with diurnal lighting.
     */
    fun getAtmosphericTemperatureColor(
        weatherType: WeatherType,
        timeOfDay: TimeOfDay? = null,
    ): Color {
        val isNight = timeOfDay == TimeOfDay.Night || timeOfDay == TimeOfDay.Evening
        if (isNight) {
            return Color(0xFFDCE1FF).copy(alpha = 0.90f) // Lunar Cool
        }
        return when (weatherType) {
            WeatherType.Sunny -> Color(0xFFFFF8DC).copy(alpha = 0.95f) // Warm Ivory
            WeatherType.Rain, WeatherType.Storm -> Color(0xFFDCF0FF).copy(alpha = 0.92f) // Misty Cool
            WeatherType.Cloudy, WeatherType.PartlyCloudy, WeatherType.Fog -> Color(0xFFE8EEF5).copy(alpha = 0.92f) // Soft Silver
            WeatherType.Snow -> Color(0xFFF0F9FF).copy(alpha = 0.95f) // Frosty Ice White
        }
    }

    /** Formats numeric temperature with degree symbol. */
    fun formatTemperature(temperature: Int): String = "$temperature°"

    /** Formats glanceable metrics row. */
    fun formatMetrics(feelsLike: Int, humidity: Int): String = "体感 $feelsLike° · 湿度 $humidity%"
}

// ─── Home Hero Composable ─────────────────────────────────────────────────────

/**
 * Phase F: Atmospheric Weather Canvas Hero Stage.
 *
 * Implements "Not a Dashboard, but a Window".
 * Zero container overhead: elements sit directly against the dynamic celestial canvas.
 * Integrates 80dp WeatherOrb, 68sp AtmosphericTemperature, wide-tracked location title,
 * and literary weather phrases.
 */
@Composable
fun HomeHero(
    weather: WeatherPreview,
    modifier: Modifier = Modifier,
    runtimeNotice: String? = null,
    onRetryNotice: (() -> Unit)? = null,
    onCityClick: (() -> Unit)? = null,
) {
    val isReducedMotion = LocalMistyReduceMotion.current

    // Choreographed entrance animatables
    val locationAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val locationOffsetY = remember { Animatable(if (isReducedMotion) 0f else -12f) }

    val weatherAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val weatherOffsetY = remember { Animatable(if (isReducedMotion) 0f else 16f) }

    val metricsAlpha = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val metricsOffsetY = remember { Animatable(if (isReducedMotion) 0f else 12f) }

    LaunchedEffect(weather.city, weather.temperature, weather.weatherType, weather.timeOfDay, isReducedMotion) {
        if (isReducedMotion) {
            locationAlpha.snapTo(1f)
            locationOffsetY.snapTo(0f)
            weatherAlpha.snapTo(1f)
            weatherOffsetY.snapTo(0f)
            metricsAlpha.snapTo(1f)
            metricsOffsetY.snapTo(0f)
        } else {
            // Reset for entry transition
            locationAlpha.snapTo(0f)
            locationOffsetY.snapTo(-10f)
            weatherAlpha.snapTo(0f)
            weatherOffsetY.snapTo(12f)
            metricsAlpha.snapTo(0f)
            metricsOffsetY.snapTo(10f)

            // Step 1: Location header (0ms)
            launch {
                locationAlpha.animateTo(1f, tween(MistyMotion.DurationNormal, easing = MistyMotion.EasingEmphasized))
            }
            launch {
                locationOffsetY.animateTo(0f, tween(MistyMotion.DurationNormal, easing = MistyMotion.EasingEmphasized))
            }

            // Step 2: Hero weather presence (60ms delay)
            launch {
                delay(60)
                weatherAlpha.animateTo(1f, tween(MistyMotion.DurationNormal, easing = MistyMotion.EasingEmphasized))
            }
            launch {
                delay(60)
                weatherOffsetY.animateTo(0f, tween(MistyMotion.DurationNormal, easing = MistyMotion.EasingEmphasized))
            }

            // Step 3: Condition & Metrics (120ms delay)
            launch {
                delay(120)
                metricsAlpha.animateTo(1f, tween(MistyMotion.DurationNormal, easing = MistyMotion.EasingEmphasized))
            }
            launch {
                delay(120)
                metricsOffsetY.animateTo(0f, tween(MistyMotion.DurationNormal, easing = MistyMotion.EasingEmphasized))
            }
        }
    }

    val a11ySummary = "${weather.city}，当前天气${weather.condition}，温度${weather.temperature}度，体感${weather.feelsLike}度，湿度${weather.humidity}百分比"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = a11ySummary
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── 1. Location Header & Literary Phrase ─────────────────────────────
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = locationAlpha.value
                translationY = locationOffsetY.value * density
            },
            contentAlignment = Alignment.Center,
        ) {
            HomeLocationHeader(
                city = weather.city,
                cityLatin = weather.cityLatin,
                condition = weather.condition,
                weatherType = weather.weatherType,
                timeOfDay = weather.timeOfDay,
                onCityClick = onCityClick,
            )
        }

        // ── 2. Runtime Notice Pill (if present) ──────────────────────────────
        if (runtimeNotice != null) {
            Spacer(Modifier.height(MistyDimensions.Space10))
            HomeRuntimeNoticePill(
                notice = runtimeNotice,
                onRetry = onRetryNotice,
            )
        }

        Spacer(Modifier.height(MistyDimensions.Space24))

        // ── 3. Hero Celestial Presence (WeatherOrb + Atmospheric Temperature) ─
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = weatherAlpha.value
                translationY = weatherOffsetY.value * density
            },
            contentAlignment = Alignment.Center,
        ) {
            HomeHeroWeatherPresence(
                weather = weather,
            )
        }

        Spacer(Modifier.height(MistyDimensions.Space10))

        // ── 4. Condition & Glanceable Metrics ────────────────────────────────
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = metricsAlpha.value
                translationY = metricsOffsetY.value * density
            },
            contentAlignment = Alignment.Center,
        ) {
            HomeConditionAndMetrics(
                condition = weather.condition,
                feelsLike = weather.feelsLike,
                humidity = weather.humidity,
            )
        }
    }
}

// ─── Subcomponents ────────────────────────────────────────────────────────────

@Composable
private fun HomeLocationHeader(
    city: String,
    cityLatin: String,
    condition: String,
    weatherType: WeatherType,
    timeOfDay: TimeOfDay?,
    onCityClick: (() -> Unit)?,
) {
    val wideTitle = HomeHeroFormatters.formatLocationTitleWide(cityLatin, city)
    val subtitle = HomeHeroFormatters.formatLocationSubtitle(cityLatin)
    val literaryPhrase = HomeHeroFormatters.formatLiteraryPhrase(condition, weatherType, timeOfDay)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Primary Latin Wide-Tracked Title (e.g. "T O K Y O")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = if (onCityClick != null) {
                Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 24.dp),
                        onClick = onCityClick,
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = "选择城市，当前城市 $city"
                    }
                    .padding(horizontal = MistyDimensions.Space8, vertical = MistyDimensions.Space2)
            } else {
                Modifier.padding(horizontal = MistyDimensions.Space8, vertical = MistyDimensions.Space2)
            },
        ) {
            Text(
                text = wideTitle,
                style = MistyType.LocationTitleWide,
                color = MistyColors.TextOnGlass,
                textAlign = TextAlign.Center,
            )

            if (onCityClick != null) {
                Spacer(Modifier.width(MistyDimensions.Space6))
                Canvas(modifier = Modifier.size(10.dp)) {
                    val w = size.width
                    val h = size.height
                    drawLine(
                        color = MistyColors.TextOnGlassSecondary,
                        start = Offset(0f, h * 0.35f),
                        end = Offset(w * 0.5f, h * 0.75f),
                        strokeWidth = 1.6f.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = MistyColors.TextOnGlassSecondary,
                        start = Offset(w * 0.5f, h * 0.75f),
                        end = Offset(w, h * 0.35f),
                        strokeWidth = 1.6f.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        // Secondary: Chinese localized label (e.g. "东京" or "GPS 定位 · 东京")
        val secondaryLabel = when {
            subtitle.isNotEmpty() && subtitle != city && !subtitle.contains(city, ignoreCase = true) -> "$city · $subtitle"
            else -> city
        }
        Text(
            text = secondaryLabel,
            style = MistyType.LocationSecondary,
            color = MistyColors.TextOnGlassSecondary.copy(alpha = 0.90f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(MistyDimensions.Space4))

        // Tertiary: Literary weather phrase (e.g. "晨光清澈 · 空气微冽")
        Text(
            text = literaryPhrase,
            style = MistyType.LiteraryPhrase,
            color = MistyColors.TextOnGlassSecondary.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HomeRuntimeNoticePill(
    notice: String,
    onRetry: (() -> Unit)?,
) {
    GlassSurface(
        level = GlassLevel.Thin,
        shape = MistyShapes.Pill,
        modifier = Modifier
            .wrapContentHeight()
            .clickable(enabled = onRetry != null) { onRetry?.invoke() },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "⚡ $notice" + if (onRetry != null) " · 点击重试" else "",
                style = MistyType.ForecastTime,
                color = MistyColors.TextOnGlassSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun HomeHeroWeatherPresence(
    weather: WeatherPreview,
) {
    val tempColor = HomeHeroFormatters.getAtmosphericTemperatureColor(weather.weatherType, weather.timeOfDay)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hero Weather Orb (80dp) with celestial aura and volumetric depth
        WeatherOrb(
            weatherType = weather.weatherType,
            timeOfDay   = weather.timeOfDay,
            size        = MistyDimensions.WeatherOrbSize,
        )

        Spacer(Modifier.height(MistyDimensions.Space8))

        // Hero Temperature Display (68sp Light Plus Jakarta Sans with atmospheric tint)
        Text(
            text = HomeHeroFormatters.formatTemperature(weather.temperature),
            style = MistyType.TemperatureHero,
            color = tempColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HomeConditionAndMetrics(
    condition: String,
    feelsLike: Int,
    humidity: Int,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Condition summary badge
        Text(
            text = condition,
            style = MistyType.ConditionPrimary,
            color = MistyColors.TextOnGlassSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(MistyDimensions.Space8))

        // Glanceable metric band (minimal dot-separated inline row)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "体感 $feelsLike°",
                style = MistyType.DetailRow,
                color = MistyColors.TextOnGlassSecondary,
            )

            Spacer(Modifier.width(MistyDimensions.Space10))

            Canvas(modifier = Modifier.size(3.dp)) {
                drawCircle(
                    color = MistyColors.TextOnGlassTertiary,
                    radius = 1.5.dp.toPx(),
                )
            }

            Spacer(Modifier.width(MistyDimensions.Space10))

            Text(
                text = "湿度 $humidity%",
                style = MistyType.DetailRow,
                color = MistyColors.TextOnGlassSecondary,
            )
        }
    }
}
