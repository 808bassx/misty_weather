package com.misty.weather.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.unit.sp
import com.misty.weather.core.weather.ForecastSlot
import com.misty.weather.core.weather.PreviewForecastSlots
import com.misty.weather.core.weather.PreviewWeatherTokyo
import com.misty.weather.core.weather.WeatherPreview
import com.misty.weather.core.weather.WeatherType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.misty.weather.weather.domain.WeatherMapper
import com.misty.weather.weather.domain.WeatherRenderState
import com.misty.weather.weather.viewmodel.WeatherUiState
import com.misty.weather.weather.viewmodel.WeatherViewModel
import com.misty.weather.weather.scene.CinematicWeatherScene
import com.misty.weather.theme.GlassLevel
import com.misty.weather.theme.GlassSurface
import com.misty.weather.theme.LocalGlassAmbient
import com.misty.weather.theme.MistyColors
import com.misty.weather.theme.MistyDimensions
import com.misty.weather.theme.MistyShapes
import com.misty.weather.theme.MistyType
import com.misty.weather.theme.MistyWeatherTheme
import com.misty.weather.weather.atmosphere.TimeOfDay
import com.misty.weather.weather.atmosphere.computeAmbientAtmosphere
import com.misty.weather.weather.icon.ui.WeatherIcon

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
    weatherOverride: String? = null,
    onOpenShowcase: () -> Unit = {},
    onOpenIconPacks: () -> Unit = {},
) {
    if (weatherOverride != null) {
        val (presetWeather, presetForecast, presetTimeOfDay) = when (weatherOverride.uppercase()) {
            "SUNNY", "CLEAR" -> Triple(
                WeatherPreview(
                    city        = "东京",
                    cityLatin   = "Tokyo",
                    temperature = 28,
                    condition   = "晴朗",
                    feelsLike   = 29,
                    humidity    = 45,
                    weatherType = WeatherType.Sunny,
                    timeOfDay   = TimeOfDay.Noon,
                ),
                listOf(
                    ForecastSlot(label = "现在",  temperature = 28, weatherType = WeatherType.Sunny),
                    ForecastSlot(label = "13:00", temperature = 29, weatherType = WeatherType.Sunny),
                    ForecastSlot(label = "14:00", temperature = 29, weatherType = WeatherType.Sunny),
                    ForecastSlot(label = "15:00", temperature = 28, weatherType = WeatherType.Sunny),
                    ForecastSlot(label = "16:00", temperature = 27, weatherType = WeatherType.Sunny),
                    ForecastSlot(label = "17:00", temperature = 25, weatherType = WeatherType.PartlyCloudy),
                ),
                TimeOfDay.Noon,
            )
            "RAIN", "RAINY" -> Triple(
                WeatherPreview(
                    city        = "伦敦",
                    cityLatin   = "London",
                    temperature = 14,
                    condition   = "水汽氤氲 · 暮雨",
                    feelsLike   = 13,
                    humidity    = 88,
                    weatherType = WeatherType.Rain,
                    timeOfDay   = TimeOfDay.Evening,
                ),
                listOf(
                    ForecastSlot(label = "现在",  temperature = 14, weatherType = WeatherType.Rain),
                    ForecastSlot(label = "18:00", temperature = 14, weatherType = WeatherType.Rain),
                    ForecastSlot(label = "19:00", temperature = 13, weatherType = WeatherType.Rain),
                    ForecastSlot(label = "20:00", temperature = 13, weatherType = WeatherType.Rain),
                    ForecastSlot(label = "21:00", temperature = 12, weatherType = WeatherType.Cloudy),
                    ForecastSlot(label = "22:00", temperature = 12, weatherType = WeatherType.Cloudy),
                ),
                TimeOfDay.Evening,
            )
            "NIGHT" -> Triple(
                WeatherPreview(
                    city        = "京都",
                    cityLatin   = "Kyoto",
                    temperature = 17,
                    condition   = "月华如练 · 澄澈",
                    feelsLike   = 16,
                    humidity    = 62,
                    weatherType = WeatherType.Sunny,
                    timeOfDay   = TimeOfDay.Night,
                ),
                listOf(
                    ForecastSlot(label = "现在",  temperature = 17, weatherType = WeatherType.Sunny),
                    ForecastSlot(label = "22:00", temperature = 16, weatherType = WeatherType.Sunny),
                    ForecastSlot(label = "23:00", temperature = 15, weatherType = WeatherType.Sunny),
                    ForecastSlot(label = "00:00", temperature = 15, weatherType = WeatherType.Sunny),
                    ForecastSlot(label = "01:00", temperature = 14, weatherType = WeatherType.Cloudy),
                    ForecastSlot(label = "02:00", temperature = 14, weatherType = WeatherType.Cloudy),
                ),
                TimeOfDay.Night,
            )
            "CLOUDY" -> Triple(
                WeatherPreview(
                    city        = "巴黎",
                    cityLatin   = "Paris",
                    temperature = 18,
                    condition   = "天光温润 · 阴",
                    feelsLike   = 17,
                    humidity    = 76,
                    weatherType = WeatherType.Cloudy,
                    timeOfDay   = TimeOfDay.Noon,
                ),
                listOf(
                    ForecastSlot(label = "现在",  temperature = 18, weatherType = WeatherType.Cloudy),
                    ForecastSlot(label = "15:00", temperature = 18, weatherType = WeatherType.Cloudy),
                    ForecastSlot(label = "16:00", temperature = 19, weatherType = WeatherType.Cloudy),
                    ForecastSlot(label = "17:00", temperature = 18, weatherType = WeatherType.Cloudy),
                    ForecastSlot(label = "18:00", temperature = 17, weatherType = WeatherType.PartlyCloudy),
                    ForecastSlot(label = "19:00", temperature = 16, weatherType = WeatherType.PartlyCloudy),
                ),
                TimeOfDay.Noon,
            )
            else -> Triple(
                PreviewWeatherTokyo,
                PreviewForecastSlots,
                TimeOfDay.Noon,
            )
        }

        HomeScreenContent(
            weather         = presetWeather,
            forecast        = presetForecast,
            timeOfDay       = presetTimeOfDay,
            runtimeNotice   = null,
            onOpenShowcase  = onOpenShowcase,
            onOpenIconPacks = onOpenIconPacks,
            onCityClick     = null,
            modifier        = modifier,
        )
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentCity by viewModel.currentCity.collectAsStateWithLifecycle()
    val favoriteCities by viewModel.favoriteCities.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val locationNotice by viewModel.locationNotice.collectAsStateWithLifecycle()

    var showCityDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.requestCurrentLocation()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    val onCityClick = { showCityDialog = true }
    val onUseMyLocation = {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    when (val state = uiState) {
        WeatherUiState.Loading -> {
            HomeScreenContent(
                weather         = PreviewWeatherTokyo,
                forecast        = PreviewForecastSlots,
                timeOfDay       = timeOfDay,
                runtimeNotice   = locationNotice,
                onOpenShowcase  = onOpenShowcase,
                onOpenIconPacks = onOpenIconPacks,
                onCityClick     = onCityClick,
                modifier        = modifier,
            )
        }
        is WeatherUiState.Success -> {
            HomeScreenContent(
                weather         = state.preview,
                forecast        = state.forecastSlots,
                renderState     = state.renderState,
                timeOfDay       = timeOfDay ?: state.activeTimeOfDay,
                runtimeNotice   = locationNotice,
                onOpenShowcase  = onOpenShowcase,
                onOpenIconPacks = onOpenIconPacks,
                onCityClick     = onCityClick,
                modifier        = modifier,
            )
        }
        is WeatherUiState.Refreshing -> {
            HomeScreenContent(
                weather         = state.previous.preview,
                forecast        = state.previous.forecastSlots,
                renderState     = state.previous.renderState,
                timeOfDay       = timeOfDay ?: state.previous.activeTimeOfDay,
                runtimeNotice   = locationNotice ?: "正在同步天气数据...",
                onRetryNotice   = null,
                onOpenShowcase  = onOpenShowcase,
                onOpenIconPacks = onOpenIconPacks,
                onCityClick     = onCityClick,
                modifier        = modifier,
            )
        }
        is WeatherUiState.Error -> {
            val lastKnown = state.lastKnown
            HomeScreenContent(
                weather         = lastKnown?.preview ?: PreviewWeatherTokyo,
                forecast        = lastKnown?.forecastSlots ?: PreviewForecastSlots,
                renderState     = lastKnown?.renderState,
                timeOfDay       = timeOfDay ?: state.activeTimeOfDay,
                runtimeNotice   = locationNotice ?: state.error.userFacingMessage,
                onRetryNotice   = if (state.canRetry) { { viewModel.refreshWeather() } } else null,
                onOpenShowcase  = onOpenShowcase,
                onOpenIconPacks = onOpenIconPacks,
                onCityClick     = onCityClick,
                modifier        = modifier,
            )
        }
    }

    if (showCityDialog) {
        CitySelectionDialog(
            cities          = favoriteCities,
            currentCity     = currentCity,
            onSelectCity    = { city ->
                viewModel.selectCity(city)
                showCityDialog = false
            },
            onUseMyLocation = {
                onUseMyLocation()
                showCityDialog = false
            },
            onDismiss       = { showCityDialog = false },
            onSearch        = { query -> viewModel.searchCities(query) },
            searchResults   = searchResults,
        )
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
    onOpenIconPacks: () -> Unit = {},
    onCityClick: (() -> Unit)? = null,
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
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(
                        top        = MistyDimensions.Space16,
                        start      = MistyDimensions.ScreenHorizontalPadding,
                        end        = MistyDimensions.ScreenHorizontalPadding,
                        // Extra bottom padding so content clears the floating nav.
                        bottom     = MistyDimensions.BottomSafeArea + MistyDimensions.NavBarHeight +
                                     MistyDimensions.NavBarBottomInset,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Phase D: Atmospheric Zero-Container Home Hero
                HomeHero(
                    weather         = weather,
                    runtimeNotice   = runtimeNotice,
                    onRetryNotice   = onRetryNotice,
                    onCityClick     = onCityClick,
                )

                Spacer(Modifier.height(MistyDimensions.HeroSpacing))

                // Phase F.2: Borderless Atmospheric Timeline
                AtmosphericTimeline(
                    slots           = forecast,
                    activeTimeOfDay = activeTime,
                )
            }

            // ── Layer 4: Floating Glass Navigation ────────────────────────────────
            FloatingGlassNavigation(
                onOpenShowcase  = onOpenShowcase,
                onOpenIconPacks = onOpenIconPacks,
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

// ─── Phase F.2: Atmospheric Timeline (Borderless Floating Celestial Strip) ───

@Composable
internal fun AtmosphericTimeline(
    slots: List<ForecastSlot>,
    activeTimeOfDay: TimeOfDay? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        // Minimalist timeline header floating without container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MistyDimensions.Space8, vertical = MistyDimensions.Space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "今日预报",
                style = MistyType.ForecastTime,
                color = MistyColors.TextOnGlassSecondary.copy(alpha = 0.80f),
            )
            Text(
                text = "24-HOUR",
                style = MistyType.CaptionMicro,
                color = MistyColors.TextOnGlassTertiary,
                letterSpacing = 1.5.sp,
            )
        }

        Spacer(Modifier.height(MistyDimensions.Space8))

        // Borderless, transparent horizontal scrolling timeline
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(MistyDimensions.Space8),
        ) {
            slots.forEach { slot ->
                AtmosphericTimelineSlotItem(
                    slot = slot,
                    activeTimeOfDay = activeTimeOfDay,
                )
            }
        }
    }
}

@Composable
private fun AtmosphericTimelineSlotItem(
    slot: ForecastSlot,
    activeTimeOfDay: TimeOfDay?,
) {
    val isCurrentSlot = slot.label.trim() == "现在"
    val isSlotDay = isHourlySlotDaytime(slot.label, activeTimeOfDay)
    val ambientColor = LocalGlassAmbient.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(MistyDimensions.ForecastItemMinWidth)
            .then(
                if (isCurrentSlot) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.07f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        )
                    }
                } else Modifier
            )
            .padding(vertical = MistyDimensions.Space8, horizontal = MistyDimensions.Space4)
            .semantics {
                contentDescription = "${slot.label} ${slot.temperature}度 ${slot.weatherType.name}"
            },
    ) {
        // Time label
        Text(
            text = slot.label,
            style = MistyType.ForecastTime,
            color = if (isCurrentSlot) MistyColors.TextOnGlass else MistyColors.TextOnGlassSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(MistyDimensions.Space8))

        // Weather symbol with subtle ambient halo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ambientColor.copy(alpha = 0.16f),
                                Color.Transparent,
                            ),
                            radius = size.width * 0.5f,
                            center = center,
                        ),
                    )
                },
        ) {
            WeatherIcon(
                weatherType = slot.weatherType,
                isDaytime   = isSlotDay,
                modifier    = Modifier.size(MistyDimensions.WeatherIconForecast),
                tint        = Color(0xFFF1F5F9).copy(alpha = 0.92f),
            )
        }

        Spacer(Modifier.height(MistyDimensions.Space8))

        // Temperature
        Text(
            text = "${slot.temperature}°",
            style = MistyType.ForecastTemp,
            color = if (isCurrentSlot) MistyColors.TextOnGlass else MistyColors.TextOnGlass.copy(alpha = 0.90f),
            textAlign = TextAlign.Center,
        )
    }
}

internal fun isHourlySlotDaytime(label: String, activeTime: TimeOfDay?): Boolean {
    val trimmed = label.trim()
    if (trimmed == "现在") {
        return activeTime?.let { it != TimeOfDay.Night && it != TimeOfDay.Evening } ?: true
    }
    val hour = trimmed.substringBefore(":").toIntOrNull()
    return if (hour != null) {
        hour in 6..18
    } else {
        true
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
    onOpenIconPacks: () -> Unit = {},
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
                        when (item.index) {
                            1 -> onOpenShowcase()
                            2 -> onOpenIconPacks()
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
            .clickable(onClick = onClick)
            .padding(horizontal = MistyDimensions.Space8),
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
