package com.example.mistyweather.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mistyweather.core.weather.ForecastSlot
import com.example.mistyweather.core.weather.PreviewForecastSlots
import com.example.mistyweather.core.weather.PreviewWeatherTokyo
import com.example.mistyweather.core.weather.WeatherPreview
import com.example.mistyweather.core.weather.WeatherType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mistyweather.weather.domain.WeatherMapper
import com.example.mistyweather.weather.domain.WeatherRenderState
import com.example.mistyweather.weather.viewmodel.WeatherUiState
import com.example.mistyweather.weather.viewmodel.WeatherViewModel
import com.example.mistyweather.weather.scene.CinematicWeatherScene
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
import kotlin.math.cos
import kotlin.math.sin

// ─── Home Screen ─────────────────────────────────────────────────────────────
//
// Visual layer stack (bottom → top):
//   Layer 0 — CinematicWeatherScene  (sky + light + clouds + particles)
//   Layer 1 — Ambient gradient wash  (subtle bottom fade into glass)
//   Layer 2 — Glass surfaces         (forecast card, nav bar)
//   Layer 3 — Text / Icons
//   Layer 4 — Floating Navigation
//
// Phase 2: Animated cinematic engine. Static preview data only.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Home Screen entry point.
 *
 * In Phase 4, wired to [WeatherViewModel] and [WeatherRepository].
 * Pulls domain data and frozen [WeatherRenderState] into [HomeScreenContent].
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.provideFactory()),
    timeOfDay: TimeOfDay? = null,
    onOpenShowcase: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        WeatherUiState.Loading -> {
            HomeScreenContent(
                weather        = PreviewWeatherTokyo,
                forecast       = PreviewForecastSlots,
                timeOfDay      = timeOfDay,
                onOpenShowcase = onOpenShowcase,
                modifier       = modifier,
            )
        }
        is WeatherUiState.Success -> {
            HomeScreenContent(
                weather        = state.preview,
                forecast       = state.forecastSlots,
                renderState    = state.renderState,
                timeOfDay      = timeOfDay ?: state.activeTimeOfDay,
                onOpenShowcase = onOpenShowcase,
                modifier       = modifier,
            )
        }
        is WeatherUiState.Error -> {
            HomeScreenContent(
                weather        = state.lastKnownPreview ?: PreviewWeatherTokyo,
                forecast       = state.lastKnownForecastSlots ?: PreviewForecastSlots,
                renderState    = state.lastKnownRenderState,
                timeOfDay      = timeOfDay ?: state.activeTimeOfDay,
                runtimeNotice  = state.error.userFacingMessage,
                onRetryNotice  = if (state.canRetry) { { viewModel.refreshWeather() } } else null,
                onOpenShowcase = onOpenShowcase,
                modifier       = modifier,
            )
        }
    }
}

/**
 * Stateless Home Screen content — accepts weather, forecast, and optional [renderState]
 * so Compose Previews can inject arbitrary environmental states without a ViewModel.
 */
