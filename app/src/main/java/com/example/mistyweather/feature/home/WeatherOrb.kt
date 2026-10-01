package com.example.mistyweather.feature.home

import android.graphics.Path as AndroidPath
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mistyweather.core.weather.WeatherType
import com.example.mistyweather.theme.LocalMistyReduceMotion
import com.example.mistyweather.theme.MistyDimensions
import com.example.mistyweather.weather.atmosphere.TimeOfDay
import com.example.mistyweather.weather.icon.BuiltInIconPack
import com.example.mistyweather.weather.icon.WeatherIconKey
import com.example.mistyweather.weather.icon.WeatherIconPackManager
import com.example.mistyweather.weather.icon.ui.WeatherIcon

// ─── Weather Orb: Atmospheric Celestial Hero Presence ────────────────────────
//
// Phase F: Replaces harsh silhouette/ink-blot icons with a breathing, luminous,
// volumetric celestial orb.
//
// Design Invariants:
//   1. Radial Ambient Aura: Multi-layer luminous glow keyed to weather and diurnal phase.
//   2. Volumetric Depth: Soft gradient shading, specular highlights, zero pitch-black fills.
//   3. Diurnal Awareness: Renders silvery crescent moon & stars at night, golden sun at noon.
//   4. Breathing Motion: Subtle floating (±2.5dp) & pulsing (±3%), strictly respecting
//      [LocalMistyReduceMotion].
//   5. Extensible Icon Pack Support: Seamlessly wraps custom imported icon packs with the
//      same atmospheric aura and breathing motion.
//
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WeatherOrb(
    weatherType: WeatherType,
    modifier: Modifier = Modifier,
    size: Dp = MistyDimensions.WeatherOrbSize,
    timeOfDay: TimeOfDay? = null,
) {
    val isReducedMotion = LocalMistyReduceMotion.current
    val isDaytime = timeOfDay?.let { it != TimeOfDay.Night && it != TimeOfDay.Evening } ?: true

    // Breathing motion transition
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherOrbBreathing")
    val breathingProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathingFloat",
    )
    val floatProgress = if (isReducedMotion) 0f else breathingProgress

    // Check active icon pack
    val context = LocalContext.current
    val manager = remember(context) { WeatherIconPackManager.getInstance(context) }
    val activePack by manager.activePack.collectAsState()
    val isDefaultPack = activePack.id == BuiltInIconPack.DEFAULT_PACK_ID

    val a11yDescription = "${weatherType.name} ${if (isDaytime) "白天" else "夜间"} 气象光球"

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                val floatOffset = (floatProgress * 5f - 2.5f) * density
                translationY = floatOffset
                val scale = 0.98f + floatProgress * 0.04f
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                contentDescription = a11yDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        // Layer 1: Ambient Luminous Aura
        Canvas(modifier = Modifier.size(size + 32.dp)) {
            drawAtmosphericAura(
                weatherType   = weatherType,
                isDaytime     = isDaytime,
                floatProgress = floatProgress,
            )
        }

        // Layer 2: Celestial Core (Volumetric Orb or Custom Pack Icon)
        if (isDefaultPack) {
            Canvas(modifier = Modifier.size(size)) {
                drawVolumetricWeatherOrb(
                    weatherType   = weatherType,
                    isDaytime     = isDaytime,
                    floatProgress = floatProgress,
                )
            }
        } else {
            WeatherIcon(
                weatherType = weatherType,
                isDaytime   = isDaytime,
                modifier    = Modifier.size(size * 0.85f),
            )
        }
    }
}

// ─── Layer 1: Ambient Aura Drawing ───────────────────────────────────────────

