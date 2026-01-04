package com.cryptosignal.assistant.domain.repository

import com.cryptosignal.assistant.domain.models.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    
    fun getSettings(): Flow<AppSettings>
    
    suspend fun getSettingsSync(): AppSettings
    
    suspend fun saveSettings(settings: AppSettings)
    
    suspend fun updateTradingPairs(pairs: List<String>)
    
    suspend fun updateAggressiveness(level: com.cryptosignal.assistant.domain.models.AggressivenessLevel)
    
    suspend fun toggleNotifications(enabled: Boolean)
    
    suspend fun updateNotificationTypes(types: Set<com.cryptosignal.assistant.domain.models.NotificationType>)
    
    suspend fun updateQuietHours(quietHours: com.cryptosignal.assistant.domain.models.QuietHours)
    
    suspend fun updateSecuritySettings(settings: com.cryptosignal.assistant.domain.models.SecuritySettings)
    
    suspend fun updateTheme(theme: com.cryptosignal.assistant.domain.models.AppTheme)
    
    suspend fun resetToDefaults()
}