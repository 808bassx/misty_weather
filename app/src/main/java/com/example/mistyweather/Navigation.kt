package com.example.mistyweather

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.mistyweather.feature.home.HomeScreen
import com.example.mistyweather.feature.showcase.WeatherShowcaseScreen

// ─── Main Navigation ──────────────────────────────────────────────────────────
// Phase 1: single destination — HomeScreen.
// Phase 3.5: adds Showcase destination for real-device visual calibration.
// Future phases will add LocationSearch.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack     = backStack,
        onBack        = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                HomeScreen(
                    onOpenShowcase = { backStack.add(Showcase) },
                )
            }
            entry<Showcase> {
                WeatherShowcaseScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
