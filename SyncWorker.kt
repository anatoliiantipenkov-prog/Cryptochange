package com.cryptosignal.assistant.services

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cryptosignal.assistant.domain.models.MarketData
import com.cryptosignal.assistant.domain.models.Timeframe
import com.cryptosignal.assistant.domain.repository.MarketDataRepository
import com.cryptosignal.assistant.domain.repository.SettingsRepository
import com.cryptosignal.assistant.domain.repository.SignalRepository
import com.cryptosignal.assistant.domain.services.SignalGenerator
import com.cryptosignal.assistant.domain.services.managers.RiskManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val marketDataRepository: MarketDataRepository,
    private val settingsRepository: SettingsRepository,
    private val signalGenerator: SignalGenerator,
    private val riskManager: RiskManager,
    private val signalRepository: SignalRepository,
    private val notificationService: NotificationService
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "SyncWorker"
    }
    
    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("Starting sync work")
        
        return try {
            // Get settings
            val settings = settingsRepository.getSettingsSync()
            
            if (!settings.notificationsEnabled) {
                Timber.tag(TAG).d("Notifications disabled, skipping sync")
                return Result.success()
            }
            
            // Get trading pairs
            val tradingPairs = settings.tradingPairs
            if (tradingPairs.isEmpty()) {
                Timber.tag(TAG).w("No trading pairs configured")
                return Result.success()
            }
            
            // Check risk limits
            val riskStatus = riskManager.checkRiskLimits()
            if (!riskStatus.isWithinLimits) {
                Timber.tag(TAG).w("Risk limits exceeded, pausing signal generation")
                // Send notification about risk limit
                notificationService.showRiskLimitNotification(riskStatus)
                return Result.success()
            }
            
            // Generate signals for each pair
            val newSignals = mutableListOf<com.cryptosignal.assistant.domain.models.Signal>()
            
            coroutineScope {
                val deferredSignals = tradingPairs.map { pair ->
                    async {
                        generateSignalsForPair(pair, settings)
                    }
                }
                
                val results = deferredSignals.awaitAll()
                newSignals.addAll(results.flatten())
            }
            
            // Filter and save signals
            val validSignals = newSignals.filter { signal ->
                // Check if we already have a recent signal for this pair
                !signalRepository.hasRecentSignal(
                    pair = signal.pair,
                    minInterval = 15 * 60 * 1000L // 15 minutes
                )
            }
            
            // Save valid signals
            validSignals.forEach { signal ->
                signalRepository.saveSignal(signal)
                
                // Send notification
                if (settings.notificationsEnabled && 
                    settings.notificationTypes.contains(com.cryptosignal.assistant.domain.models.NotificationType.NEW_SIGNALS)) {
                    notificationService.showSignalNotification(signal)
                }
            }
            
            // If no signals generated, check if we should send "no entry" notification
            if (validSignals.isEmpty() && settings.notificationTypes.contains(com.cryptosignal.assistant.domain.models.NotificationType.NO_ENTRY_SIGNALS)) {
                val lastSignalTime = signalRepository.getLastSignalTimestamp()
                val now = System.currentTimeMillis()
                
                // Send "no entry" notification if no signals for more than 2 hours
                if (lastSignalTime == null || now - lastSignalTime > 2 * 60 * 60 * 1000L) {
                    notificationService.showNoSignalNotification()
                }
            }
            
            Timber.tag(TAG).d("Sync completed. Generated ${validSignals.size} signals")
            Result.success()
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in sync work")
            Result.retry()
        }
    }
    
    private suspend fun generateSignalsForPair(
        pair: String,
        settings: com.cryptosignal.assistant.domain.models.AppSettings
    ): List<com.cryptosignal.assistant.domain.models.Signal> {
        try {
            Timber.tag(TAG).d("Generating signals for $pair")
            
            // Get market data
            val timeframes = listOf(Timeframe.H1, Timeframe.H4) // Focus on higher timeframes
            val allSignals = mutableListOf<com.cryptosignal.assistant.domain.models.Signal>()
            
            for (timeframe in timeframes) {
                try {
                    val marketData = marketDataRepository.getMarketData(
                        pair = pair,
                        timeframe = timeframe,
                        candleLimit = 100
                    )
                    
                    // Generate signals
                    val signals = signalGenerator.generateSignals(marketData)
                    
                    // Apply risk management
                    val riskManagedSignals = signals.mapNotNull { signal ->
                        riskManager.evaluateSignal(signal, marketData)
                    }
                    
                    allSignals.addAll(riskManagedSignals)
                    
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e, "Error generating signals for $pair on ${timeframe.name}")
                }
            }
            
            // Take best signals based on confidence
            val bestSignals = allSignals
                .sortedByDescending { it.confidence * it.riskReward }
                .take(1) // Max 1 signal per pair per sync
            
            return bestSignals
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error generating signals for $pair")
            return emptyList()
        }
    }
}