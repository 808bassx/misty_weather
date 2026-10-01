package com.misty.weather.weather.icon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.misty.weather.theme.GlassLevel
import com.misty.weather.theme.GlassSurface
import com.misty.weather.theme.MistyColors
import com.misty.weather.theme.MistyDimensions
import com.misty.weather.theme.MistyShapes
import com.misty.weather.theme.MistyType
import com.misty.weather.weather.icon.WeatherIconKey
import com.misty.weather.weather.icon.WeatherIconPack

// ─── Icon Pack Preview Dialog ────────────────────────────────────────────────
//
// Phase 4.4-B0: Visual inspection modal for all weather conditions.
//
// Highlights:
//   • Full 14-condition preview grid.
//   • Explicit coverage metrics (e.g. "覆盖率 14 / 14").
//   • Visual badge for fallback icons when a pack is incomplete.
//   • Provenance, license, and author attribution.
//
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IconPackPreviewDialog(
    pack: WeatherIconPack,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            GlassSurface(
                level    = GlassLevel.Regular,
                shape    = MistyShapes.Medium,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
                    .padding(MistyDimensions.Space8),
            ) {
                Column(
                    modifier = Modifier.padding(MistyDimensions.Space20),
                ) {
                    // Header: Title & Close
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text  = pack.name,
                                style = MistyType.SectionHeader,
                                color = MistyColors.TextOnGlass,
                            )
                            Spacer(Modifier.height(MistyDimensions.Space4))
                            Text(
                                text  = (pack.author?.let { "作者: $it · " } ?: "") +
                                        (pack.license?.let { "许可: $it" } ?: ""),
                                style = MistyType.ForecastTime,
                                color = MistyColors.TextOnGlassSecondary,
                            )
                        }
                        Text(
                            text     = "关闭",
                            style    = MistyType.ForecastTime,
                            color    = MistyColors.TextOnGlassSecondary,
                            modifier = Modifier.clickable(onClick = onDismiss),
                        )
                    }

                    Spacer(Modifier.height(MistyDimensions.Space12))

                    // Coverage badge pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        GlassSurface(
                            level = GlassLevel.Thin,
                            shape = MistyShapes.Pill,
                        ) {
                            Text(
                                text     = if (pack.isCompleteCoverage) "✓ 完整覆盖 ${pack.coverageText}"
                                           else "⚡ 覆盖 ${pack.coverageText} (部分 Fallback)",
                                style    = MistyType.ForecastTime,
                                color    = if (pack.isCompleteCoverage) Color(0xFF81C784) else Color(0xFFFFB74D),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(MistyDimensions.Space16))

                    // Icons Grid (All 14 standard keys)
                    LazyVerticalGrid(
                        columns               = GridCells.Fixed(3),
                        modifier              = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        horizontalArrangement = Arrangement.spacedBy(MistyDimensions.Space10),
                        verticalArrangement   = Arrangement.spacedBy(MistyDimensions.Space10),
                    ) {
                        items(WeatherIconKey.requiredKeys.toList()) { key ->
                            val isSupported = pack.supportedKeys.contains(key)
                            GlassSurface(
                                level = GlassLevel.UltraThin,
                                shape = MistyShapes.Small,
                            ) {
                                Column(
                                    modifier              = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    WeatherIcon(
                                        key      = key,
                                        modifier = Modifier.size(36.dp),
                                    )
                                    Spacer(Modifier.height(MistyDimensions.Space6))
                                    Text(
                                        text      = key.displayName,
                                        style     = MistyType.ForecastTime.copy(fontSize = 11.sp),
                                        color     = MistyColors.TextOnGlassSecondary,
                                        textAlign = TextAlign.Center,
                                        maxLines  = 1,
                                    )
                                    if (!isSupported && !pack.isBuiltIn) {
                                        Spacer(Modifier.height(MistyDimensions.Space2))
                                        Text(
                                            text      = "Fallback",
                                            style     = MistyType.ForecastTime.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                            color     = Color(0xFFFFB74D),
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!pack.description.isNullOrBlank()) {
                        Spacer(Modifier.height(MistyDimensions.Space12))
                        Text(
                            text  = pack.description,
                            style = MistyType.ForecastTime.copy(fontSize = 11.sp),
                            color = MistyColors.TextOnGlassTertiary,
                        )
                    }
                }
            }
        }
    }
}
