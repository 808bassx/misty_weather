package com.example.mistyweather.feature.showcase

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.theme.GlassLevel
import com.example.mistyweather.theme.GlassSurface
import com.example.mistyweather.theme.LocalGlassAmbient
import com.example.mistyweather.theme.MistyColors
import com.example.mistyweather.theme.MistyDimensions
import com.example.mistyweather.theme.MistyShapes
import com.example.mistyweather.theme.MistyType
import com.example.mistyweather.theme.MistyWeatherTheme
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.atmosphere.computeAmbientAtmosphere
import com.example.mistyweather.weather.scene.CinematicWeatherScene

// ─── Weather Showcase & Visual Calibration Screen ────────────────────────────
//
// Phase 3.5: Real-device visual calibration harness.
//
// Enables side-by-side inspection and fine-tuning of:
//   • All 7 Weather Types × 6 Diurnal Times (42 potential atmospheric combinations)
//   • 5 Canonical signature presets (Sunny Morning, Sunny Sunset, Rain Evening, Storm Night, Snow Night)
//   • Adaptive Liquid Glass transmittance, tint absorption, and specular contrast
//   • Night environment verification (deep navy sky, star opacity, translucent cold glass)
//   • Real-time parameter sliders with collapsible HUD to ensure zero visual obstruction
//
// ─────────────────────────────────────────────────────────────────────────────

/** Signature calibration preset descriptor. */
private data class ShowcasePreset(
    val name: String,
    val weather: WeatherType,
    val time: TimeOfDay,
    val description: String,
)

private val signaturePresets = listOf(
    ShowcasePreset("🌅 晴晨", WeatherType.Sunny, TimeOfDay.Morning, "清透晨蓝 · 柔金初升 · 晨光琉璃"),
    ShowcasePreset("🌇 晚霞", WeatherType.Sunny, TimeOfDay.Sunset, "琥珀余晖 · 晚霞紫调 · 暖色玻璃"),
    ShowcasePreset("🌧️ 暮雨", WeatherType.Rain, TimeOfDay.Evening, "暮色青灰 · 湿润低照 · 灰蓝微光"),
    ShowcasePreset("⛈️ 夜暴", WeatherType.Storm, TimeOfDay.Night, "幽暗雷暴 · 厚云蔽星 · 冷夜深邃"),
    ShowcasePreset("❄️ 雪夜", WeatherType.Snow, TimeOfDay.Night, "皎白月光 · 漫天飞雪 · 冰晶蓝调"),
)

