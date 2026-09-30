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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.example.mistyweather.theme.GlassLevel
import com.example.mistyweather.theme.GlassSurface
import com.example.mistyweather.theme.LocalGlassAmbient
import com.example.mistyweather.theme.LocalMistyReduceMotion
import com.example.mistyweather.theme.MistyColors
import com.example.mistyweather.theme.MistyDimensions
import com.example.mistyweather.theme.MistyMotion
import com.example.mistyweather.theme.MistyShapes
import com.example.mistyweather.theme.MistyType
import com.example.mistyweather.weather.icon.ui.WeatherIcon
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

    /** Formats numeric temperature with degree symbol. */
    fun formatTemperature(temperature: Int): String = "$temperature°"

    /** Formats glanceable metrics row. */
    fun formatMetrics(feelsLike: Int, humidity: Int): String = "体感 $feelsLike° · 湿度 $humidity%"
}

// ─── Home Hero Composable ─────────────────────────────────────────────────────

/**
 * Phase D: Home / Hero Atmospheric Stage.
 *
 * Implements "Weather is the Atmosphere, not a Collection of Cards".
 * Zero container overhead: elements sit directly against the dynamic celestial canvas.
 * Integrates 72dp WeatherIcon, 84sp TemperatureHero, and calm inertial motion choreography.
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

    LaunchedEffect(weather.city, weather.temperature, weather.weatherType, isReducedMotion) {
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
        // ── 1. Location Header ───────────────────────────────────────────────
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
                onCityClick = onCityClick,
            )
        }

        // ── 2. Runtime Notice Pill (if present) ──────────────────────────────
        if (runtimeNotice != null) {
            Spacer(Modifier.height(MistyDimensions.Space12))
            HomeRuntimeNoticePill(
                notice = runtimeNotice,
                onRetry = onRetryNotice,
            )
        }

        Spacer(Modifier.height(MistyDimensions.Space32))

        // ── 3. Hero Weather Presence (Icon + Temperature) ────────────────────
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

        Spacer(Modifier.height(MistyDimensions.Space6))

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
    onCityClick: (() -> Unit)?,
) {
    val formattedSubtitle = HomeHeroFormatters.formatLocationSubtitle(cityLatin)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                    .padding(horizontal = MistyDimensions.Space8, vertical = MistyDimensions.Space4)
            } else {
                Modifier.padding(horizontal = MistyDimensions.Space8, vertical = MistyDimensions.Space4)
            },
        ) {
            Text(
                text = city,
                style = MistyType.LocationPrimary,
                color = MistyColors.TextOnGlass,
                textAlign = TextAlign.Center,
            )

            if (onCityClick != null) {
                Spacer(Modifier.width(MistyDimensions.Space6))
                // Minimalist geometric dropdown chevron
                Canvas(modifier = Modifier.size(10.dp)) {
                    val w = size.width
                    val h = size.height
                    drawLine(
                        color = MistyColors.TextOnGlassSecondary,
                        start = Offset(0f, h * 0.35f),
                        end = Offset(w * 0.5f, h * 0.75f),
                        strokeWidth = 1.8f.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = MistyColors.TextOnGlassSecondary,
                        start = Offset(w * 0.5f, h * 0.75f),
                        end = Offset(w, h * 0.35f),
                        strokeWidth = 1.8f.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        if (formattedSubtitle.isNotEmpty()) {
            Spacer(Modifier.height(MistyDimensions.Space2))
            Text(
                text = formattedSubtitle,
                style = MistyType.LocationSecondary,
                color = MistyColors.TextOnGlassSecondary,
                textAlign = TextAlign.Center,
            )
        }
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
    val ambientColor = LocalGlassAmbient.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hero Weather Icon (72dp) with subtle ambient glow
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(MistyDimensions.WeatherIconHero + MistyDimensions.Space16)
                .drawBehind {
                    // Soft ambient aura harmonizing with the diurnal sky
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ambientColor.copy(alpha = 0.22f),
                                Color.Transparent,
                            ),
                            radius = size.width * 0.55f,
                            center = center,
                        ),
                    )
                },
        ) {
            WeatherIcon(
                weatherType = weather.weatherType,
                modifier = Modifier.size(MistyDimensions.WeatherIconHero),
            )
        }

        Spacer(Modifier.height(MistyDimensions.Space4))

        // Hero Temperature Display (84sp Light Plus Jakarta Sans)
        Text(
            text = HomeHeroFormatters.formatTemperature(weather.temperature),
            style = MistyType.TemperatureHero,
            color = MistyColors.TextOnGlass,
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
        // Literary weather condition phrase (e.g. "毛毛细雨", "晴空万里")
        Text(
            text = condition,
            style = MistyType.ConditionPrimary,
            color = MistyColors.TextOnGlassSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(MistyDimensions.Space12))

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

            Spacer(Modifier.width(MistyDimensions.Space12))

            Canvas(modifier = Modifier.size(3.dp)) {
                drawCircle(
                    color = MistyColors.TextOnGlassTertiary,
                    radius = 1.5.dp.toPx(),
                )
            }

            Spacer(Modifier.width(MistyDimensions.Space12))

            Text(
                text = "湿度 $humidity%",
                style = MistyType.DetailRow,
                color = MistyColors.TextOnGlassSecondary,
            )
        }
    }
}