private fun DrawScope.drawAtmosphericAura(
    weatherType: WeatherType,
    isDaytime: Boolean,
    floatProgress: Float,
) {
    val auraRadius = size.minDimension * 0.52f
    val auraColors = when {
        !isDaytime -> listOf(
            Color(0xFF818CF8).copy(alpha = 0.28f + floatProgress * 0.08f),
            Color(0xFF6366F1).copy(alpha = 0.10f + floatProgress * 0.04f),
            Color.Transparent,
        )
        weatherType == WeatherType.Sunny -> listOf(
            Color(0xFFFFD54F).copy(alpha = 0.32f + floatProgress * 0.08f),
            Color(0xFFFF9800).copy(alpha = 0.12f + floatProgress * 0.04f),
            Color.Transparent,
        )
        weatherType == WeatherType.Rain || weatherType == WeatherType.Storm -> listOf(
            Color(0xFF38BDF8).copy(alpha = 0.30f + floatProgress * 0.08f),
            Color(0xFF0284C7).copy(alpha = 0.10f + floatProgress * 0.04f),
            Color.Transparent,
        )
        weatherType == WeatherType.Snow -> listOf(
            Color(0xFFE0F2FE).copy(alpha = 0.34f + floatProgress * 0.08f),
            Color(0xFFBAE6FD).copy(alpha = 0.10f + floatProgress * 0.04f),
            Color.Transparent,
        )
        else -> listOf(
            Color(0xFFE2E8F0).copy(alpha = 0.26f + floatProgress * 0.06f),
            Color(0xFF94A3B8).copy(alpha = 0.08f + floatProgress * 0.04f),
            Color.Transparent,
        )
    }

    drawCircle(
        brush  = Brush.radialGradient(
            colors = auraColors,
            center = center,
            radius = auraRadius,
        ),
        radius = auraRadius,
        center = center,
    )
}

// ─── Layer 2: Volumetric Celestial Core Drawing ──────────────────────────────

private fun DrawScope.drawVolumetricWeatherOrb(
    weatherType: WeatherType,
    isDaytime: Boolean,
    floatProgress: Float,
) {
    val w = size.width
    val h = size.height

    when (weatherType) {
        WeatherType.Sunny -> {
            if (isDaytime) {
                drawVolumetricSun(w, h)
            } else {
                drawLuminousCrescentMoon(w, h)
            }
        }
        WeatherType.PartlyCloudy -> {
            if (isDaytime) {
                // Sun peeking top-right
                drawSunSphere(center = Offset(w * 0.68f, h * 0.36f), radius = w * 0.17f)
                // Foreground cloud volume
                drawVolumetricCloud(w, h, isNight = false, floatProgress = floatProgress)
            } else {
                // Moon peeking top-right
                drawMiniCrescent(center = Offset(w * 0.66f, h * 0.35f), radius = w * 0.14f)
                drawVolumetricCloud(w, h, isNight = true, floatProgress = floatProgress)
            }
        }
        WeatherType.Cloudy -> {
            drawDualCloudVolume(w, h, isNight = !isDaytime, floatProgress = floatProgress)
        }
        WeatherType.Rain -> {
            drawVolumetricCloud(w, h, isNight = !isDaytime, floatProgress = floatProgress)
            drawRainStreaks(w, h, floatProgress = floatProgress)
        }
        WeatherType.Storm -> {
            drawVolumetricCloud(w, h, isNight = true, floatProgress = floatProgress, tintStorm = true)
            drawRainStreaks(w, h, floatProgress = floatProgress)
            drawLightningSpark(w, h)
        }
        WeatherType.Snow -> {
            drawVolumetricCloud(w, h, isNight = !isDaytime, floatProgress = floatProgress)
            drawSnowflakes(w, h, floatProgress = floatProgress)
        }
        WeatherType.Fog -> {
            drawFogRibbons(w, h, floatProgress = floatProgress)
        }
    }
}

// ── Sub-Renderers ─────────────────────────────────────────────────────────────

private fun DrawScope.drawVolumetricSun(w: Float, h: Float) {
    val r = w * 0.28f
    val sunCenter = Offset(w * 0.5f, h * 0.5f)

    // Soft outer solar corona
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFE082).copy(alpha = 0.45f),
                Color(0xFFFFB74D).copy(alpha = 0.12f),
                Color.Transparent,
            ),
            center = sunCenter,
            radius = r * 1.55f,
        ),
        radius = r * 1.55f,
        center = sunCenter,
    )

    // 3D Volumetric celestial sphere with specular highlight
    val lightOffset = Offset(sunCenter.x - r * 0.32f, sunCenter.y - r * 0.32f)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFFFFBEB),
                0.35f to Color(0xFFFFD54F),
                0.80f to Color(0xFFFFA000),
                1.0f to Color(0xFFF57C00),
            ),
            center = lightOffset,
            radius = r * 1.4f,
        ),
        radius = r,
        center = sunCenter,
    )

    // Specular glimmer
    drawCircle(
        color = Color.White.copy(alpha = 0.55f),
        radius = r * 0.22f,
        center = lightOffset,
    )
}