@Composable
fun WeatherShowcaseScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedWeather by remember { mutableStateOf(WeatherType.Sunny) }
    var selectedTime by remember { mutableStateOf(TimeOfDay.Morning) }
    var isHudExpanded by remember { mutableStateOf(true) }

    // Calibration overrides
    var glassTintBoost by remember { mutableFloatStateOf(1.0f) }

    // Live atmospheric telemetry computation
    val baseAtmosphere = remember(selectedWeather, selectedTime) {
        computeAmbientAtmosphere(selectedWeather, selectedTime)
    }

    val activeGlassTint = remember(baseAtmosphere.glassAmbient, glassTintBoost) {
        val base = baseAtmosphere.glassAmbient
        val targetAlpha = (base.alpha * glassTintBoost).coerceIn(0.05f, 0.45f)
        base.copy(alpha = targetAlpha)
    }

    CompositionLocalProvider(LocalGlassAmbient provides activeGlassTint) {
        Box(modifier = modifier.fillMaxSize()) {

            // ── Background: Cinematic Weather Scene ───────────────────────────
            CinematicWeatherScene(
                weatherType = selectedWeather,
                timeOfDay   = selectedTime,
                modifier    = Modifier.fillMaxSize(),
            )

            // ── Weather Content Mock Cards for Glass Readability Check ────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(
                        top    = MistyDimensions.Space16,
                        start  = MistyDimensions.Space16,
                        end    = MistyDimensions.Space16,
                        bottom = 240.dp, // Clearance for bottom HUD
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header with navigation back & title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MistyColors.GlassWhite20)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("←", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "视觉校准台",
                        style = MistyType.SectionHeader,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )

                    // HUD Toggle Button
                    Box(
                        modifier = Modifier
                            .clip(MistyShapes.Small)
                            .background(if (isHudExpanded) MistyColors.GlassWhite30 else MistyColors.GlassWhite15)
                            .clickable { isHudExpanded = !isHudExpanded }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isHudExpanded) "收起调色板" else "打开调色板",
                            style = MistyType.ForecastTime,
                            color = Color.White,
                        )
                    }
                }

                Spacer(Modifier.height(MistyDimensions.Space24))

                // Primary Temp & Condition Display
                Text(
                    text = "${selectedWeather.name} · ${selectedTime.name}",
                    style = MistyType.LocationDisplay,
                    color = MistyColors.TextOnGlass,
                )
                Text(
                    text = when (selectedWeather) {
                        WeatherType.Sunny -> "26°"
                        WeatherType.PartlyCloudy -> "23°"
                        WeatherType.Cloudy -> "20°"
                        WeatherType.Rain -> "17°"
                        WeatherType.Storm -> "15°"
                        WeatherType.Snow -> "-2°"
                        WeatherType.Fog -> "14°"
                    },
                    style = MistyType.TemperatureDisplay,
                    color = MistyColors.TextOnGlass,
                )

                Spacer(Modifier.height(MistyDimensions.Space24))

                // Verification Glass Card 1: Atmospheric Telemetry
                GlassSurface(
                    level = GlassLevel.Regular,
                    shape = MistyShapes.Glass,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(MistyDimensions.Space16)) {
                        Text(
                            text = "环境光色度监控",
                            style = MistyType.SectionHeader,
                            color = MistyColors.TextOnGlass,
                        )
                        Spacer(Modifier.height(MistyDimensions.Space8))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            TelemetryPill(
                                label = "太阳位置",
                                value = if (baseAtmosphere.sunPosition.visible) {
                                    "X:${(baseAtmosphere.sunPosition.position.x * 100).toInt()}% Y:${(baseAtmosphere.sunPosition.position.y * 100).toInt()}%"
                                } else "地平线下",
                            )
                            TelemetryPill(
                                label = "繁星可见度",
                                value = "${(baseAtmosphere.starOpacity * 100).toInt()}%",
                            )
                            TelemetryPill(
                                label = "基底照度",
                                value = "${(baseAtmosphere.luminosity * 100).toInt()}%",
                            )
                        }
                    }
                }

                Spacer(Modifier.height(MistyDimensions.Space16))

                // Verification Glass Card 2: 24-Hour Forecast Strip
                GlassSurface(
                    level = GlassLevel.Thin,
                    shape = MistyShapes.Glass,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(MistyDimensions.Space16)) {
                        Text(
                            text = "24 小时微光反射测试",
                            style = MistyType.SectionHeader,
                            color = MistyColors.TextOnGlass,
                        )
                        Spacer(Modifier.height(MistyDimensions.Space12))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(MistyDimensions.Space16),
                        ) {
                            listOf(
                                "06:00" to "Dawn",
                                "09:00" to "Morning",
                                "12:00" to "Noon",
                                "15:00" to "Noon",
                                "18:00" to "Sunset",
                                "20:00" to "Evening",
                                "23:00" to "Night",
                            ).forEach { (timeStr, phaseName) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(timeStr, style = MistyType.ForecastTime, color = MistyColors.TextOnGlassSecondary)
                                    Spacer(Modifier.height(MistyDimensions.Space4))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MistyColors.GlassWhite20),
                                    )
                                    Spacer(Modifier.height(MistyDimensions.Space4))
                                    Text(phaseName, style = MistyType.ForecastTime, color = MistyColors.TextOnGlass)
                                }
                            }
                        }
                    }
                }
            }

            // ── Floating Calibration HUD Panel ────────────────────────────────
            AnimatedVisibility(
                visible = isHudExpanded,
                enter   = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit    = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(MistyDimensions.Space12),
            ) {
                GlassSurface(
                    level = GlassLevel.Floating,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MistyDimensions.Space16),
                    ) {
                        // Preset Pills
                        Text(
                            text = "设计稿签名预设 (1-Click Presets)",
                            style = MistyType.ForecastTime,
                            color = MistyColors.TextOnGlassSecondary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(MistyDimensions.Space8))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(MistyDimensions.Space8),
                        ) {
                            signaturePresets.forEach { preset ->
                                val isSelected = selectedWeather == preset.weather && selectedTime == preset.time
                                Box(
                                    modifier = Modifier
                                        .clip(MistyShapes.Medium)
                                        .background(if (isSelected) MistyColors.GlassWhite40 else MistyColors.GlassWhite15)
                                        .clickable {
                                            selectedWeather = preset.weather
                                            selectedTime = preset.time
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = preset.name,
                                        style = MistyType.ForecastTime,
                                        color = if (isSelected) Color.White else MistyColors.TextOnGlassSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(MistyDimensions.Space12))

                        // Weather Condition Selector
                        Text(
                            text = "天气类型 (Weather)",
                            style = MistyType.ForecastTime,
                            color = MistyColors.TextOnGlassSecondary,
                        )
                        Spacer(Modifier.height(MistyDimensions.Space6))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(MistyDimensions.Space6),
                        ) {
                            WeatherType.values().forEach { weather ->
                                val active = selectedWeather == weather
                                Box(
                                    modifier = Modifier
                                        .clip(MistyShapes.Small)
                                        .background(if (active) MistyColors.GlassWhite30 else MistyColors.GlassWhite10)
                                        .clickable { selectedWeather = weather }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = weather.name,
                                        style = MistyType.NavLabel,
                                        color = if (active) Color.White else MistyColors.TextOnGlassTertiary,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(MistyDimensions.Space10))

                        // TimeOfDay Selector
                        Text(
                            text = "时段环境 (TimeOfDay)",
                            style = MistyType.ForecastTime,
                            color = MistyColors.TextOnGlassSecondary,
                        )
                        Spacer(Modifier.height(MistyDimensions.Space6))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(MistyDimensions.Space6),
                        ) {
                            TimeOfDay.values().forEach { time ->
                                val active = selectedTime == time
                                Box(
                                    modifier = Modifier
                                        .clip(MistyShapes.Small)
                                        .background(if (active) MistyColors.GlassWhite30 else MistyColors.GlassWhite10)
                                        .clickable { selectedTime = time }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = time.name,
                                        style = MistyType.NavLabel,
                                        color = if (active) Color.White else MistyColors.TextOnGlassTertiary,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(MistyDimensions.Space12))

                        // Glass Tint Multiplier Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "玻璃环境反色强度: ${(glassTintBoost * 100).toInt()}%",
                                style = MistyType.ForecastTime,
                                color = MistyColors.TextOnGlassSecondary,
                            )
                        }
                        Slider(
                            value = glassTintBoost,
                            onValueChange = { glassTintBoost = it },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = MistyColors.GlassWhite40,
                                inactiveTrackColor = MistyColors.GlassWhite10,
                            ),
                            modifier = Modifier.height(24.dp),
                        )
                    }
                }
            }

            // Minimized Floating Pill when HUD is collapsed
            if (!isHudExpanded) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(MistyDimensions.Space16)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MistyColors.GlassWhite30)
                        .clickable { isHudExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = "⚙️ 打开校准板 (${selectedWeather.name} · ${selectedTime.name})",
                        style = MistyType.ForecastTime,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(MistyShapes.Small)
            .background(MistyColors.GlassWhite10)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(label, style = MistyType.ForecastTime, color = MistyColors.TextOnGlassSecondary, fontSize = 10.sp)
        Text(value, style = MistyType.ForecastTime, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

// ─── Showcase Previews ────────────────────────────────────────────────────────

@Preview(name = "Showcase — Sunny Morning", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PreviewShowcaseSunnyMorning() {
    MistyWeatherTheme {
        WeatherShowcaseScreen()
    }
}
