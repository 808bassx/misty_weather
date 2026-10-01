package com.misty.weather.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Misty Weather Design Tokens Showcase ─────────────────────────────────────
//
// Interactive calibration & visual benchmark component for all Phase C tokens:
//   • Typography (Plus Jakarta Sans + CJK, Temperature scales)
//   • Atmospheric Color Palettes (Light, Dark, Semantics, Weather Accents)
//   • Liquid Glass Tiers (None → Subtle → Regular → Interactive → Floating → Modal)
//   • Spacing & Corner Radii scales
//   • Icon Sizing Grid (16dp → 72dp)
//   • Motion & Calm Easing demonstration
//
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignTokensShowcase(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val isDark = isMistyDark
    val scrollState = rememberScrollState()

    var motionTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue   = if (motionTriggered) 1f else 0f,
        animationSpec = MistyMotion.normalSpec(),
        label         = "token_motion_demo",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(MistyDimensions.Space16),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text  = "Misty Design Tokens",
                    style = MistyType.ScreenTitle,
                    color = MistyColors.TextOnGlass,
                )
                Text(
                    text  = "Phase C Foundation · Plus Jakarta Sans · Liquid Glass",
                    style = MistyType.ForecastTime,
                    color = MistyColors.TextOnGlassSecondary,
                )
            }
            if (onNavigateBack != null) {
                GlassSurface(
                    level    = GlassLevel.Thin,
                    shape    = MistyShapes.Pill,
                    modifier = Modifier.clickable(onClick = onNavigateBack),
                ) {
                    Text(
                        text     = "返回",
                        style    = MistyType.ForecastTime,
                        color    = MistyColors.TextOnGlass,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(MistyDimensions.Space24))

        // ── 1. Typography Benchmark ───────────────────────────────────────────
        ShowcaseSectionTitle("1. Typography (Plus Jakarta Sans + CJK)")

        GlassSurface(
            level    = GlassLevel.Regular,
            shape    = MistyShapes.Large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(MistyDimensions.Space16)) {
                Text(
                    text  = "Temperature Hero (84sp Light)",
                    style = MistyType.CaptionMicro,
                    color = MistyColors.TextOnGlassSecondary,
                )
                Text(
                    text  = "24°",
                    style = MistyType.TemperatureHero,
                    color = MistyColors.TextOnGlass,
                )

                Spacer(Modifier.height(MistyDimensions.Space12))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Bottom,
                ) {
                    Column {
                        Text(
                            text  = "Large (56sp)",
                            style = MistyType.CaptionMicro,
                            color = MistyColors.TextOnGlassSecondary,
                        )
                        Text(
                            text  = "19°",
                            style = MistyType.TemperatureLarge,
                            color = MistyColors.TextOnGlass,
                        )
                    }
                    Column {
                        Text(
                            text  = "Compact (28sp)",
                            style = MistyType.CaptionMicro,
                            color = MistyColors.TextOnGlassSecondary,
                        )
                        Text(
                            text  = "16° / 26°",
                            style = MistyType.TemperatureCompact,
                            color = MistyColors.TextOnGlass,
                        )
                    }
                }

                Spacer(Modifier.height(MistyDimensions.Space16))

                Text(
                    text  = "Location Primary (22sp Medium) + CJK",
                    style = MistyType.CaptionMicro,
                    color = MistyColors.TextOnGlassSecondary,
                )
                Text(
                    text  = "东京 · Tokyo · 晴空晚霞",
                    style = MistyType.LocationPrimary,
                    color = MistyColors.TextOnGlass,
                )

                Spacer(Modifier.height(MistyDimensions.Space8))

                Text(
                    text  = "Condition Primary (17sp Normal)",
                    style = MistyType.CaptionMicro,
                    color = MistyColors.TextOnGlassSecondary,
                )
                Text(
                    text  = "轻度阴天，南风三级，体感舒适",
                    style = MistyType.ConditionPrimary,
                    color = MistyColors.TextOnGlass,
                )

                Spacer(Modifier.height(MistyDimensions.Space8))

                Text(
                    text  = "Detail Row (14sp) & Card Header (12sp SemiBold)",
                    style = MistyType.CaptionMicro,
                    color = MistyColors.TextOnGlassSecondary,
                )
                Text(
                    text  = "体感 23° · 湿度 65% · 能见度 10km · 紫外线 3",
                    style = MistyType.DetailRow,
                    color = MistyColors.TextOnGlassSecondary,
                )
            }
        }

        Spacer(Modifier.height(MistyDimensions.Space24))

        // ── 2. Liquid Glass Tiers Benchmark ───────────────────────────────────
        ShowcaseSectionTitle("2. Liquid Glass Tiers (None → Modal)")

        Column(verticalArrangement = Arrangement.spacedBy(MistyDimensions.Space10)) {
            listOf(
                GlassLevel.None        to "Level 0: None (Pure Atmosphere, 0%)",
                GlassLevel.Subtle      to "Level 1: Subtle (Veil grouping, 8%)",
                GlassLevel.Regular     to "Level 2: Regular (Standard panel, 18%)",
                GlassLevel.Interactive to "Level 3: Interactive (Focus state, 26%)",
                GlassLevel.Floating    to "Level 4: Floating (Bottom nav pill, 36%)",
                GlassLevel.Modal       to "Level 5: Modal (Dialog & sheet focus, 44%)",
            ).forEach { (level, label) ->
                GlassSurface(
                    level    = level,
                    shape    = MistyShapes.Medium,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Row(
                        modifier              = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text  = label,
                            style = MistyType.ForecastTime,
                            color = MistyColors.TextOnGlass,
                        )
                        Text(
                            text  = "α=${(level.fillAlpha * 100).toInt()}%  elev=${level.elevationDp.toInt()}dp",
                            style = MistyType.CaptionMicro,
                            color = MistyColors.TextOnGlassSecondary,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(MistyDimensions.Space24))

        // ── 3. Semantic & Atmospheric Colors Benchmark ────────────────────────
        ShowcaseSectionTitle("3. Semantic Status & Atmospheric Accents")

        GlassSurface(
            level    = GlassLevel.Regular,
            shape    = MistyShapes.Large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(MistyDimensions.Space16)) {
                Text(
                    text  = "Semantic Status Colors (Restrained Soft Tones)",
                    style = MistyType.CardHeader,
                    color = MistyColors.TextOnGlass,
                )
                Spacer(Modifier.height(MistyDimensions.Space8))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ColorSwatch("Success", MistyColors.StatusSuccess)
                    ColorSwatch("Warning", MistyColors.StatusWarning)
                    ColorSwatch("Danger", MistyColors.StatusDanger)
                    ColorSwatch("AccentSky", MistyColors.AccentSky)
                }

                Spacer(Modifier.height(MistyDimensions.Space16))

                Text(
                    text  = "Weather Atmosphere Accents (Subtle Tints & Washes)",
                    style = MistyType.CardHeader,
                    color = MistyColors.TextOnGlass,
                )
                Spacer(Modifier.height(MistyDimensions.Space8))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ColorSwatch("Clear", MistyColors.AtmosphereClear)
                    ColorSwatch("Cloudy", MistyColors.AtmosphereCloudy)
                    ColorSwatch("Rain", MistyColors.AtmosphereRain)
                    ColorSwatch("Snow", MistyColors.AtmosphereSnow)
                    ColorSwatch("Night", MistyColors.AtmosphereNight)
                    ColorSwatch("Sunset", MistyColors.AtmosphereSunset)
                }

                Spacer(Modifier.height(MistyDimensions.Space16))

                Text(
                    text  = "Secondary Screen Deep Navy Gradients (Never Pure Black)",
                    style = MistyType.CardHeader,
                    color = MistyColors.TextOnGlass,
                )
                Spacer(Modifier.height(MistyDimensions.Space8))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ColorSwatch("SecBg", MistyColors.SecondaryBg)
                    ColorSwatch("SecMid", MistyColors.SecondaryBgMid)
                    ColorSwatch("SecDark", MistyColors.SecondaryBgDark)
                    ColorSwatch("DeepNavy", MistyColors.DeepNavy)
                }
            }
        }

        Spacer(Modifier.height(MistyDimensions.Space24))

        // ── 4. Spacing & Corner Radii ─────────────────────────────────────────
        ShowcaseSectionTitle("4. Corner Radii & Spacing Grid")

        GlassSurface(
            level    = GlassLevel.Regular,
            shape    = MistyShapes.Large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(MistyDimensions.Space16)) {
                Text(
                    text  = "Corner Radius Hierarchy",
                    style = MistyType.CardHeader,
                    color = MistyColors.TextOnGlass,
                )
                Spacer(Modifier.height(MistyDimensions.Space10))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MistyDimensions.Space8),
                    verticalArrangement   = Arrangement.spacedBy(MistyDimensions.Space8),
                ) {
                    listOf(
                        "Subtle 8dp" to MistyShapes.Subtle,
                        "Small 12dp" to MistyShapes.Small,
                        "Medium 16dp" to MistyShapes.Medium,
                        "Large 22dp" to MistyShapes.Large,
                        "ExtraLarge 28dp" to MistyShapes.ExtraLarge,
                        "Modal 32dp" to MistyShapes.Modal,
                        "Pill 999dp" to MistyShapes.Pill,
                    ).forEach { (name, shape) ->
                        Box(
                            modifier = Modifier
                                .clip(shape)
                                .background(MistyColors.White.copy(alpha = 0.12f))
                                .border(1.dp, MistyColors.GlassBorderSubtle, shape)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text  = name,
                                style = MistyType.CaptionMicro,
                                color = MistyColors.TextOnGlass,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(MistyDimensions.Space24))

        // ── 5. Icon Scales ────────────────────────────────────────────────────
        ShowcaseSectionTitle("5. Weather & Semantic Icon Scales")

        GlassSurface(
            level    = GlassLevel.Regular,
            shape    = MistyShapes.Large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(MistyDimensions.Space16),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom,
            ) {
                IconScaleSample("XS", MistyDimensions.IconXS)
                IconScaleSample("Small", MistyDimensions.IconSmall)
                IconScaleSample("Forecast", MistyDimensions.WeatherIconForecast)
                IconScaleSample("Medium", MistyDimensions.IconMedium)
                IconScaleSample("Large", MistyDimensions.IconLarge)
                IconScaleSample("Hero", MistyDimensions.WeatherIconHero)
            }
        }

        Spacer(Modifier.height(MistyDimensions.Space24))

        // ── 6. Motion & Calm Easing ───────────────────────────────────────────
        ShowcaseSectionTitle("6. Motion System (Tap to Trigger Calm Settle)")

        GlassSurface(
            level    = GlassLevel.Interactive,
            shape    = MistyShapes.Large,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { motionTriggered = !motionTriggered },
        ) {
            Column(modifier = Modifier.padding(MistyDimensions.Space16)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "MistyMotion.normalSpec() · 280ms Emphasized",
                        style = MistyType.CardHeader,
                        color = MistyColors.TextOnGlass,
                    )
                    Text(
                        text  = if (motionTriggered) "State: B" else "State: A",
                        style = MistyType.CaptionMicro,
                        color = MistyColors.AccentSky,
                    )
                }

                Spacer(Modifier.height(MistyDimensions.Space12))

                // Track bar showing animated progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MistyShapes.Pill)
                        .background(MistyColors.White.copy(alpha = 0.10f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(MistyShapes.Pill)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(MistyColors.AccentSky, MistyColors.StatusSuccess),
                                ),
                            ),
                    )
                }

                Spacer(Modifier.height(MistyDimensions.Space8))

                Text(
                    text  = "Principles: Zero bounce · Calm inertial damping · Weather respiration",
                    style = MistyType.CaptionMicro,
                    color = MistyColors.TextOnGlassSecondary,
                )
            }
        }

        Spacer(Modifier.height(MistyDimensions.Space48))
    }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun ShowcaseSectionTitle(title: String) {
    Text(
        text     = title,
        style    = MistyType.CardHeader.copy(fontWeight = FontWeight.Bold),
        color    = MistyColors.AccentSky,
        modifier = Modifier.padding(bottom = MistyDimensions.Space8, start = 4.dp),
    )
}

