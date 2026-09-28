package com.example.mistyweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.mistyweather.theme.MistyWeatherTheme
import com.example.mistyweather.weather.di.WeatherModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Phase 4.2: Enable remote weather pipeline with Open-Meteo REST API
        WeatherModule.useRemoteRepository()

        // Edge-to-edge: weather scene bleeds behind status bar and nav bar —
        // the floating glass nav handles its own safe-area insets.
        enableEdgeToEdge()

        setContent {
            MistyWeatherTheme {
                // No Surface wrapper — HomeScreen owns the full canvas including
                // the weather background. A Surface here would clip the scene.
                MainNavigation()
            }
        }
    }
}
