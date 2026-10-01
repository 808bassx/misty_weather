package com.misty.weather.weather.city

import kotlinx.serialization.Serializable

// ─── City Domain Model ────────────────────────────────────────────────────────
//
// Phase 4.3: City entity representation for multi-city weather and location systems.
//
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class City(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = false,
    val isCurrentLocation: Boolean = false,
    val nameLatin: String = id.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
) {
    companion object {
        val Tokyo = City(
            id = "tokyo",
            name = "东京",
            country = "日本",
            latitude = 35.6762,
            longitude = 139.6503,
            isFavorite = true,
        )

        val Kyoto = City(
            id = "kyoto",
            name = "京都",
            country = "日本",
            latitude = 35.0116,
            longitude = 135.7681,
            isFavorite = true,
        )

        val Osaka = City(
            id = "osaka",
            name = "大阪",
            country = "日本",
            latitude = 34.6937,
            longitude = 135.5023,
            isFavorite = true,
        )

        val Shanghai = City(
            id = "shanghai",
            name = "上海",
            country = "中国",
            latitude = 31.2304,
            longitude = 121.4737,
            isFavorite = true,
        )

        val Beijing = City(
            id = "beijing",
            name = "北京",
            country = "中国",
            latitude = 39.9042,
            longitude = 116.4074,
            isFavorite = true,
        )

        val DefaultCities = listOf(Tokyo, Kyoto, Osaka, Shanghai, Beijing)
    }
}
