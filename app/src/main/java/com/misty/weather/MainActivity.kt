package com.misty.weather

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.misty.weather.theme.MistyWeatherTheme
import com.misty.weather.weather.di.WeatherModule

class MainActivity : ComponentActivity() {
    private var weatherOverrideState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weatherOverrideState = intent?.getStringExtra("EXTRA_WEATHER")

        // Phase 4.3: Initialize real persistent DataStore cache, City storage & Location provider
        WeatherModule.initialize(applicationContext, useRemote = true)

        // Edge-to-edge: weather scene bleeds behind status bar and nav bar —
        // the floating glass nav handles its own safe-area insets.
        enableEdgeToEdge()

        setContent {
            MistyWeatherTheme {
                // No Surface wrapper — HomeScreen owns the full canvas including
                // the weather background. A Surface here would clip the scene.
                MainNavigation(weatherOverride = weatherOverrideState)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        weatherOverrideState = intent.getStringExtra("EXTRA_WEATHER")
    }
}
