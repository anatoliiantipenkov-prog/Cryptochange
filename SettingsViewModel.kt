package com.cryptosignal.assistant.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptosignal.assistant.domain.models.AppSettings
import com.cryptosignal.assistant.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Settings are already being collected via flow
                Timber.d("Loading settings...")
            } catch (e: Exception) {
                Timber.e(e, "Error loading settings")
                _error.value = "Ошибка загрузки настроек: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            try {
                settingsRepository.saveSettings(newSettings)
                Timber.d("Settings updated successfully")
            } catch (e: Exception) {
                Timber.e(e, "Error updating settings")
                _error.value = "Ошибка обновления настроек: ${e.message}"
            }
        }
    }
    
    fun updateTradingPairs(pairs: List<String>) {
        viewModelScope.launch {
            try {
                settingsRepository.updateTradingPairs(pairs)
                Timber.d("Trading pairs updated: $pairs")
            } catch (e: Exception) {
                Timber.e(e, "Error updating trading pairs")
                _error.value = "Ошибка обновления торговых пар: ${e.message}"
            }
        }
    }
    
    fun updateAggressiveness(level: com.cryptosignal.assistant.domain.models.AggressivenessLevel) {
        viewModelScope.launch {
            try {
                settingsRepository.updateAggressiveness(level)
                Timber.d("Aggressiveness updated: $level")
            } catch (e: Exception) {
                Timber.e(e, "Error updating aggressiveness")
                _error.value = "Ошибка обновления режима: ${e.message}"
            }
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.toggleNotifications(enabled)
                Timber.d("Notifications toggled: $enabled")
            } catch (e: Exception) {
                Timber.e(e, "Error toggling notifications")
                _error.value = "Ошибка переключения уведомлений: ${e.message}"
            }
        }
    }
    
    fun updateNotificationTypes(types: Set<com.cryptosignal.assistant.domain.models.NotificationType>) {
        viewModelScope.launch {
            try {
                // This would be implemented in the repository
                Timber.d("Notification types updated: $types")
            } catch (e: Exception) {
                Timber.e(e, "Error updating notification types")
                _error.value = "Ошибка обновления типов уведомлений: ${e.message}"
            }
        }
    }
    
    fun updateQuietHours(quietHours: com.cryptosignal.assistant.domain.models.QuietHours) {
        viewModelScope.launch {
            try {
                settingsRepository.updateQuietHours(quietHours)
                Timber.d("Quiet hours updated: $quietHours")
            } catch (e: Exception) {
                Timber.e(e, "Error updating quiet hours")
                _error.value = "Ошибка обновления тихого режима: ${e.message}"
            }
        }
    }
    
    fun updateSecuritySettings(settings: com.cryptosignal.assistant.domain.models.SecuritySettings) {
        viewModelScope.launch {
            try {
                settingsRepository.updateSecuritySettings(settings)
                Timber.d("Security settings updated")
            } catch (e: Exception) {
                Timber.e(e, "Error updating security settings")
                _error.value = "Ошибка обновления настроек безопасности: ${e.message}"
            }
        }
    }
    
    fun updateTheme(theme: com.cryptosignal.assistant.domain.models.AppTheme) {
        viewModelScope.launch {
            try {
                settingsRepository.updateTheme(theme)
                Timber.d("Theme updated: $theme")
            } catch (e: Exception) {
                Timber.e(e, "Error updating theme")
                _error.value = "Ошибка обновления темы: ${e.message}"
            }
        }
    }
    
    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                settingsRepository.resetToDefaults()
                Timber.d("Settings reset to defaults")
            } catch (e: Exception) {
                Timber.e(e, "Error resetting settings")
                _error.value = "Ошибка сброса настроек: ${e.message}"
            }
        }
    }
    
    fun exportStatistics() {
        viewModelScope.launch {
            try {
                // TODO: Implement statistics export
                Timber.d("Exporting statistics...")
            } catch (e: Exception) {
                Timber.e(e, "Error exporting statistics")
                _error.value = "Ошибка экспорта статистики: ${e.message}"
            }
        }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            try {
                // TODO: Implement data clearing
                Timber.d("Clearing all data...")
            } catch (e: Exception) {
                Timber.e(e, "Error clearing data")
                _error.value = "Ошибка очистки данных: ${e.message}"
            }
        }
    }
}