package com.example.mistyweather.weather.scene

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.mistyweather.weather.model.CloudEnvironment
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ─── Cloud Layer ─────────────────────────────────────────────────────────────
//
// Phase 2 Layer 2: Parallax cloud system.
//
// Three cloud tiers drawn on a single Canvas pass:
//   Far clouds   — slower drift, smaller, top-third of screen.
//   Mid clouds   — normal drift, primary blob layer.
//   Near clouds  — faster drift, larger blobs near screen centre.
//
// Each tier is horizontally offset by cloudDrift * speed factor.
// Cloud morphing (cloudShape) subtly warps the ellipse radii, giving
// blobs an organic, slow-breathing quality.
//
// Performance: All Canvas draw calls in one frame, zero object allocation.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders three tiers of softly drifting cloud blobs.
 *
 * @param clouds      Lerped [CloudEnvironment] from [rememberWeatherTransition].
 * @param cloudDrift  Normalised 0→1 horizontal scroll from [WeatherMotion.cloudDrift].
 * @param cloudShape  Normalised 0→1 morphing clock from [WeatherMotion.cloudShape].
 * @param modifier    Should fill the parent.
 */
@Composable
fun CloudLayer(
    clouds: CloudEnvironment,
    cloudDrift: Float,
    cloudShape: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawClouds(clouds, cloudDrift, cloudShape)
    }
}

internal fun DrawScope.drawClouds(
    clouds: CloudEnvironment,
    drift: Float,
    shape: Float,
) {
    if (clouds.density <= 0f) return

    val w = size.width
    val h = size.height
    val d = clouds.density

    // ── Far cloud tier (z = -2) — slow, top 20% of screen ──────────────────
    val farOffset = (drift * clouds.speed * 3.0f * w) % (w * 2f)
    drawCloudTier(
        blobs = farCloudBlobs,
        xOffset   = farOffset,
        yBand     = 0.05f to 0.25f,
        width     = w,
        height    = h,
        blobColor = clouds.blobColor.copy(alpha = 0.18f * d),
        shadow    = clouds.shadowColor.copy(alpha = 0.08f * d),
        shape     = shape,
        scaleY    = 0.65f,
    )

    // ── Mid cloud tier (z = 0) — standard drift ──────────────────────────────
    val midOffset = (drift * clouds.speed * 1.0f * w) % (w * 2f)
    drawCloudTier(
        blobs     = midCloudBlobs,
        xOffset   = midOffset,
        yBand     = 0.10f to 0.48f,
        width     = w,
        height    = h,
        blobColor = clouds.blobColor.copy(alpha = 0.32f * d),
        shadow    = clouds.shadowColor.copy(alpha = 0.14f * d),
        shape     = shape,
        scaleY    = 1.0f,
    )

    // ── Near cloud tier (z = +1) — faster drift, larger blobs ───────────────
    // Only rendered when density is above a threshold (partly cloudy +)
    if (d > 0.30f) {
        val nearOffset = (drift * clouds.speed * 1.6f * w) % (w * 2f)
        drawCloudTier(
            blobs     = nearCloudBlobs,
            xOffset   = nearOffset,
            yBand     = 0.05f to 0.35f,
            width     = w,
            height    = h,
            blobColor = clouds.blobColor.copy(alpha = 0.25f * d),
            shadow    = clouds.shadowColor.copy(alpha = 0.10f * d),
            shape     = shape,
            scaleY    = 1.25f,
        )
    }

    // ── High-altitude cirrus streaks ─────────────────────────────────────────
    if (clouds.highAltitude) {
        drawCirrus(w, h, drift * clouds.speed, clouds.blobColor)
    }

    // ── Bottom mist wash ─────────────────────────────────────────────────────
    val mistAlpha = (d - 0.3f).coerceAtLeast(0f) * 0.22f
    if (mistAlpha > 0f) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, clouds.blobColor.copy(alpha = mistAlpha)),
                startY = h * 0.70f,
                endY   = h,
            ),
        )
    }
}

// ─── Cloud Blob Descriptors ───────────────────────────────────────────────────

