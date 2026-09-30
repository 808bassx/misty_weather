package com.example.mistyweather.weather.icon

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

// ─── Weather Icon Pack Importer ──────────────────────────────────────────────
//
// Phase 4.4-B0: Secure, atomic importer for weather icon pack ZIP archives.
//
// Workflow:
//   1. Unpack stream into isolated temporary directory.
//   2. Enforce limits: Zip Slip prevention, Zip Bomb uncompressed size & entry caps.
//   3. Inspect manifest schema and SVG security.
//   4. If validation PASS: atomically install to official icon pack directory.
//   5. If validation FAIL: wipe temporary directory completely. Zero directory pollution.
//
// ─────────────────────────────────────────────────────────────────────────────

sealed interface IconPackImportResult {
    data class Success(val pack: WeatherIconPack) : IconPackImportResult
    data class InvalidManifest(val reason: String) : IconPackImportResult
    data class UnsafeArchive(val reason: String) : IconPackImportResult
    data class CorruptedArchive(val reason: String) : IconPackImportResult
    data class StorageError(val reason: String) : IconPackImportResult

    val isSuccess: Boolean get() = this is Success
    val errorMessage: String?
        get() = when (this) {
            is Success -> null
            is InvalidManifest -> "Manifest 校验失败: $reason"
            is UnsafeArchive -> "归档安全风险拦截: $reason"
            is CorruptedArchive -> "Zip 归档已损坏或格式错误: $reason"
            is StorageError -> "存储或文件移动异常: $reason"
        }
}

class WeatherIconPackImporter(
    private val stagingBaseDir: File,
    private val officialPacksDir: File,
) {

    init {
        stagingBaseDir.mkdirs()
        officialPacksDir.mkdirs()
    }

    /**
     * Unpacks, validates, and installs an icon pack ZIP archive from an [InputStream].
     */
    fun importFromZipStream(inputStream: InputStream): IconPackImportResult {
        val stagingDir = File(stagingBaseDir, "stage_${UUID.randomUUID()}")
        try {
            stagingDir.mkdirs()

            // 1. Secure stream unzipping
            val unpackResult = safeUnzip(inputStream, stagingDir)
            if (unpackResult is IconPackImportResult) {
                return unpackResult
            }

            // 2. Validate unpacked content
            val (validationResult, manifest) = WeatherIconPackValidator.validateUnpackedIconPack(stagingDir)
            if (validationResult is WeatherIconPackValidator.ValidationResult.Invalid) {
                return IconPackImportResult.InvalidManifest(validationResult.reason)
            }
            if (manifest == null) {
                return IconPackImportResult.InvalidManifest("未能解析有效的 manifest.json")
            }

            // 3. Atomically install into official directory
            val targetDir = File(officialPacksDir, manifest.id)
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }

            val moved = stagingDir.renameTo(targetDir)
            if (!moved) {
                // Fallback copy if cross-filesystem or Windows rename failure
                copyDirectory(stagingDir, targetDir)
                stagingDir.deleteRecursively()
            }

            // 4. Build high-level domain model
            val pack = buildPackFromInstalledDir(targetDir, manifest)
            return IconPackImportResult.Success(pack)

        } catch (e: Exception) {
            return IconPackImportResult.CorruptedArchive("解压或解析过程异常: ${e.message ?: e.javaClass.simpleName}")
        } finally {
            if (stagingDir.exists()) {
                stagingDir.deleteRecursively()
            }
        }
    }

    private fun safeUnzip(inputStream: InputStream, targetDir: File): IconPackImportResult? {
        var totalBytes = 0L
        var totalEntries = 0
        val buffer = ByteArray(8192)

        ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
            while (true) {
                val entry: ZipEntry = zis.nextEntry ?: break
                totalEntries++

                if (totalEntries > WeatherIconPackValidator.MAX_ENTRY_COUNT) {
                    return IconPackImportResult.UnsafeArchive(
                        "条目数量超出上限 (最大 ${WeatherIconPackValidator.MAX_ENTRY_COUNT} 项)"
                    )
                }

                // Check entry name for Zip Slip
                val nameValidation = WeatherIconPackValidator.validateZipEntryName(entry.name, targetDir)
                if (nameValidation is WeatherIconPackValidator.ValidationResult.Invalid) {
                    return IconPackImportResult.UnsafeArchive(nameValidation.reason)
                }

                val outputFile = File(targetDir, entry.name.replace('\\', '/'))

                if (entry.isDirectory) {
                    outputFile.mkdirs()
                } else {
                    outputFile.parentFile?.mkdirs()
                    FileOutputStream(outputFile).use { fos ->
                        var bytesRead: Int
                        while (zis.read(buffer).also { bytesRead = it } != -1) {
                            totalBytes += bytesRead
                            if (totalBytes > WeatherIconPackValidator.MAX_UNCOMPRESSED_TOTAL_BYTES) {
                                return IconPackImportResult.UnsafeArchive(
                                    "解压总大小超出安全上限 (最大 ${WeatherIconPackValidator.MAX_UNCOMPRESSED_TOTAL_BYTES / 1024 / 1024} MB)"
                                )
                            }
                            fos.write(buffer, 0, bytesRead)
                        }
                    }
                }
                zis.closeEntry()
            }
        }

        if (totalEntries == 0) {
            return IconPackImportResult.CorruptedArchive("ZIP 归档为空，未找到任何文件")
        }

        return null
    }

    private fun copyDirectory(source: File, target: File) {
        target.mkdirs()
        source.listFiles()?.forEach { file ->
            val dest = File(target, file.name)
            if (file.isDirectory) {
                copyDirectory(file, dest)
            } else {
                file.copyTo(dest, overwrite = true)
            }
        }
    }

    companion object {
        fun buildPackFromInstalledDir(dir: File, manifest: WeatherIconPackManifest): WeatherIconPack {
            val supportedKeys = mutableSetOf<WeatherIconKey>()
            val relativeMap = mutableMapOf<WeatherIconKey, String>()

            for ((keyStr, path) in manifest.icons) {
                val key = WeatherIconKey.fromSerialName(keyStr)
                if (key != WeatherIconKey.UNKNOWN) {
                    val file = File(dir, path)
                    if (file.exists() && file.isFile) {
                        supportedKeys.add(key)
                        relativeMap[key] = path
                    }
                }
            }

            val previewFile = manifest.preview?.let { File(dir, it) }
            val previewPath = if (previewFile != null && previewFile.exists()) previewFile.absolutePath else null

            return WeatherIconPack(
                id                  = manifest.id,
                name                = manifest.name,
                version             = manifest.version,
                author              = manifest.author,
                license             = manifest.license,
                description         = manifest.description,
                previewPath         = previewPath,
                iconCount           = supportedKeys.size,
                supportedKeys       = supportedKeys,
                source              = IconPackSource.USER_IMPORTED,
                rootDir             = dir,
                iconRelativePaths   = relativeMap,
                sourceUrl           = manifest.sourceUrl,
                licenseUrl          = manifest.licenseUrl,
            )
        }
    }
}
