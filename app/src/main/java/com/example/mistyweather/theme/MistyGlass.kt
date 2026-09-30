package com.example.mistyweather.theme

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
import androidx.compose.ui.unit.dp

// ─── Misty Liquid Glass System ────────────────────────────────────────────────
//
// Core Principle: "Glass is Material, not Decoration."
//
// Glass is an atmospheric refractive lens that lives inside the weather scene.
// It absorbs the chromatic temperature of the sky (LocalGlassAmbient).
//
// 5 Tiers:
//   Level 0: None         — pure atmosphere, zero container
//   Level 1: Subtle       — light veil for inline grouping, no elevation
//   Level 2: Regular      — structured panels (forecast ribbons, card substrates)
//   Level 3: Interactive  — active / focused cards and clickable elements
//   Level 4: Floating     — floating controls and bottom navigation pill
//   Level 5: Modal        — dialogs and bottom sheets with deep refraction
//
// ─────────────────────────────────────────────────────────────────────────────

/** Opacity & elevation levels that define how each glass tier behaves. */
enum class GlassLevel(
    /** Base fill alpha (0f–1f). */
    val fillAlpha: Float,
    /** Highlight bar alpha at the top edge. */
    val highlightAlpha: Float,
    /** Border stroke alpha. */
    val borderAlpha: Float,
    /** Shadow elevation in dp. */
    val elevationDp: Float,
) {
    /** Level 0: Pure atmosphere — no container, content projected directly on sky. */
    None(fillAlpha = 0.00f, highlightAlpha = 0.00f, borderAlpha = 0.00f, elevationDp = 0f),

    /** Level 1: Subtle — barely-there veil for grouping, 0dp shadow, hairline border. */
    Subtle(fillAlpha = 0.08f, highlightAlpha = 0.05f, borderAlpha = 0.06f, elevationDp = 0f),

    /** Level 2: Regular — standard weather card panel, soft 2dp ambient shadow. */
    Regular(fillAlpha = 0.18f, highlightAlpha = 0.09f, borderAlpha = 0.10f, elevationDp = 2f),

    /** Level 3: Interactive — active selection state, primary cards, 4dp shadow. */
    Interactive(fillAlpha = 0.26f, highlightAlpha = 0.12f, borderAlpha = 0.14f, elevationDp = 4f),

    /** Level 4: Floating — bottom nav pill, floating chips, 8dp ambient elevation. */
    Floating(fillAlpha = 0.36f, highlightAlpha = 0.16f, borderAlpha = 0.16f, elevationDp = 8f),

    /** Level 5: Modal — focused dialogs and sheets with deep ambient presence. */
    Modal(fillAlpha = 0.44f, highlightAlpha = 0.18f, borderAlpha = 0.20f, elevationDp = 12f),

    // ── Legacy Aliases (preserved for binary and source compatibility) ─────────
    @Deprecated("Use Subtle instead", ReplaceWith("Subtle"))
    UltraThin(fillAlpha = 0.08f, highlightAlpha = 0.05f, borderAlpha = 0.06f, elevationDp = 0f),

    @Deprecated("Use Regular instead", ReplaceWith("Regular"))
    Thin(fillAlpha = 0.18f, highlightAlpha = 0.09f, borderAlpha = 0.10f, elevationDp = 2f),

    @Deprecated("Use Floating instead", ReplaceWith("Floating"))
    Thick(fillAlpha = 0.36f, highlightAlpha = 0.16f, borderAlpha = 0.16f, elevationDp = 6f);
}

/** CompositionLocal providing environmental adaptive tint to nested [GlassSurface] components. */
val LocalGlassAmbient = compositionLocalOf { Color.Unspecified }

/**
 * Liquid Glass surface — the core visual building block of Misty Weather.
 *
 * @param modifier  Standard Compose modifier.
 * @param level     Opacity & depth tier — see [GlassLevel].
 * @param tint      Adaptive tint. Defaults to [LocalGlassAmbient.current] to seamlessly
 *                  absorb environmental lighting (dawn, sunset, rain, night).
 * @param shape     Surface shape — defaults to [MistyShapes.Glass] (24–28dp rounded).
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
    if (level == GlassLevel.None) {
        Box(modifier = modifier) { content() }
        return
    }

    val isDark = isMistyDark

    // Base fill: in dark mode use atmospheric deep-navy glass, in light mode use white glass.
    val baseFill: Color = if (isDark) {
        MistyColors.NightBlue.copy(alpha = level.fillAlpha)
    } else {
        MistyColors.White.copy(alpha = level.fillAlpha)
    }

    // Blend in the ambient tint on top of base fill (additive but stays subtle, max 15% weight).
    val effectiveFill: Color = if (tint != Color.Unspecified) {
        blendColors(baseFill, tint.copy(alpha = tint.alpha * 0.15f))
    } else {
        baseFill
    }

    // Specular highlight: soft directional linear gradient from top edge
    val highlightColor = MistyColors.White.copy(alpha = level.highlightAlpha)

    // Border: hairline stroke with tier-specific restrained alpha (never harsh opaque white)
    val borderColor = if (isDark) {
        MistyColors.White.copy(alpha = level.borderAlpha)
    } else {
        MistyColors.White.copy(alpha = level.borderAlpha * 1.2f)
    }

    // Ambient shadow: low alpha tinted to background hue (never muddy pitch black)
    val shadowColor = if (isDark) {
        MistyColors.DeepNavy.copy(alpha = 0.30f)
    } else {
        MistyColors.BlueGray.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            // Clip to shape first so all drawing respects corners.
            .clip(shape)
            // 1. Base fill + directional specular highlight drawn on canvas.
            .drawBehind {
                // Base Fill
                drawRect(color = effectiveFill)
                // Directional Top Highlight: linear gradient white → transparent
                if (level.highlightAlpha > 0f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(highlightColor, Color.Transparent),
                            startY = 0f,
                            endY   = size.height * 0.35f,
                        ),
                    )
                }
            }
            // 2. Border — hairline stroke with tier-specific alpha.
            .border(
                width = MistyDimensions.GlassBorderWidth,
                color = borderColor,
                shape = shape,
            )
            // 3. Ambient shadow — subtle elevation without heavy offset.
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
 * Additive straight-alpha color blending.
 * Used to tint glass with weather scene ambiance without oversaturating it.
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
            listOf(
                GlassLevel.None,
                GlassLevel.Subtle,
                GlassLevel.Regular,
                GlassLevel.Interactive,
                GlassLevel.Floating,
                GlassLevel.Modal,
            ).forEach { level ->
                GlassSurface(
                    level    = level,
                    modifier = Modifier.padding(bottom = 8.dp).size(width = 240.dp, height = 52.dp),
                ) {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text  = "Level: ${level.name}",
                            style = MistyType.DetailRow,
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
            listOf(
                GlassLevel.None,
                GlassLevel.Subtle,
                GlassLevel.Regular,
                GlassLevel.Interactive,
                GlassLevel.Floating,
                GlassLevel.Modal,
            ).forEach { level ->
                GlassSurface(
                    level    = level,
                    modifier = Modifier.padding(bottom = 8.dp).size(width = 240.dp, height = 52.dp),
                ) {
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text  = "Level: ${level.name}",
                            style = MistyType.DetailRow,
                            color = MistyColors.TextOnGlass,
                        )
                    }
                }
            }
        }
    }
}
