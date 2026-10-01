package com.misty.weather.weather.icon.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misty.weather.theme.GlassLevel
import com.misty.weather.theme.GlassSurface
import com.misty.weather.theme.MistyColors
import com.misty.weather.theme.MistyDimensions
import com.misty.weather.theme.MistyShapes
import com.misty.weather.theme.MistyType
import com.misty.weather.weather.icon.IconPackImportResult
import com.misty.weather.weather.icon.WeatherIconPack
import com.misty.weather.weather.icon.WeatherIconPackManager
import kotlinx.coroutines.launch

// ─── Weather Icon Pack Settings Screen ───────────────────────────────────────
//
// Phase 4.4-B0: Management interface for installed and custom weather icon packs.
//
// Features:
//   • Display all installed packs (Misty Default, Meteocons, User Imported).
//   • Storage Access Framework (SAF) ZIP import without broad storage permissions.
//   • Instant preview modal showing all 14 weather conditions and coverage metrics.
//   • Safe deletion with automatic fallback to Misty Default.
//   • Liquid glass styling harmonious with Misty Weather aesthetics.
//
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IconPackSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val manager = remember(context) { WeatherIconPackManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    val installedPacks by manager.installedPacks.collectAsState()
    val activePackId by manager.activePackId.collectAsState()

    var previewingPack by remember { mutableStateOf<WeatherIconPack?>(null) }
    var deletingPack by remember { mutableStateOf<WeatherIconPack?>(null) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }
    var importSuccessMessage by remember { mutableStateOf<String?>(null) }

    // SAF Document Picker launcher for ZIP files
    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val result = manager.importPack(stream)
                        when (result) {
                            is IconPackImportResult.Success -> {
                                importSuccessMessage = "成功导入图标包: ${result.pack.name} (${result.pack.coverageText})"
                            }
                            else -> {
                                importErrorMessage = result.errorMessage ?: "导入失败"
                            }
                        }
                    } ?: run {
                        importErrorMessage = "无法打开选择的文件数据流"
                    }
                } catch (e: Exception) {
                    importErrorMessage = "读取文件异常: ${e.message}"
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MistyColors.SecondaryBg,
                        MistyColors.SecondaryBgMid,
                        MistyColors.SecondaryBgDark,
                    ),
                ),
            )
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MistyDimensions.Space16),
        ) {
            Spacer(Modifier.height(MistyDimensions.Space16))

            // Top Bar: Back Button & Screen Title
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassSurface(
                    level    = GlassLevel.Thin,
                    shape    = MistyShapes.Pill,
                    modifier = Modifier.clickable(onClick = onNavigateBack),
                ) {
                    Text(
                        text     = "← 返回",
                        style    = MistyType.ForecastTime,
                        color    = MistyColors.TextOnGlass,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }

                Spacer(Modifier.width(MistyDimensions.Space16))

                Text(
                    text  = "天气图标管理",
                    style = MistyType.ScreenTitle,
                    color = MistyColors.TextOnGlass,
                )
            }

            Spacer(Modifier.height(MistyDimensions.Space20))

            // Import Button Banner
            GlassSurface(
                level    = GlassLevel.Interactive,
                shape    = MistyShapes.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        zipPickerLauncher.launch(
                            arrayOf(
                                "application/zip",
                                "application/x-zip-compressed",
                                "application/octet-stream",
                            ),
                        )
                    },
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(MistyDimensions.Space16),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = "+ 导入第三方图标包",
                            style = MistyType.SectionHeader.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                            color = MistyColors.TextOnGlass,
                        )
                        Spacer(Modifier.height(MistyDimensions.Space4))
                        Text(
                            text  = "支持包含 manifest.json 与静态 SVG 的 ZIP 归档",
                            style = MistyType.ForecastTime,
                            color = MistyColors.TextOnGlassSecondary,
                        )
                    }
                    Spacer(Modifier.width(MistyDimensions.Space12))

                    GlassSurface(
                        level = GlassLevel.Floating,
                        shape = MistyShapes.Pill,
                    ) {
                        Text(
                            text     = "选择 ZIP",
                            style    = MistyType.ForecastTime.copy(fontWeight = FontWeight.Bold),
                            color    = MistyColors.AccentSky,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(MistyDimensions.Space20))

            Text(
                text  = "已安装图标包 (${installedPacks.size})",
                style = MistyType.ForecastTime.copy(fontWeight = FontWeight.SemiBold),
                color = MistyColors.TextOnGlassSecondary,
            )

            Spacer(Modifier.height(MistyDimensions.Space10))

            // Installed Icon Packs List
            LazyColumn(
                modifier              = Modifier.fillMaxSize(),
                verticalArrangement   = Arrangement.spacedBy(MistyDimensions.Space12),
            ) {
                items(installedPacks, key = { it.id }) { pack ->
                    IconPackCard(
                        pack     = pack,
                        isActive = pack.id == activePackId,
                        onSelect = {
                            scope.launch { manager.selectPack(pack.id) }
                        },
                        onPreview = {
                            previewingPack = pack
                        },
                        onDelete = {
                            deletingPack = pack
                        },
                    )
                }

                item {
                    Spacer(Modifier.height(MistyDimensions.Space48))
                }
            }
        }

        // Preview Dialog
        previewingPack?.let { pack ->
            IconPackPreviewDialog(
                pack      = pack,
                onDismiss = { previewingPack = null },
            )
        }

        // Delete Confirmation Dialog
        deletingPack?.let { pack ->
            AlertDialog(
                onDismissRequest = { deletingPack = null },
                title = {
                    Text(
                        text = "删除图标包 “${pack.name}”？",
                        color = MistyColors.TextOnGlass,
                    )
                },
                text = {
                    Text(
                        text = if (pack.id == activePackId) {
                            "当前正在使用此图标包。删除后将自动切换回系统默认 Misty Default。"
                        } else {
                            "删除后将从本地移除此图标包及其全部矢量资产。"
                        },
                        color = MistyColors.TextOnGlassSecondary,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val idToDelete = pack.id
                            deletingPack = null
                            scope.launch { manager.deletePack(idToDelete) }
                        },
                    ) {
                        Text("确认删除", color = MistyColors.StatusDanger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingPack = null }) {
                        Text("取消", color = MistyColors.TextOnGlassSecondary)
                    }
                },
                containerColor = MistyColors.SecondaryBg,
            )
        }

        // Error Feedback Dialog
        importErrorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { importErrorMessage = null },
                title = { Text("无法导入图标包", color = MistyColors.StatusDanger) },
                text  = { Text(message, color = MistyColors.TextOnGlassSecondary) },
                confirmButton = {
                    TextButton(onClick = { importErrorMessage = null }) {
                        Text("知道了", color = MistyColors.TextOnGlass)
                    }
                },
                containerColor = MistyColors.SecondaryBg,
            )
        }

        // Success Feedback Dialog
        importSuccessMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { importSuccessMessage = null },
                title = { Text("导入成功", color = MistyColors.StatusSuccess) },
                text  = { Text(message, color = MistyColors.TextOnGlassSecondary) },
                confirmButton = {
                    TextButton(onClick = { importSuccessMessage = null }) {
                        Text("确定", color = MistyColors.TextOnGlass)
                    }
                },
                containerColor = MistyColors.SecondaryBg,
            )
        }
    }
}
