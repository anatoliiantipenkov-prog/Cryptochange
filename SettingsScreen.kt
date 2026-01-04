package com.cryptosignal.assistant.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cryptosignal.assistant.R
import com.cryptosignal.assistant.domain.models.AggressivenessLevel
import com.cryptosignal.assistant.domain.models.AppTheme
import com.cryptosignal.assistant.presentation.theme.*

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        SettingsAppBar()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trading Settings
            item {
                SettingsSection(title = "Торговые параметры") {
                    TradingPairsSetting(
                        pairs = settings.tradingPairs,
                        onPairsChanged = { viewModel.updateTradingPairs(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    AggressivenessSetting(
                        selectedLevel = settings.aggressivenessLevel,
                        onLevelChanged = { viewModel.updateAggressiveness(it) }
                    )
                }
            }
            
            // Notifications
            item {
                SettingsSection(title = "Уведомления") {
                    NotificationSettings(
                        settings = settings,
                        onSettingsChanged = { viewModel.updateSettings(it) }
                    )
                }
            }
            
            // Risk Settings
            item {
                SettingsSection(title = "Риск-параметры") {
                    RiskSettings(settings = settings)
                }
            }
            
            // Security
            item {
                SettingsSection(title = "Безопасность") {
                    SecuritySettings(settings = settings)
                }
            }
            
            // Appearance
            item {
                SettingsSection(title = "Внешний вид") {
                    ThemeSetting(
                        selectedTheme = settings.theme,
                        onThemeChanged = { viewModel.updateTheme(it) }
                    )
                }
            }
            
            // Data Management
            item {
                SettingsSection(title = "Данные") {
                    DataManagementSettings(viewModel = viewModel)
                }
            }
            
            // About
            item {
                SettingsSection(title = "О приложении") {
                    AboutSettings()
                }
            }
        }
    }
}

@Composable
private fun SettingsAppBar() {
    SmallTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge
            )
        }
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun TradingPairsSetting(
    pairs: List<String>,
    onPairsChanged: (List<String>) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.settings_trading_pairs),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val availablePairs = listOf(
            "BTC/USDT", "ETH/USDT", "ADA/USDT", "SOL/USDT", "DOT/USDT",
            "AVAX/USDT", "BNB/USDT", "XRP/USDT", "MATIC/USDT", "LINK/USDT"
        )
        
        availablePairs.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pair,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Switch(
                    checked = pairs.contains(pair),
                    onCheckedChange = { isChecked ->
                        val newPairs = if (isChecked) {
                            (pairs + pair).distinct()
                        } else {
                            pairs.filter { it != pair }
                        }
                        onPairsChanged(newPairs)
                    }
                )
            }
        }
    }
}

@Composable
private fun AggressivenessSetting(
    selectedLevel: AggressivenessLevel,
    onLevelChanged: (AggressivenessLevel) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.settings_aggressiveness),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AggressivenessLevel.values().forEach { level ->
            val isSelected = selectedLevel == level
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLevelChanged(level) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getAggressivenessLabel(level),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = getAggressivenessDescription(level),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                RadioButton(
                    selected = isSelected,
                    onClick = { onLevelChanged(level) }
                )
            }
        }
    }
}

@Composable
private fun getAggressivenessLabel(level: AggressivenessLevel): String {
    return when (level) {
        AggressivenessLevel.CONSERVATIVE -> stringResource(R.string.settings_conservative)
        AggressivenessLevel.BALANCED -> stringResource(R.string.settings_balanced)
        AggressivenessLevel.AGGRESSIVE -> stringResource(R.string.settings_aggressive)
    }
}

@Composable
private fun getAggressivenessDescription(level: AggressivenessLevel): String {
    return when (level) {
        AggressivenessLevel.CONSERVATIVE -> stringResource(R.string.risk_conservative_desc)
        AggressivenessLevel.BALANCED -> stringResource(R.string.risk_balanced_desc)
        AggressivenessLevel.AGGRESSIVE -> stringResource(R.string.risk_aggressive_desc)
    }
}