/** Normalised (cx, cy, rx, ry) blob descriptors per tier. */
private data class BlobDesc(val cx: Float, val cy: Float, val rx: Float, val ry: Float)

private val farCloudBlobs = listOf(
    BlobDesc(0.15f, 0.08f, 0.28f, 0.06f),
    BlobDesc(0.55f, 0.12f, 0.32f, 0.07f),
    BlobDesc(0.90f, 0.07f, 0.26f, 0.05f),
    BlobDesc(1.30f, 0.10f, 0.30f, 0.06f),
)

private val midCloudBlobs = listOf(
    BlobDesc(0.20f, 0.22f, 0.48f, 0.10f),
    BlobDesc(0.65f, 0.18f, 0.55f, 0.12f),
    BlobDesc(1.10f, 0.28f, 0.50f, 0.11f),
    BlobDesc(0.42f, 0.35f, 0.40f, 0.09f),
    BlobDesc(0.85f, 0.30f, 0.45f, 0.10f),
)

private val nearCloudBlobs = listOf(
    BlobDesc(0.10f, 0.15f, 0.55f, 0.13f),
    BlobDesc(0.68f, 0.10f, 0.60f, 0.14f),
    BlobDesc(1.25f, 0.20f, 0.50f, 0.12f),
)

// ─── Tier Rendering ──────────────────────────────────────────────────────────

private fun DrawScope.drawCloudTier(
    blobs: List<BlobDesc>,
    xOffset: Float,
    yBand: Pair<Float, Float>,
    width: Float,
    height: Float,
    blobColor: Color,
    shadow: Color,
    shape: Float,
    scaleY: Float,
) {
    val yMin = yBand.first
    val yMax = yBand.second

    for (blob in blobs) {
        // Apply horizontal scroll — tile across [-1, 2] screen widths
        val scrolledX = ((blob.cx * width - xOffset) % (width * 2f) + width * 2f) % (width * 2f)
        if (scrolledX < -blob.rx * width || scrolledX > width + blob.rx * width) continue

        val cx = scrolledX
        val cy = height * (yMin + blob.cy * (yMax - yMin))
        val rx = blob.rx * width
        // Morph ry slightly: ±8% based on shape clock
        val morphFactor = 1f + (shape - 0.5f) * 0.16f
        val ry = blob.ry * height * scaleY * morphFactor

        // Shadow blob slightly below and offset
        drawCloudBlob(cx + rx * 0.04f, cy + ry * 0.18f, rx * 0.96f, ry * 0.80f, shadow)
        // Main blob
        drawCloudBlob(cx, cy, rx, ry, blobColor)
    }
}

// ─── Cirrus Streaks ───────────────────────────────────────────────────────────

private fun DrawScope.drawCirrus(w: Float, h: Float, drift: Float, color: Color) {
    val streakY    = h * 0.04f
    val streakAlpha = 0.12f
    val streakWidth = w * 0.5f
    val offset = (drift * w * 2f) % (w * 1.5f)

    for (i in 0..2) {
        val x = (w * (0.05f + i * 0.45f) - offset) % (w * 1.5f)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    color.copy(alpha = streakAlpha),
                    color.copy(alpha = streakAlpha * 0.5f),
                    Color.Transparent,
                ),
                startX = x,
                endX   = x + streakWidth,
            ),
            start = Offset(x, streakY + i * h * 0.018f),
            end   = Offset(x + streakWidth, streakY + i * h * 0.015f),
            strokeWidth = 4f,
        )
    }
}

// ─── Shared: Cloud Blob ───────────────────────────────────────────────────────

/**
 * Draws a single soft radial-gradient cloud blob (ellipse via canvas scale).
 * No hard edges — alpha falls smoothly to transparent at the perimeter.
 */
internal fun DrawScope.drawCloudBlob(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    color: Color,
) {
    if (ry <= 0f || rx <= 0f) return
    withTransform(
        transformBlock = { scale(scaleX = rx / ry, scaleY = 1f, pivot = Offset(cx, cy)) },
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.00f to color,
                    0.55f to color.copy(alpha = color.alpha * 0.55f),
                    1.00f to Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = ry,
            ),
            radius = ry,
            center = Offset(cx, cy),
        )
    }
}
