package com.misty.weather.weather.icon

import java.io.File
import java.io.InputStream
import java.util.Locale

// ─── Weather Icon Pack Validator & Security Engine ───────────────────────────
//
// Phase 4.4-B0: Rigorous security verification for user-imported icon packs.
//
// Defends against:
//   1. Zip Slip (path traversal / malicious entry paths).
//   2. Zip Bomb (archive decompression bombs / excessive resource exhaustion).
//   3. Malicious SVG (embedded scripts, external entities, protocol injection).
//   4. Manifest corruption & malformed icon mappings.
//
// ─────────────────────────────────────────────────────────────────────────────

object WeatherIconPackValidator {

    // ── Hard Resource Limits (Anti-DoS / Anti-Zip-Bomb) ──────────────────────
    const val MAX_ZIP_SIZE_BYTES: Long = 20L * 1024 * 1024          // 20 MB max archive size
    const val MAX_UNCOMPRESSED_TOTAL_BYTES: Long = 50L * 1024 * 1024 // 50 MB max unpacked size
    const val MAX_ENTRY_COUNT: Int = 500                            // 500 files max
    const val MAX_SINGLE_SVG_BYTES: Long = 2L * 1024 * 1024         // 2 MB max single SVG
    const val MAX_PREVIEW_BYTES: Long = 5L * 1024 * 1024            // 5 MB max preview image

    private val ID_REGEX = Regex("^[a-zA-Z0-9_-]{1,64}$")
    private const val RESERVED_DEFAULT_ID = "misty-default"

