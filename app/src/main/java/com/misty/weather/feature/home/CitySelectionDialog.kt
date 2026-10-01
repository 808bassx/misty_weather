package com.misty.weather.feature.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.misty.weather.theme.GlassLevel
import com.misty.weather.theme.GlassSurface
import com.misty.weather.theme.MistyColors
import com.misty.weather.theme.MistyDimensions
import com.misty.weather.theme.MistyShapes
import com.misty.weather.theme.MistyType
import com.misty.weather.weather.city.City

// ─── City Selection Dialog (Liquid Glass Modal) ───────────────────────────────
//
// Phase 4.3: User City Selection & Management Modal.
//
// Displays:
//   • [使用我的位置] on-demand location action
//   • Favorite cities list with active indicator
//   • Quick city search and addition
//
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CitySelectionDialog(
    cities: List<City>,
    currentCity: City,
    onSelectCity: (City) -> Unit,
    onUseMyLocation: () -> Unit,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit = {},
    searchResults: List<City> = emptyList(),
) {
    var searchQuery by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MistyColors.ScrimDark)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            GlassSurface(
                level    = GlassLevel.Modal,
                shape    = MistyShapes.Medium,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) {} // Prevent click-through dismissal
                    .padding(MistyDimensions.Space8),
            ) {
                Column(
                    modifier = Modifier.padding(MistyDimensions.Space20),
                ) {
                    // Title & Close Button
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text(
                            text  = "城市管理",
                            style = MistyType.SectionHeader,
                            color = MistyColors.TextOnGlass,
                        )
                        Text(
                            text     = "关闭",
                            style    = MistyType.ForecastTime,
                            color    = MistyColors.TextOnGlassSecondary,
                            modifier = Modifier.clickable(onClick = onDismiss),
                        )
                    }

                    Spacer(Modifier.height(MistyDimensions.Space16))

                    // "使用我的位置" On-Demand Button (Zero unsolicited popups)
                    GlassSurface(
                        level    = GlassLevel.Thin,
                        shape    = MistyShapes.Pill,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUseMyLocation()
                                onDismiss()
                            },
                    ) {
                        Row(
                            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text  = "📍",
                                style = MistyType.ForecastTime,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text  = "使用我的位置",
                                style = MistyType.ForecastTime,
                                color = MistyColors.TextOnGlass,
                            )
                        }
                    }

                    Spacer(Modifier.height(MistyDimensions.Space16))

                    // City Search Bar
                    GlassSurface(
                        level    = GlassLevel.UltraThin,
                        shape    = MistyShapes.Small,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        BasicTextField(
                            value         = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                onSearch(it)
                            },
                            singleLine    = true,
                            cursorBrush   = SolidColor(MistyColors.TextOnGlass),
                            textStyle     = MistyType.ForecastTime.copy(color = MistyColors.TextOnGlass),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text  = "搜索城市 (例如: 巴黎, 伦敦, tokyo)...",
                                            style = MistyType.ForecastTime,
                                            color = MistyColors.TextOnGlassSecondary,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                    }

                    Spacer(Modifier.height(MistyDimensions.Space12))

                    // Cities list (shows search results if searching, otherwise favorites)
                    val displayList = if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                        searchResults
                    } else {
                        cities
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                    ) {
                        items(displayList, key = { it.id }) { city ->
                            val isSelected = city.id == currentCity.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectCity(city)
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text  = city.name + if (city.isCurrentLocation) " (当前位置)" else "",
                                        style = MistyType.ForecastTime,
                                        color = if (isSelected) MistyColors.TextOnGlass else MistyColors.TextOnGlassSecondary,
                                    )
                                    Text(
                                        text  = city.country,
                                        style = MistyType.DetailLabel,
                                        color = MistyColors.TextOnGlassSecondary.copy(alpha = 0.7f),
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text  = "✓",
                                        style = MistyType.ForecastTime,
                                        color = MistyColors.TextOnGlass,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
