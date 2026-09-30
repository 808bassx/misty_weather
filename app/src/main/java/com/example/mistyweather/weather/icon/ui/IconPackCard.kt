package com.example.mistyweather.weather.icon.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mistyweather.theme.GlassLevel
import com.example.mistyweather.theme.GlassSurface
import com.example.mistyweather.theme.MistyColors
import com.example.mistyweather.theme.MistyDimensions
import com.example.mistyweather.theme.MistyShapes
import com.example.mistyweather.theme.MistyType
import com.example.mistyweather.weather.icon.WeatherIconKey
import com.example.mistyweather.weather.icon.WeatherIconPack

// ─── Icon Pack List Item Card ────────────────────────────────────────────────
//
// Phase 4.4-B0: Glass surface card for displaying icon pack properties and actions.
//
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IconPackCard(
    pack: WeatherIconPack,
    isActive: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        level    = if (isActive) GlassLevel.Interactive else GlassLevel.Thin,
        shape    = MistyShapes.Medium,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(MistyDimensions.Space16)) {
            // Header Row: Name & Active Indicator
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = pack.name,
                        style = MistyType.SectionHeader,
                        color = MistyColors.TextOnGlass,
                    )
                    Spacer(Modifier.width(MistyDimensions.Space8))
                    Text(
                        text  = "v${pack.version}",
                        style = MistyType.ForecastTime.copy(fontSize = 11.sp),
                        color = MistyColors.TextOnGlassTertiary,
                    )
                }

                if (isActive) {
                    GlassSurface(
                        level = GlassLevel.Floating,
                        shape = MistyShapes.Pill,
                    ) {
                        Text(
                            text     = "✓ 当前使用",
                            style    = MistyType.ForecastTime.copy(fontWeight = FontWeight.Bold),
                            color    = Color(0xFF81C784),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(MistyDimensions.Space8))

            // Author & License info
            val metadataStr = buildString {
                if (!pack.author.isNullOrBlank()) append(pack.author).append(" · ")
                append(pack.coverageText).append(" 图标")
                if (!pack.license.isNullOrBlank()) append(" · ").append(pack.license)
            }
            Text(
                text  = metadataStr,
                style = MistyType.ForecastTime,
                color = MistyColors.TextOnGlassSecondary,
            )

            Spacer(Modifier.height(MistyDimensions.Space12))

            // Mini Icon Preview Row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MistyDimensions.Space12),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                listOf(
                    WeatherIconKey.CLEAR_DAY,
                    WeatherIconKey.PARTLY_CLOUDY_DAY,
                    WeatherIconKey.RAIN,
                    WeatherIconKey.THUNDERSTORM,
                    WeatherIconKey.SNOW,
                ).forEach { key ->
                    GlassSurface(
                        level = GlassLevel.UltraThin,
                        shape = MistyShapes.Small,
                    ) {
                        Box(
                            modifier         = Modifier.padding(6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            WeatherIcon(
                                key      = key,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(MistyDimensions.Space16))

            // Action Buttons Row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                // Preview Button
                GlassSurface(
                    level    = GlassLevel.Thin,
                    shape    = MistyShapes.Pill,
                    modifier = Modifier.clickable(onClick = onPreview),
                ) {
                    Text(
                        text     = "预览全部",
                        style    = MistyType.ForecastTime,
                        color    = MistyColors.TextOnGlass,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }

                if (!isActive) {
                    Spacer(Modifier.width(MistyDimensions.Space10))
                    // Select Button
                    GlassSurface(
                        level    = GlassLevel.Interactive,
                        shape    = MistyShapes.Pill,
                        modifier = Modifier.clickable(onClick = onSelect),
                    ) {
                        Text(
                            text     = "启用此包",
                            style    = MistyType.ForecastTime.copy(fontWeight = FontWeight.Bold),
                            color    = Color(0xFF64B5F6),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        )
                    }
                }

                if (!pack.isBuiltIn) {
                    Spacer(Modifier.width(MistyDimensions.Space10))
                    // Delete Button
                    GlassSurface(
                        level    = GlassLevel.Thin,
                        shape    = MistyShapes.Pill,
                        modifier = Modifier.clickable(onClick = onDelete),
                    ) {
                        Text(
                            text     = "删除",
                            style    = MistyType.ForecastTime,
                            color    = Color(0xFFE57373),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
