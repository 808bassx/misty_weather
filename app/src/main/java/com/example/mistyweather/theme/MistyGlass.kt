package com.example.mistyweather.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Misty Liquid Glass System ────────────────────────────────────────────────
//
// Glass is NOT a simple white overlay. It is a layered surface:
//
//   1. Base transparency   — semi-transparent fill tied to GlassLevel
//   2. Adaptive tint       — caller-supplied color for weather-scene tinting
//   3. Top highlight       — subtle linear gradient from white-10% → transparent
//   4. Border              — 1dp white-10% stroke
//   5. Soft elevation shadow — very low alpha, never dark/heavy
//
// On Android < API 31 we approximate blur via alpha; true RenderEffect blur
// requires API 31+ and should be added in Phase 2 behind a version check.
//
// Core principle:
//   "Glass is part of the weather scene, not a box covering it."
//
// ─────────────────────────────────────────────────────────────────────────────

/** Opacity levels that define how "opaque" each glass tier is. */
enum class GlassLevel(
    /** Base fill alpha (0f–1f). */
    internal val fillAlpha: Float,
    /** Highlight bar alpha at the top edge. */
    internal val highlightAlpha: Float,
    /** Shadow elevation in dp. */
    internal val elevationDp: Float,
) {
    /** Barely-there — top information strips, ghost overlays. */
    UltraThin(fillAlpha = 0.10f, highlightAlpha = 0.08f, elevationDp = 0f),

    /** Light veil — secondary weather info zones. */
    Thin(fillAlpha = 0.18f, highlightAlpha = 0.10f, elevationDp = 2f),

    /** Standard glass panel — forecast cards, detail rows. */
    Regular(fillAlpha = 0.25f, highlightAlpha = 0.12f, elevationDp = 4f),

    /** Interactive / active state — selected cards, primary action buttons. */
    Interactive(fillAlpha = 0.32f, highlightAlpha = 0.14f, elevationDp = 5f),

    /** More opaque — complex info areas needing readability. */
    Thick(fillAlpha = 0.38f, highlightAlpha = 0.15f, elevationDp = 6f),

    /** Floating pill — bottom navigation, floating controls. */
    Floating(fillAlpha = 0.45f, highlightAlpha = 0.18f, elevationDp = 8f),
}

/** CompositionLocal providing environmental adaptive tint to nested [GlassSurface] components. */
val LocalGlassAmbient = compositionLocalOf { Color.Unspecified }

/**
 * Liquid Glass surface — the core visual building block of Misty Weather.
 *
 * @param modifier  Standard Compose modifier.
 * @param level     Opacity tier — see [GlassLevel].
 * @param tint      Adaptive tint. Defaults to [LocalGlassAmbient.current] to seamlessly
 *                  absorb environmental lighting (dawn, sunset, rain, night).
 * @param shape     Surface shape — defaults to [MistyShapes.Glass] (28dp rounded).
 * @param content   Composable content drawn on top of the glass.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.Regular,
    tint: Color = LocalGlassAmbient.current,
    shape: Shape = MistyShapes.Glass,
    content: @Composable () -> Unit = {},
) {
    val isDark = isMistyDark

    // Base fill: in dark mode use deep-navy glass, in light mode use white glass.
    val baseFill: Color = if (isDark) {
        MistyColors.NightBlue.copy(alpha = level.fillAlpha)
    } else {
        MistyColors.White.copy(alpha = level.fillAlpha)
    }

    // Blend in the tint on top of base fill (additive but stays subtle).
    val effectiveFill: Color = if (tint != Color.Unspecified) {
        blendColors(baseFill, tint.copy(alpha = tint.alpha * 0.15f))
    } else {
        baseFill
    }

    val highlightColor = MistyColors.White.copy(alpha = level.highlightAlpha)
    val shadowColor    = if (isDark) {
        MistyColors.DeepNavy.copy(alpha = 0.25f)
    } else {
        MistyColors.BlueGray.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            // Clip to shape first so all drawing respects corners.
            .clip(shape)
            // 1. Base fill + highlight gradient drawn on canvas.
            .drawBehind {
                // Fill
                drawRect(color = effectiveFill)
                // Top highlight: linear gradient white → transparent
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(highlightColor, Color.Transparent),
                        startY = 0f,
                        endY   = size.height * 0.35f,
                    ),
                )
            }
            // 2. Border — 1dp white stroke at 10–18%.
            .border(
                width = MistyDimensions.GlassBorderWidth,
                color = MistyColors.GlassBorder,
                shape = shape,
            )
            // 3. Soft shadow — very low alpha, not Material's typical heavy shadow.
            .shadow(
                elevation        = level.elevationDp.dp,
                shape            = shape,
                ambientColor     = shadowColor,
                spotColor        = shadowColor,
                clip             = false,
            ),
    ) {
        content()
    }
}

// ─── Internal helper ──────────────────────────────────────────────────────────

/**
 * Simple additive color blend: src over dst in straight-alpha compositing.
 * Used to tint glass with a weather-scene color without saturating it.
 */
private fun blendColors(dst: Color, src: Color): Color {
    val srcA = src.alpha
    val dstA = dst.alpha * (1f - srcA)
    val outA = srcA + dstA
    if (outA == 0f) return Color.Transparent
    return Color(
        red   = (src.red   * srcA + dst.red   * dstA) / outA,
        green = (src.green * srcA + dst.green * dstA) / outA,
        blue  = (src.blue  * srcA + dst.blue  * dstA) / outA,
        alpha = outA,
    )
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Glass — Light — All Levels", showBackground = true, backgroundColor = 0xFF6BAED6)
@Composable
private fun GlassLightPreview() {
    MistyWeatherTheme(darkTheme = false) {
        Column(Modifier.padding(16.dp)) {
            GlassLevel.entries.forEach { level ->
                GlassSurface(
                    level    = level,
                    modifier = Modifier.padding(bottom = 8.dp).size(width = 220.dp, height = 56.dp),
                ) {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text  = level.name,
                            style = MistyType.DetailLabel,
                            color = MistyColors.TextOnGlass,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Glass — Dark — All Levels", showBackground = true, backgroundColor = 0xFF172033)
@Composable
private fun GlassDarkPreview() {
    MistyWeatherTheme(darkTheme = true) {
        Column(Modifier.padding(16.dp)) {
            GlassLevel.entries.forEach { level ->
                GlassSurface(
                    level    = level,
                    modifier = Modifier.padding(bottom = 8.dp).size(width = 220.dp, height = 56.dp),
                ) {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text  = level.name,
                            style = MistyType.DetailLabel,
                            color = MistyColors.TextOnGlass,
                        )
                    }
                }
            }
        }
    }
}