private fun DrawScope.drawSunSphere(center: Offset, radius: Float) {
    val lightOffset = Offset(center.x - radius * 0.30f, center.y - radius * 0.30f)
    drawCircle(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0xFFFFFBEB),
                0.40f to Color(0xFFFFD54F),
                1.0f to Color(0xFFF59E0B),
            ),
            center = lightOffset,
            radius = radius * 1.3f,
        ),
        radius = radius,
        center = center,
    )
}

private fun DrawScope.drawLuminousCrescentMoon(w: Float, h: Float) {
    val r = w * 0.27f
    val moonCenter = Offset(w * 0.5f, h * 0.5f)

    val moonPath = Path().apply {
        addOval(Rect(moonCenter.x - r, moonCenter.y - r, moonCenter.x + r, moonCenter.y + r))
    }
    val cutoutRadius = r * 0.88f
    val cutoutCenter = Offset(moonCenter.x + r * 0.44f, moonCenter.y - r * 0.36f)
    val cutoutPath = Path().apply {
        addOval(
            Rect(
                cutoutCenter.x - cutoutRadius,
                cutoutCenter.y - cutoutRadius,
                cutoutCenter.x + cutoutRadius,
                cutoutCenter.y + cutoutRadius,
            )
        )
    }

    val crescent = Path().apply {
        op(moonPath, cutoutPath, PathOperation.Difference)
    }

    drawPath(
        path = crescent,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFE2E8F0),
                Color(0xFF94A3B8),
            ),
            start = Offset(moonCenter.x - r, moonCenter.y - r),
            end = Offset(moonCenter.x + r, moonCenter.y + r),
        ),
    )

    // Diamond star twinkles
    drawDiamondStar(Offset(moonCenter.x + r * 0.65f, moonCenter.y + r * 0.15f), 5.dp.toPx())
    drawDiamondStar(Offset(moonCenter.x - r * 0.70f, moonCenter.y - r * 0.50f), 3.5f.dp.toPx())
}

private fun DrawScope.drawMiniCrescent(center: Offset, radius: Float) {
    val moonPath = Path().apply {
        addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
    }
    val cutoutRadius = radius * 0.88f
    val cutoutCenter = Offset(center.x + radius * 0.42f, center.y - radius * 0.35f)
    val cutoutPath = Path().apply {
        addOval(
            Rect(
                cutoutCenter.x - cutoutRadius,
                cutoutCenter.y - cutoutRadius,
                cutoutCenter.x + cutoutRadius,
                cutoutCenter.y + cutoutRadius,
            )
        )
    }
    val crescent = Path().apply {
        op(moonPath, cutoutPath, PathOperation.Difference)
    }

    drawPath(
        path = crescent,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1)),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius),
        ),
    )
}

private fun DrawScope.drawDiamondStar(center: Offset, size: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x + size * 0.35f, center.y)
        lineTo(center.x, center.y + size)
        lineTo(center.x - size * 0.35f, center.y)
        close()
    }
    drawPath(path = path, color = Color(0xFFE0E7FF).copy(alpha = 0.90f))
}

private fun DrawScope.drawVolumetricCloud(
    w: Float,
    h: Float,
    isNight: Boolean,
    floatProgress: Float,
    tintStorm: Boolean = false,
) {
    val cloudCenterY = h * 0.52f
    val puffRadiusL = w * 0.16f
    val puffRadiusC = w * 0.22f
    val puffRadiusR = w * 0.15f

    val centerL = Offset(w * 0.34f, cloudCenterY + h * 0.04f)
    val centerC = Offset(w * 0.51f, cloudCenterY - h * 0.04f)
    val centerR = Offset(w * 0.68f, cloudCenterY + h * 0.06f)

    val cloudPath = Path().apply {
        addOval(Rect(centerL.x - puffRadiusL, centerL.y - puffRadiusL, centerL.x + puffRadiusL, centerL.y + puffRadiusL))
        addOval(Rect(centerC.x - puffRadiusC, centerC.y - puffRadiusC, centerC.x + puffRadiusC, centerC.y + puffRadiusC))
        addOval(Rect(centerR.x - puffRadiusR, centerR.y - puffRadiusR, centerR.x + puffRadiusR, centerR.y + puffRadiusR))
        addRoundRect(
            RoundRect(
                rect = Rect(w * 0.22f, cloudCenterY, w * 0.78f, cloudCenterY + h * 0.18f),
                cornerRadius = CornerRadius(h * 0.09f, h * 0.09f),
            )
        )
    }

    val topColor = when {
        tintStorm -> Color(0xFF64748B)
        isNight -> Color(0xFFE2E8F0).copy(alpha = 0.92f)
        else -> Color(0xFFFFFFFF)
    }
    val bottomColor = when {
        tintStorm -> Color(0xFF1E293B)
        isNight -> Color(0xFF64748B).copy(alpha = 0.85f)
        else -> Color(0xFFCBD5E1).copy(alpha = 0.88f)
    }

    drawPath(
        path = cloudPath,
        brush = Brush.verticalGradient(
            colors = listOf(topColor, bottomColor),
            startY = centerC.y - puffRadiusC,
            endY = cloudCenterY + h * 0.18f,
        ),
    )
}