@Composable
internal fun HomeScreenContent(
    weather: WeatherPreview,
    forecast: List<ForecastSlot>,
    renderState: WeatherRenderState? = null,
    timeOfDay: TimeOfDay? = null,
    runtimeNotice: String? = null,
    onRetryNotice: (() -> Unit)? = null,
    onOpenShowcase: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Determine active diurnal phase (caller override > weather model > Noon static demo)
    val activeTime = timeOfDay ?: weather.timeOfDay ?: TimeOfDay.Noon

    // Compute or reuse frozen atmosphere & render state
    val activeRenderState = renderState ?: remember(weather.weatherType, activeTime) {
        WeatherMapper.toRenderState(weather.weatherType, activeTime)
    }

    // Adaptive Glass Tint: All nested GlassSurfaces inherit atmosphere.glassAmbient
    CompositionLocalProvider(LocalGlassAmbient provides activeRenderState.atmosphere.glassAmbient) {
        Box(modifier = modifier.fillMaxSize()) {

            // ── Layer 0: Cinematic Weather Background (Frozen Contract) ──────
            CinematicWeatherScene(
                state    = activeRenderState,
                modifier = Modifier.fillMaxSize(),
            )

            // ── Layer 1: Bottom ambient wash — softens the transition to glass ────
            AmbientBottomWash()

            // ── Layer 2 & 3: Scrollable weather content ───────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top        = MistyDimensions.Space48,
                        start      = MistyDimensions.Space16,
                        end        = MistyDimensions.Space16,
                        // Extra bottom padding so content clears the floating nav.
                        bottom     = MistyDimensions.Space48 + MistyDimensions.NavBarHeight +
                                     MistyDimensions.NavBarBottomInset,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Location header
                LocationHeader(
                    city      = weather.city,
                    cityLatin = weather.cityLatin,
                )

                // Runtime offline / error notification pill
                if (runtimeNotice != null) {
                    Spacer(Modifier.height(MistyDimensions.Space12))
                    GlassSurface(
                        level    = GlassLevel.Thin,
                        shape    = MistyShapes.Pill,
                        modifier = Modifier
                            .wrapContentHeight()
                            .clickable(enabled = onRetryNotice != null) { onRetryNotice?.invoke() },
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text      = "⚡ $runtimeNotice" + if (onRetryNotice != null) " · 点击重试" else "",
                                style     = MistyType.ForecastTime,
                                color     = MistyColors.TextOnGlassSecondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(MistyDimensions.Space48))

                // Primary temperature display
                TemperatureDisplay(
                    temperature = weather.temperature,
                    condition   = weather.condition,
                )

                Spacer(Modifier.height(MistyDimensions.Space20))

                // Secondary detail row
                WeatherDetailsRow(
                    feelsLike = weather.feelsLike,
                    humidity  = weather.humidity,
                )

                Spacer(Modifier.height(MistyDimensions.Space40))

                // Hourly forecast glass card
                ForecastGlassCard(slots = forecast)
            }

            // ── Layer 4: Floating Glass Navigation ────────────────────────────────
            FloatingGlassNavigation(
                onOpenShowcase = onOpenShowcase,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(
                        horizontal = MistyDimensions.NavBarHorizontal,
                        vertical   = MistyDimensions.NavBarBottomInset,
                    ),
            )
        }
    }
}

// ─── Layer 1: Ambient Wash ────────────────────────────────────────────────────

@Composable
private fun AmbientBottomWash() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    MistyColors.DeepNavy.copy(alpha = 0.08f),
                ),
                startY = 0f,
                endY   = size.height,
            ),
        )
    }
}

// ─── Location Header ──────────────────────────────────────────────────────────

@Composable
private fun LocationHeader(
    city: String,
    cityLatin: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "$city ($cityLatin)"
        },
    ) {
        Text(
            text      = city,
            style     = MistyType.LocationDisplay,
            color     = MistyColors.TextOnGlass,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MistyDimensions.Space4))
        Text(
            text      = cityLatin,
            style     = MistyType.DetailLabel,
            color     = MistyColors.TextOnGlassSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Temperature Display ──────────────────────────────────────────────────────

@Composable
private fun TemperatureDisplay(
    temperature: Int,
    condition: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "当前温度 $temperature 度，$condition"
        },
    ) {
        // Large temperature — 84sp Light, cinematic
        Text(
            text      = "$temperature°",
            style     = MistyType.TemperatureDisplay,
            color     = MistyColors.TextOnGlass,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MistyDimensions.Space8))
        Text(
            text      = condition,
            style     = MistyType.ConditionLabel,
            color     = MistyColors.TextOnGlassSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Weather Details Row ──────────────────────────────────────────────────────

@Composable
private fun WeatherDetailsRow(feelsLike: Int, humidity: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "体感温度 $feelsLike 度，湿度 $humidity%"
        },
    ) {
        Text(
            text  = "体感 $feelsLike°",
            style = MistyType.DetailLabel,
            color = MistyColors.TextOnGlassSecondary,
        )
        Spacer(Modifier.width(MistyDimensions.Space16))
        // Dot divider
        Canvas(modifier = Modifier.size(4.dp)) {
            drawCircle(
                color  = MistyColors.TextOnGlassTertiary,
                radius = 2.dp.toPx(),
            )
        }
        Spacer(Modifier.width(MistyDimensions.Space16))
        Text(
            text  = "湿度 $humidity%",
            style = MistyType.DetailLabel,
            color = MistyColors.TextOnGlassSecondary,
        )
    }
}

