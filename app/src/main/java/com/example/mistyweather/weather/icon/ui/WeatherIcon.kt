package com.example.mistyweather.weather.icon.ui

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.theme.MistyColors
import com.example.mistyweather.weather.domain.WeatherCondition
import com.example.mistyweather.weather.icon.BuiltInIconPack
import com.example.mistyweather.weather.icon.BuiltInIconPack.drawBuiltInGeometricIcon
import com.example.mistyweather.weather.icon.WeatherIconAsset
import com.example.mistyweather.weather.icon.WeatherIconKey
import com.example.mistyweather.weather.icon.WeatherIconPackManager

// ─── Weather Icon Unified Composable ─────────────────────────────────────────
//
// Phase 4.4-B0: Unified declarative weather icon presentation.
//
// Invariants:
//   1. Recompositions and state updates perform ZERO filesystem IO.
//   2. High-performance memory-cached SVG Picture rendering via Skia.
//   3. Seamlessly degrades to [BuiltInIconPack.drawBuiltInGeometricIcon] on cache miss / fallback.
//   4. Decoupled from weather simulation, networking, and Canvas render scene.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Primary weather icon component consuming the active [WeatherIconPack].
 */
@Composable
fun WeatherIcon(
    key: WeatherIconKey,
    modifier: Modifier = Modifier,
    tint: Color = MistyColors.TextOnGlass,
) {
    val context = LocalContext.current
    val manager = remember(context) { WeatherIconPackManager.getInstance(context) }
    val activePack by manager.activePack.collectAsState()

    val asset = remember(key, activePack.id) {
        manager.resolveIcon(key)
    }

    val picture = remember(asset, activePack.id) {
        manager.getOrLoadPicture(asset)
    }

    Canvas(modifier = modifier) {
        if (picture != null && picture.width > 0 && picture.height > 0) {
            val scaleX = size.width / picture.width.toFloat()
            val scaleY = size.height / picture.height.toFloat()
            drawIntoCanvas { canvas ->
                val isBuiltIn = activePack.id == BuiltInIconPack.DEFAULT_PACK_ID
                if (isBuiltIn) {
                    val paint = Paint().apply {
                        colorFilter = PorterDuffColorFilter(tint.toArgb(), PorterDuff.Mode.SRC_IN)
                    }
                    canvas.nativeCanvas.saveLayer(null, paint)
                    canvas.nativeCanvas.scale(scaleX, scaleY)
                    canvas.nativeCanvas.drawPicture(picture)
                    canvas.nativeCanvas.restore()
                } else {
                    canvas.nativeCanvas.save()
                    canvas.nativeCanvas.scale(scaleX, scaleY)
                    canvas.nativeCanvas.drawPicture(picture)
                    canvas.nativeCanvas.restore()
                }
            }
        } else {
            // Three-tier guaranteed geometric fallback
            drawBuiltInGeometricIcon(key = key, color = tint)
        }
    }
}

/**
 * Convenience overload resolving canonical icon from domain [WeatherCondition].
 */
@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    isDaytime: Boolean = true,
    modifier: Modifier = Modifier,
    tint: Color = MistyColors.TextOnGlass,
) {
    val key = remember(condition, isDaytime) {
        WeatherIconKey.fromCondition(condition, isDaytime)
    }
    WeatherIcon(key = key, modifier = modifier, tint = tint)
}

/**
 * Convenience overload resolving canonical icon from legacy/preview [WeatherType].
 */
@Composable
fun WeatherIcon(
    weatherType: WeatherType,
    isDaytime: Boolean = true,
    modifier: Modifier = Modifier,
    tint: Color = MistyColors.TextOnGlass,
) {
    val key = remember(weatherType, isDaytime) {
        WeatherIconKey.fromWeatherType(weatherType, isDaytime)
    }
    WeatherIcon(key = key, modifier = modifier, tint = tint)
}