@Composable
private fun ColorSwatch(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MistyColors.GlassBorderSubtle, CircleShape),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = label,
            style = MistyType.CaptionMicro,
            color = MistyColors.TextOnGlassSecondary,
        )
    }
}

@Composable
private fun IconScaleSample(label: String, sizeDp: androidx.compose.ui.unit.Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(sizeDp)
                .clip(MistyShapes.Small)
                .background(MistyColors.AccentSky.copy(alpha = 0.20f))
                .border(1.dp, MistyColors.AccentSky.copy(alpha = 0.40f), MistyShapes.Small),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = "☀️",
                style = MistyType.CaptionMicro.copy(fontSize = (sizeDp.value * 0.45f).sp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text  = "${sizeDp.value.toInt()}dp",
            style = MistyType.CaptionMicro,
            color = MistyColors.TextOnGlassSecondary,
        )
        Text(
            text  = label,
            style = MistyType.CaptionMicro.copy(fontSize = 9.sp),
            color = MistyColors.TextOnGlassTertiary,
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Design Tokens Showcase — Dark", showBackground = true, backgroundColor = 0xFF141A28)
@Composable
private fun DesignTokensShowcaseDarkPreview() {
    MistyWeatherTheme(darkTheme = true) {
        DesignTokensShowcase()
    }
}

@Preview(name = "Design Tokens Showcase — Light", showBackground = true, backgroundColor = 0xFF6BAED6)
@Composable
private fun DesignTokensShowcaseLightPreview() {
    MistyWeatherTheme(darkTheme = false) {
        DesignTokensShowcase()
    }
}