// ─── Forecast Glass Card ──────────────────────────────────────────────────────

@Composable
private fun ForecastGlassCard(slots: List<ForecastSlot>) {
    GlassSurface(
        level    = GlassLevel.Regular,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = MistyDimensions.Space16,
                vertical   = MistyDimensions.Space16,
            ),
        ) {
            // Section header
            Text(
                text  = "今天",
                style = MistyType.SectionHeader,
                color = MistyColors.TextOnGlassSecondary,
            )

            Spacer(Modifier.height(MistyDimensions.Space12))

            // Divider — 1px glass border line
            Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                drawLine(
                    color       = MistyColors.GlassBorder,
                    start       = Offset(0f, 0f),
                    end         = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            Spacer(Modifier.height(MistyDimensions.Space12))

            // Hourly slots
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                slots.forEach { slot ->
                    ForecastSlotItem(slot = slot)
                }
            }
        }
    }
}

@Composable
private fun ForecastSlotItem(slot: ForecastSlot) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(MistyDimensions.ForecastItemMinWidth)
            .semantics {
                contentDescription = "${slot.label} ${slot.temperature}度 ${slot.weatherType.name}"
            },
    ) {
        // Time label
        Text(
            text      = slot.label,
            style     = MistyType.ForecastTime,
            color     = MistyColors.TextOnGlassSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(MistyDimensions.Space8))

        // Weather type icon (drawn on Canvas — no emoji)
        WeatherIconCanvas(
            weatherType = slot.weatherType,
            modifier    = Modifier.size(22.dp),
        )

        Spacer(Modifier.height(MistyDimensions.Space8))

        // Temperature
        Text(
            text      = "${slot.temperature}°",
            style     = MistyType.ForecastTemp,
            color     = MistyColors.TextOnGlass,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Minimal canvas-drawn weather icon — geometric, not emoji, not illustration.
 * Sun = circle. Cloud = overlapping ellipses. Rain = circle + diagonal lines.
 */
@Composable
private fun WeatherIconCanvas(
    weatherType: WeatherType,
    modifier: Modifier = Modifier,
) {
    val iconColor = MistyColors.TextOnGlass

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (weatherType) {
            WeatherType.Sunny -> {
                // Circle sun
                drawCircle(color = iconColor.copy(alpha = 0.90f), radius = w * 0.26f)
                // 8 short rays
                for (i in 0 until 8) {
                    val angle    = Math.toRadians(i * 45.0)
                    val innerR   = w * 0.32f
                    val outerR   = w * 0.46f
                    val sinA     = sin(angle).toFloat()
                    val cosA     = cos(angle).toFloat()
                    drawLine(
                        color       = iconColor.copy(alpha = 0.75f),
                        start       = Offset(cx + cosA * innerR, cy + sinA * innerR),
                        end         = Offset(cx + cosA * outerR, cy + sinA * outerR),
                        strokeWidth = 1.5f,
                        cap         = StrokeCap.Round,
                    )
                }
            }
            WeatherType.PartlyCloudy -> {
                // Small sun upper-right
                drawCircle(
                    color  = iconColor.copy(alpha = 0.80f),
                    radius = w * 0.18f,
                    center = Offset(cx + w * 0.18f, cy - h * 0.18f),
                )
                // Cloud shape — two overlapping ellipses
                drawOval(color = iconColor, topLeft = Offset(w * 0.05f, h * 0.42f),
                    size = Size(w * 0.55f, h * 0.36f))
                drawOval(color = iconColor, topLeft = Offset(w * 0.28f, h * 0.30f),
                    size = Size(w * 0.44f, h * 0.36f))
                drawOval(color = iconColor, topLeft = Offset(w * 0.00f, h * 0.54f),
                    size = Size(w * 1.00f, h * 0.26f))
            }
            WeatherType.Cloudy -> {
                // Cloud shape — overlapping ellipses
                drawOval(color = iconColor, topLeft = Offset(w * 0.04f, h * 0.38f),
                    size = Size(w * 0.58f, h * 0.38f))
                drawOval(color = iconColor, topLeft = Offset(w * 0.26f, h * 0.24f),
                    size = Size(w * 0.48f, h * 0.38f))
                drawOval(color = iconColor, topLeft = Offset(w * 0.00f, h * 0.52f),
                    size = Size(w * 1.00f, h * 0.28f))
            }
            WeatherType.Rain,
            WeatherType.Storm -> {
                // Cloud top
                drawOval(color = iconColor.copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.04f, h * 0.10f),
                    size = Size(w * 0.58f, h * 0.34f))
                drawOval(color = iconColor.copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.26f, h * 0.04f),
                    size = Size(w * 0.44f, h * 0.30f))
                drawOval(color = iconColor.copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.00f, h * 0.22f),
                    size = Size(w * 1.00f, h * 0.24f))
                // Rain drops — three short diagonal lines
                val dropAlpha = 0.75f
                val dropCap   = StrokeCap.Round
                val dx = w * 0.06f
                val dy = h * 0.15f
                for (d in 0 until 4) {
                    val sx = w * 0.18f + d * w * 0.20f
                    val sy = h * 0.56f
                    drawLine(
                        color = iconColor.copy(alpha = dropAlpha),
                        start = Offset(sx,      sy),
                        end   = Offset(sx + dx, sy + dy),
                        strokeWidth = 1.5f, cap = dropCap,
                    )
                }
            }
            else -> {
                // Fallback: simple circle
                drawCircle(color = iconColor.copy(alpha = 0.50f), radius = w * 0.30f)
            }
        }
    }
}

