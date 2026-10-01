package com.misty.weather.weather.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ─── Weather API Interface & Client ───────────────────────────────────────────
//
// Phase 4.2: Open-Meteo REST API contract and client provider.
//
// Targeted endpoint: https://api.open-meteo.com/v1/forecast
// Zero API keys required. Pure meteorological REST API.
//
// ─────────────────────────────────────────────────────────────────────────────

interface WeatherApi {

    /**
     * Queries Open-Meteo weather forecast for specified geographic coordinates.
     */
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double = 35.6895,
        @Query("longitude") longitude: Double = 139.6917,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code",
        @Query("hourly") hourly: String = "temperature_2m,weather_code",
        @Query("daily") daily: String = "sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
    ): WeatherResponseDto
}

/**
 * Provider and factory for [WeatherApi] Retrofit client.
 */
object WeatherApiClient {
    const val BASE_URL = "https://api.open-meteo.com/"

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun create(
        baseUrl: String = BASE_URL,
        client: OkHttpClient = createClient(),
    ): WeatherApi {
        val mediaType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(mediaType))
            .build()
        return retrofit.create(WeatherApi::class.java)
    }

    val api: WeatherApi by lazy { create() }
}