private fun DrawScope.drawDualCloudVolume(
    w: Float,
    h: Float,
    isNight: Boolean,
    floatProgress: Float,
) {
    // Back cloud
    val backCloudCenterY = h * 0.44f
    val backCenter = Offset(w * 0.44f, backCloudCenterY)
    val backRadius = w * 0.20f
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF94A3B8).copy(alpha = 0.60f),
                Color(0xFF64748B).copy(alpha = 0.60f),
            ),
            startY = backCenter.y - backRadius,
            endY = backCenter.y + backRadius,
        ),
        radius = backRadius,
        center = backCenter,
    )

    // Front cloud
    drawVolumetricCloud(w, h, isNight = isNight, floatProgress = floatProgress)
}

private fun DrawScope.drawRainStreaks(w: Float, h: Float, floatProgress: Float) {
    val streakYOffset = floatProgress * 3.dp.toPx()
    val streakWidth = 2.5f.dp.toPx()

    val streaks = listOf(
        Offset(w * 0.38f, h * 0.68f + streakYOffset) to Offset(w * 0.36f, h * 0.84f + streakYOffset),
        Offset(w * 0.50f, h * 0.72f + streakYOffset) to Offset(w * 0.48f, h * 0.88f + streakYOffset),
        Offset(w * 0.62f, h * 0.68f + streakYOffset) to Offset(w * 0.60f, h * 0.84f + streakYOffset),
    )

    streaks.forEach { (start, end) ->
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF7DD3FC).copy(alpha = 0.85f),
                    Color(0xFF0284C7).copy(alpha = 0.30f),
                ),
                start = start,
                end = end,
            ),
            start = start,
            end = end,
            strokeWidth = streakWidth,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawLightningSpark(w: Float, h: Float) {
    val sparkPath = Path().apply {
        moveTo(w * 0.53f, h * 0.62f)
        lineTo(w * 0.47f, h * 0.74f)
        lineTo(w * 0.52f, h * 0.74f)
        lineTo(w * 0.46f, h * 0.88f)
    }
    // Ambient electric aura
    drawPath(
        path = sparkPath,
        color = Color(0xFFFACC15).copy(alpha = 0.45f),
        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
    )
    // Core bolt
    drawPath(
        path = sparkPath,
        color = Color(0xFFFFFBEB),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawSnowflakes(w: Float, h: Float, floatProgress: Float) {
    val yOffset = floatProgress * 2.5f.dp.toPx()
    val flakes = listOf(
        Offset(w * 0.38f, h * 0.76f + yOffset),
        Offset(w * 0.50f, h * 0.82f - yOffset),
        Offset(w * 0.62f, h * 0.76f + yOffset),
    )
    flakes.forEach { pos ->
        drawCircle(
            color = Color(0xFFE0F2FE).copy(alpha = 0.90f),
            radius = 2.2f.dp.toPx(),
            center = pos,
        )
    }
}

private fun DrawScope.drawFogRibbons(w: Float, h: Float, floatProgress: Float) {
    val ribbonHeight = 5.dp.toPx()
    val ribbons = listOf(
        Rect(w * 0.18f, h * 0.40f, w * 0.82f, h * 0.40f + ribbonHeight),
        Rect(w * 0.12f, h * 0.52f, w * 0.88f, h * 0.52f + ribbonHeight),
        Rect(w * 0.24f, h * 0.64f, w * 0.76f, h * 0.64f + ribbonHeight),
    )
    ribbons.forEach { rect ->
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFE2E8F0).copy(alpha = 0.85f),
                    Color(0xFFCBD5E1).copy(alpha = 0.85f),
                    Color.Transparent,
                ),
                startX = rect.left,
                endX = rect.right,
            ),
            topLeft = Offset(rect.left, rect.top),
            size = Size(rect.width, rect.height),
            cornerRadius = CornerRadius(ribbonHeight * 0.5f, ribbonHeight * 0.5f),
        )
    }
}
