package com.misty.weather.weather.runtime

// ─── Weather Error ────────────────────────────────────────────────────────────
//
// Phase 4.1: Runtime Experience & State Architecture.
//
// Domain hierarchy of weather-related runtime errors.
// Distinguishes recoverable network dropouts from unrecoverable corruptions.
//
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Domain hierarchy of weather errors.
 *
 * @param userFacingMessage Localized user-friendly explanation.
 * @param canFallbackToLastKnown Whether the UI should preserve the last known weather state.
 */
sealed class WeatherError(
    val userFacingMessage: String,
    val canFallbackToLastKnown: Boolean = true,
) {
    /** Device is offline or in airplane mode. */
    data object NetworkUnavailable : WeatherError(
        userFacingMessage = "网络连接不可用，正在显示历史缓存天气",
        canFallbackToLastKnown = true,
    )

    /** Remote API request timed out. */
    data object RequestTimeout : WeatherError(
        userFacingMessage = "天气数据请求超时，已为您保留上次天气",
        canFallbackToLastKnown = true,
    )

    /** Remote service returned 5xx or server outage. */
    data class ServiceUnavailable(val code: Int = 503) : WeatherError(
        userFacingMessage = "气象服务暂时不可用 ($code)，请稍后重试",
        canFallbackToLastKnown = true,
    )

    /** Location permission not granted (reserved for Phase 4.3). */
    data object LocationPermissionDenied : WeatherError(
        userFacingMessage = "未获得定位权限，正在显示默认城市天气",
        canFallbackToLastKnown = true,
    )

    /** Data deserialization or integrity failure. */
    data object DataCorrupted : WeatherError(
        userFacingMessage = "气象数据格式异常，已恢复为默认状态",
        canFallbackToLastKnown = true,
    )

    /** Generic uncaught error. */
    data class Unknown(val cause: Throwable?) : WeatherError(
        userFacingMessage = cause?.localizedMessage ?: "未知气象同步异常",
        canFallbackToLastKnown = true,
    )

    companion object {
        /**
         * Maps generic throwables to typed [WeatherError].
         */
        fun fromThrowable(throwable: Throwable): WeatherError = when (throwable) {
            is java.net.UnknownHostException -> NetworkUnavailable
            is java.net.SocketTimeoutException -> RequestTimeout
            else -> Unknown(throwable)
        }
    }
}
