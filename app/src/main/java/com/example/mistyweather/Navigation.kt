package com.example.mistyweather

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.mistyweather.feature.home.HomeScreen

// ─── Main Navigation ──────────────────────────────────────────────────────────
// Phase 1: single destination — HomeScreen.
// Future phases will add LocationSearch and Settings entries.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack     = backStack,
        onBack        = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                HomeScreen()
            }
        },
    )
}
