package com.example.mistyweather.weather.icon

// ─── Icon Pack License & Attribution ─────────────────────────────────────────
//
// Phase 4.4-B0: Preserves open-source licensing, authorship, and attribution.
//
// Explicitly complies with open-source licenses (MIT, Apache-2.0, CC-BY, etc.)
// and provides transparent provenance for all third-party icon packs.
//
// ─────────────────────────────────────────────────────────────────────────────

data class IconPackLicense(
    val licenseName: String,
    val author: String?,
    val sourceUrl: String? = null,
    val licenseUrl: String? = null,
    val copyrightNotice: String? = null,
) {
    val attributionText: String
        get() = buildString {
            if (!author.isNullOrBlank()) {
                append("作者: ").append(author)
            }
            if (licenseName.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append("许可: ").append(licenseName)
            }
        }
}
