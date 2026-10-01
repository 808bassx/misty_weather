package com.misty.weather

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.misty.weather.feature.home.HomeScreen
import com.misty.weather.feature.showcase.WeatherShowcaseScreen
import com.misty.weather.weather.icon.ui.IconPackSettingsScreen

// ─── Main Navigation ──────────────────────────────────────────────────────────
// Phase 1: single destination — HomeScreen.
// Phase 3.5: adds Showcase destination for real-device visual calibration.
// Phase 4.4-B0: adds IconPackSettings destination for icon pack management.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MainNavigation(
    weatherOverride: String? = null,
) {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack     = backStack,
        onBack        = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                HomeScreen(
                    weatherOverride = weatherOverride,
                    onOpenShowcase   = { backStack.add(Showcase) },
                    onOpenIconPacks  = { backStack.add(IconPackSettings) },
                )
            }
            entry<Showcase> {
                WeatherShowcaseScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
            entry<IconPackSettings> {
                IconPackSettingsScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