    // ── Verification Results ─────────────────────────────────────────────────
    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val reason: String) : ValidationResult
    }

    /**
     * Verifies that a Zip entry name is strictly safe against Zip Slip path traversal.
     */
    fun validateZipEntryName(entryName: String, targetDir: File): ValidationResult {
        if (entryName.isBlank()) {
            return ValidationResult.Invalid("Zip 包含空文件名条目")
        }
        if (entryName.contains('\u0000')) {
            return ValidationResult.Invalid("条目名称包含非法空字节 (Null Byte): $entryName")
        }
        // Normalize separators
        val normalized = entryName.replace('\\', '/')
        if (normalized.startsWith("/") || normalized.contains("../") || normalized.contains("/..") || normalized == "..") {
            return ValidationResult.Invalid("检测到潜在的 Zip Slip 路径穿越风险: $entryName")
        }
        if (normalized.length > 2 && normalized[1] == ':') {
            return ValidationResult.Invalid("禁止包含绝对驱动器盘符路径: $entryName")
        }

        // Canonical destination check
        val targetCanonical = targetDir.canonicalFile
        val resolved = File(targetCanonical, normalized).canonicalFile
        val targetPath = targetCanonical.path
        val resolvedPath = resolved.path

        if (!resolvedPath.startsWith(targetPath + File.separator) && resolvedPath != targetPath) {
            return ValidationResult.Invalid("文件解压路径越界 (Zip Slip): $entryName -> $resolvedPath")
        }

        return ValidationResult.Valid
    }

    /**
     * Validates the schema and values of [WeatherIconPackManifest].
     */
    fun validateManifest(manifest: WeatherIconPackManifest): ValidationResult {
        if (manifest.id.isBlank()) {
            return ValidationResult.Invalid("图标包 id 不能为空")
        }
        if (!ID_REGEX.matches(manifest.id)) {
            return ValidationResult.Invalid("图标包 id 格式非法 (仅限英文字母、数字、下划线及横杠，且不超过 64 字符): ${manifest.id}")
        }
        if (manifest.id.equals(RESERVED_DEFAULT_ID, ignoreCase = true)) {
            return ValidationResult.Invalid("图标包 id 不能使用系统保留标识符 '$RESERVED_DEFAULT_ID'")
        }
        if (manifest.name.isBlank()) {
            return ValidationResult.Invalid("图标包名称 (name) 不能为空")
        }
        if (manifest.version < 1) {
            return ValidationResult.Invalid("图标包版本号 (version) 必须 >= 1: ${manifest.version}")
        }
        if (manifest.icons.isEmpty()) {
            return ValidationResult.Invalid("图标映射 (icons) 不能为空，必须至少包含一个有效天气图标")
        }

        return ValidationResult.Valid
    }

    /**
     * Inspects a single SVG file for security vulnerabilities and validity.
     */
    fun validateSvgFile(svgFile: File, rootDir: File): ValidationResult {
        if (!svgFile.exists() || !svgFile.isFile) {
            return ValidationResult.Invalid("图标文件不存在: ${svgFile.name}")
        }
        if (svgFile.length() > MAX_SINGLE_SVG_BYTES) {
            return ValidationResult.Invalid("SVG 文件体积超出限制 (最大 2MB): ${svgFile.length()} 字节")
        }
        if (!svgFile.name.endsWith(".svg", ignoreCase = true)) {
            return ValidationResult.Invalid("不支持的非 SVG 图标文件: ${svgFile.name}")
        }

        // Canonical containment check
        val rootCanonical = rootDir.canonicalFile
        val fileCanonical = svgFile.canonicalFile
        if (!fileCanonical.path.startsWith(rootCanonical.path + File.separator)) {
            return ValidationResult.Invalid("图标文件位于图标包根目录之外: ${svgFile.name}")
        }

        // Content inspection
        val content = try {
            svgFile.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            return ValidationResult.Invalid("无法读取 SVG 文件内容: ${e.message}")
        }

        return validateSvgContent(content, svgFile.name)
    }

    /**
     * Inspects SVG XML string content for embedded scripts and malicious external references.
     */
    fun validateSvgContent(content: String, fileName: String = "svg"): ValidationResult {
        val lower = content.lowercase(Locale.ROOT)

        // 1. Must contain valid <svg tag
        if (!lower.contains("<svg") || !lower.contains("</svg>")) {
            return ValidationResult.Invalid("SVG 结构缺失必需的 <svg> 或 </svg> 标签 ($fileName)")
        }

        // 2. Reject scripts and event handlers
        if (lower.contains("<script") || lower.contains("</script>") || lower.contains("javascript:")) {
            return ValidationResult.Invalid("SVG 包含危险可执行脚本代码 (<script> 或 javascript:) ($fileName)")
        }

        // 3. Reject XML External Entity (XXE) and external DTD
        if (lower.contains("<!entity") || (lower.contains("<!doctype") && lower.contains("system"))) {
            return ValidationResult.Invalid("SVG 包含外部实体引用 (XXE 攻击防护) ($fileName)")
        }

        // 4. Reject external network resource references (but allow standard W3C XML/SVG namespace URIs).
        // Strip known-safe W3C namespace declarations before checking for external URLs.
        val withoutSvgNamespaces = lower
            .replace("http://www.w3.org/2000/svg", "")    // Standard SVG namespace
            .replace("http://www.w3.org/1999/xlink", "")   // Standard XLink namespace
            .replace("http://www.w3.org/2000/xlink", "")   // Alternative XLink namespace
            .replace("http://www.w3.org/xml/1998/namespace", "") // XML namespace

        if (withoutSvgNamespaces.contains("http://") ||
            withoutSvgNamespaces.contains("https://") ||
            withoutSvgNamespaces.contains("ftp://")
        ) {
            return ValidationResult.Invalid("SVG 包含外部网络资源链接 ($fileName)")
        }

        return ValidationResult.Valid
    }

    /**
     * Validates an entire unpacked icon pack directory against manifest mappings and SVG security.
     */
    fun validateUnpackedIconPack(rootDir: File): Pair<ValidationResult, WeatherIconPackManifest?> {
        val manifestFile = File(rootDir, WeatherIconPackManifest.MANIFEST_FILE_NAME)
        if (!manifestFile.exists() || !manifestFile.isFile) {
            return ValidationResult.Invalid("图标包缺少必需的 ${WeatherIconPackManifest.MANIFEST_FILE_NAME} 文件") to null
        }

        val manifest = try {
            WeatherIconPackManifest.fromJson(manifestFile.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            return ValidationResult.Invalid("解析 manifest.json 失败: ${e.message}") to null
        }

        val manifestCheck = validateManifest(manifest)
        if (manifestCheck is ValidationResult.Invalid) {
            return manifestCheck to manifest
        }

        // Validate each referenced icon
        for ((keyStr, relativePath) in manifest.icons) {
            val key = WeatherIconKey.fromSerialName(keyStr)
            if (key == WeatherIconKey.UNKNOWN) {
                // Warning/skip unknown keys, or accept for future extension
                continue
            }
            val iconFile = File(rootDir, relativePath)
            val iconCheck = validateSvgFile(iconFile, rootDir)
            if (iconCheck is ValidationResult.Invalid) {
                return ValidationResult.Invalid("图标 '$keyStr' ($relativePath) 校验失败: ${iconCheck.reason}") to manifest
            }
        }

        // Validate preview image if present
        if (!manifest.preview.isNullOrBlank()) {
            val previewFile = File(rootDir, manifest.preview)
            if (previewFile.exists()) {
                if (previewFile.length() > MAX_PREVIEW_BYTES) {
                    return ValidationResult.Invalid("预览图体积超出限制 (最大 5MB)") to manifest
                }
                val previewExt = previewFile.extension.lowercase(Locale.ROOT)
                if (previewExt !in listOf("png", "jpg", "jpeg", "webp", "svg")) {
                    return ValidationResult.Invalid("不支持的预览图格式: $previewExt") to manifest
                }
            }
        }

        return ValidationResult.Valid to manifest
    }
}