// ─── Floating Glass Navigation ────────────────────────────────────────────────

private data class NavItem(
    val label: String,
    val index: Int,
)

private val navItems = listOf(
    NavItem(label = "天气", index = 0),
    NavItem(label = "地点", index = 1),
    NavItem(label = "设置", index = 2),
)

@Composable
private fun FloatingGlassNavigation(
    onOpenShowcase: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    GlassSurface(
        level    = GlassLevel.Floating,
        shape    = MistyShapes.Pill,
        modifier = modifier
            .fillMaxWidth()
            .height(MistyDimensions.NavBarHeight),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxSize()
                .padding(horizontal = MistyDimensions.Space12),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            navItems.forEach { item ->
                NavItemButton(
                    item     = item,
                    selected = selectedIndex == item.index,
                    onClick  = {
                        selectedIndex = item.index
                        if (item.index == 2) {
                            onOpenShowcase()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun NavItemButton(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val textColor = if (selected) MistyColors.TextOnGlass
                   else MistyColors.TextOnGlassTertiary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 80.dp, height = MistyDimensions.MinTouchTarget)
            .semantics {
                contentDescription = item.label + if (selected) "，已选择" else ""
            }
            // Use clickable in a real impl; pointer input kept minimal for Phase 1.
            .then(
                Modifier.padding(horizontal = MistyDimensions.Space8)
            ),
    ) {
        // Nav icon drawn on canvas
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NavIconCanvas(
                index    = item.index,
                selected = selected,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(MistyDimensions.Space4))
            Text(
                text  = item.label,
                style = MistyType.NavLabel,
                color = textColor,
            )
        }
    }
}

/** Geometric canvas nav icons — no vector drawable dependency for Phase 1. */
@Composable
private fun NavIconCanvas(
    index: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) MistyColors.TextOnGlass else MistyColors.TextOnGlassTertiary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = if (selected) 2.0f else 1.5f
        when (index) {
            0 -> { // Weather — cloud
                drawOval(color = color, topLeft = Offset(w*0.04f, h*0.32f),
                    size = Size(w*0.56f, h*0.38f))
                drawOval(color = color, topLeft = Offset(w*0.26f, h*0.18f),
                    size = Size(w*0.44f, h*0.36f))
                drawOval(color = color, topLeft = Offset(w*0.00f, h*0.50f),
                    size = Size(w*1.00f, h*0.28f))
            }
            1 -> { // Location — pin shape
                drawCircle(color = color, radius = w*0.22f,
                    center = Offset(w*0.50f, h*0.34f))
                drawLine(color = color,
                    start = Offset(w*0.50f, h*0.56f),
                    end   = Offset(w*0.50f, h*0.92f),
                    strokeWidth = sw, cap = StrokeCap.Round)
            }
            2 -> { // Settings — three horizontal lines
                for (i in 0..2) {
                    val y = h * (0.22f + i * 0.28f)
                    drawLine(color = color,
                        start = Offset(w*0.10f, y),
                        end   = Offset(w*0.90f, y),
                        strokeWidth = sw, cap = StrokeCap.Round)
                }
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Home — Cloudy Noon", showBackground = true,
    widthDp = 360, heightDp = 780)
@Composable
fun HomeScreenPreview() {
    MistyWeatherTheme(darkTheme = false) {
        HomeScreenContent(
            weather   = PreviewWeatherTokyo,
            forecast  = PreviewForecastSlots,
            timeOfDay = TimeOfDay.Noon,
        )
    }
}

@Preview(name = "Home — Sunny Morning", showBackground = true,
    widthDp = 360, heightDp = 780)
@Composable
fun HomeScreenSunnyPreview() {
    MistyWeatherTheme(darkTheme = false) {
        HomeScreenContent(
            weather   = PreviewWeatherTokyo.copy(
                weatherType = WeatherType.Sunny,
                condition   = "晴天",
                temperature = 24,
            ),
            forecast  = PreviewForecastSlots.map { it.copy(weatherType = WeatherType.Sunny) },
            timeOfDay = TimeOfDay.Morning,
        )
    }
}

@Preview(name = "Home — Sunny Sunset", showBackground = true,
    widthDp = 360, heightDp = 780)
@Composable
fun HomeScreenSunsetPreview() {
    MistyWeatherTheme(darkTheme = false) {
        HomeScreenContent(
            weather   = PreviewWeatherTokyo.copy(
                weatherType = WeatherType.Sunny,
                condition   = "晴空晚霞",
                temperature = 22,
            ),
            forecast  = PreviewForecastSlots.map { it.copy(weatherType = WeatherType.Sunny) },
            timeOfDay = TimeOfDay.Sunset,
        )
    }
}

@Preview(name = "Home — Rain Night", showBackground = true,
    widthDp = 360, heightDp = 780)
@Composable
fun HomeScreenRainDarkPreview() {
    MistyWeatherTheme(darkTheme = true) {
        HomeScreenContent(
            weather   = PreviewWeatherTokyo.copy(
                weatherType = WeatherType.Rain,
                condition   = "夜雨",
                temperature = 16,
            ),
            forecast  = PreviewForecastSlots.map { it.copy(weatherType = WeatherType.Rain) },
            timeOfDay = TimeOfDay.Night,
        )
    }
}
