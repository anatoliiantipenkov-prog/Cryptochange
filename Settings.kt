package com.cryptosignal.assistant.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val tradingPairs: List<String> = DEFAULT_PAIRS,
    val aggressivenessLevel: AggressivenessLevel = AggressivenessLevel.BALANCED,
    val notificationsEnabled: Boolean = true,
    val notificationTypes: Set<NotificationType> = NotificationType.values().toSet(),
    val quietHours: QuietHours = QuietHours(),
    val securitySettings: SecuritySettings = SecuritySettings(),
    val riskSettings: RiskSettings = RiskSettings(),
    val theme: AppTheme = AppTheme.DARK,
    val language: String = "ru"
) {
    companion object {
        val DEFAULT_PAIRS = listOf(
            "BTC/USDT",
            "ETH/USDT",
            "ADA/USDT",
            "SOL/USDT",
            "DOT/USDT",
            "AVAX/USDT"
        )
    }
}

@Serializable
data class QuietHours(
    val enabled: Boolean = true,
    val startHour: Int = 22,
    val startMinute: Int = 0,
    val endHour: Int = 8,
    val endMinute: Int = 0
) {
    fun isInQuietHours(currentHour: Int, currentMinute: Int): Boolean {
        val currentTime = currentHour * 60 + currentMinute
        val startTime = startHour * 60 + startMinute
        val endTime = endHour * 60 + endMinute
        
        return if (startTime <= endTime) {
            currentTime in startTime..endTime
        } else {
            currentTime >= startTime || currentTime <= endTime
        }
    }
}

@Serializable
data class SecuritySettings(
    val pinEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val autoLogoutMinutes: Int = 15,
    val lastActivityTimestamp: Long = 0L
)

@Serializable
data class RiskSettings(
    val riskPerTrade: Double = 0.02,
    val maxDrawdown: Double = 0.20,
    val useAdaptiveRisk: Boolean = true,
    val minRRRatio: Double = 1.5,
    val maxLeverage: Double = 5.0
)

enum class NotificationType {
    NEW_SIGNALS,
    TP_SL_REACHED,
    MARKET_EVENTS,
    NO_ENTRY_SIGNALS
}

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}