@Composable
private fun NotificationSettings(
    settings: com.cryptosignal.assistant.domain.models.AppSettings,
    onSettingsChanged: (com.cryptosignal.assistant.domain.models.AppSettings) -> Unit
) {
    Column {
        Text(
            text = "Настройки уведомлений",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Включить уведомления",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = settings.notificationsEnabled,
                onCheckedChange = {
                    onSettingsChanged(settings.copy(notificationsEnabled = it))
                }
            )
        }
        
        if (settings.notificationsEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Notification types
            NotificationTypeSetting(
                title = stringResource(R.string.settings_notifications_new_signals),
                checked = settings.notificationTypes.contains(com.cryptosignal.assistant.domain.models.NotificationType.NEW_SIGNALS),
                onCheckedChange = { isChecked ->
                    val newTypes = if (isChecked) {
                        settings.notificationTypes + com.cryptosignal.assistant.domain.models.NotificationType.NEW_SIGNALS
                    } else {
                        settings.notificationTypes - com.cryptosignal.assistant.domain.models.NotificationType.NEW_SIGNALS
                    }
                    onSettingsChanged(settings.copy(notificationTypes = newTypes))
                }
            )
            
            NotificationTypeSetting(
                title = stringResource(R.string.settings_notifications_tp_sl),
                checked = settings.notificationTypes.contains(com.cryptosignal.assistant.domain.models.NotificationType.TP_SL_REACHED),
                onCheckedChange = { isChecked ->
                    val newTypes = if (isChecked) {
                        settings.notificationTypes + com.cryptosignal.assistant.domain.models.NotificationType.TP_SL_REACHED
                    } else {
                        settings.notificationTypes - com.cryptosignal.assistant.domain.models.NotificationType.TP_SL_REACHED
                    }
                    onSettingsChanged(settings.copy(notificationTypes = newTypes))
                }
            )
            
            NotificationTypeSetting(
                title = stringResource(R.string.settings_notifications_market_events),
                checked = settings.notificationTypes.contains(com.cryptosignal.assistant.domain.models.NotificationType.MARKET_EVENTS),
                onCheckedChange = { isChecked ->
                    val newTypes = if (isChecked) {
                        settings.notificationTypes + com.cryptosignal.assistant.domain.models.NotificationType.MARKET_EVENTS
                    } else {
                        settings.notificationTypes - com.cryptosignal.assistant.domain.models.NotificationType.MARKET_EVENTS
                    }
                    onSettingsChanged(settings.copy(notificationTypes = newTypes))
                }
            )
            
            NotificationTypeSetting(
                title = stringResource(R.string.settings_notifications_no_entry),
                checked = settings.notificationTypes.contains(com.cryptosignal.assistant.domain.models.NotificationType.NO_ENTRY_SIGNALS),
                onCheckedChange = { isChecked ->
                    val newTypes = if (isChecked) {
                        settings.notificationTypes + com.cryptosignal.assistant.domain.models.NotificationType.NO_ENTRY_SIGNALS
                    } else {
                        settings.notificationTypes - com.cryptosignal.assistant.domain.models.NotificationType.NO_ENTRY_SIGNALS
                    }
                    onSettingsChanged(settings.copy(notificationTypes = newTypes))
                }
            )
        }
    }
}

@Composable
private fun NotificationTypeSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun RiskSettings(settings: com.cryptosignal.assistant.domain.models.AppSettings) {
    Column {
        Text(
            text = stringResource(R.string.settings_risk_params),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Risk per trade (fixed)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_risk_per_trade),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.risk_per_trade_percent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Max drawdown (fixed)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_max_drawdown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.max_drawdown_percent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SecuritySettings(settings: com.cryptosignal.assistant.domain.models.AppSettings) {
    Column {
        Text(
            text = stringResource(R.string.settings_security),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_pin_code),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { /* TODO: Implement PIN setup */ }) {
                Text(stringResource(R.string.settings_pin_code))
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_biometrics),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = settings.securitySettings.biometricEnabled,
                onCheckedChange = { /* TODO: Implement biometrics toggle */ }
            )
        }
    }
}

@Composable
private fun ThemeSetting(
    selectedTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit
) {
    Column {
        Text(
            text = "Тема",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AppTheme.values().forEach { theme ->
            val isSelected = selectedTheme == theme
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeChanged(theme) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getThemeLabel(theme),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                
                RadioButton(
                    selected = isSelected,
                    onClick = { onThemeChanged(theme) }
                )
            }
        }
    }
}

@Composable
private fun getThemeLabel(theme: AppTheme): String {
    return when (theme) {
        AppTheme.LIGHT -> "Светлая"
        AppTheme.DARK -> "Темная"
        AppTheme.SYSTEM -> "Системная"
    }
}

@Composable
private fun DataManagementSettings(viewModel: SettingsViewModel) {
    Column {
        Text(
            text = "Управление данными",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_export_stats),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { viewModel.exportStatistics() }) {
                Text("Экспорт")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_clear_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            TextButton(
                onClick = { viewModel.clearAllData() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Очистить")
            }
        }
    }
}

@Composable
private fun AboutSettings() {
    Column {
        Text(
            text = stringResource(R.string.settings_about),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_version),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Crypto Signal Assistant - AI-ассистент для крипто-сигналов",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}