package com.cryptosignal.assistant.presentation.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptosignal.assistant.domain.models.Statistics
import com.cryptosignal.assistant.domain.models.TimeRange
import com.cryptosignal.assistant.domain.repository.SignalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val signalRepository: SignalRepository
) : ViewModel() {
    
    private val _selectedTimeRange = MutableStateFlow(TimeRange.MONTH)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    val statistics: StateFlow<Statistics?> = combine(
        signalRepository.getSignals(),
        _selectedTimeRange
    ) { signals, timeRange ->
        calculateStatistics(signals, timeRange)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    init {
        loadStatistics()
    }
    
    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
    }
    
    fun refreshStatistics() {
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Statistics will be recalculated automatically via combine
                Timber.d("Loading statistics...")
            } catch (e: Exception) {
                Timber.e(e, "Error loading statistics")
                _error.value = "Ошибка загрузки статистики: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun calculateStatistics(signals: List<Signal>, timeRange: TimeRange): Statistics {
        val filteredSignals = filterSignalsByTimeRange(signals, timeRange)
        val completedSignals = filteredSignals.filter { it.status == SignalStatus.COMPLETED }
        
        if (completedSignals.isEmpty()) {
            return createEmptyStatistics(timeRange)
        }
        
        // Calculate basic metrics
        val outcomes = completedSignals.mapNotNull { it.outcome }
        val wins = outcomes.filter { it.pnlR > 0 }
        val losses = outcomes.filter { it.pnlR <= 0 }
        
        val winRate = if (outcomes.isNotEmpty()) wins.size.toDouble() / outcomes.size else 0.0
        val totalProfit = wins.sumOf { it.pnlR }
        val totalLoss = abs(losses.sumOf { it.pnlR })
        val netProfit = totalProfit - totalLoss
        
        // Calculate profit factor
        val profitFactor = if (totalLoss > 0) totalProfit / totalLoss else 
            if (totalProfit > 0) Double.POSITIVE_INFINITY else 0.0
        
        // Calculate average R/R
        val averageRR = if (outcomes.isNotEmpty()) outcomes.map { it.pnlR }.average() else 0.0
        
        // Calculate max drawdown
        var peak = 0.0
        var currentDrawdown = 0.0
        var maxDrawdown = 0.0
        var cumulative = 0.0
        
        for (outcome in outcomes.sortedBy { it.exitTimestamp }) {
            cumulative += outcome.pnlR
            if (cumulative > peak) {
                peak = cumulative
            }
            currentDrawdown = peak - cumulative
            if (currentDrawdown > maxDrawdown) {
                maxDrawdown = currentDrawdown
            }
        }
        
        // Calculate streaks
        val streaks = calculateStreaks(outcomes.map { it.pnlR })
        val (currentStreak, maxWinStreak, maxLossStreak) = streaks
        
        // Calculate profit by pair
        val profitByPair = completedSignals
            .groupBy { it.pair }
            .mapValues { (_, signals) ->
                signals.sumOf { it.outcome?.pnlR ?: 0.0 }
            }
        
        // Calculate profit by timeframe
        val profitByTimeframe = completedSignals
            .groupBy { it.timeframe }
        
        // Calculate monthly profit
        val monthlyProfit = calculateMonthlyProfit(completedSignals)
        
        // Calculate expectancy and other advanced metrics
        val expectancy = if (outcomes.isNotEmpty()) {
            val avgWin = if (wins.isNotEmpty()) wins.map { it.pnlR }.average() else 0.0
            val avgLoss = if (losses.isNotEmpty()) losses.map { it.pnlR }.average() else 0.0
            (avgWin * winRate) + (avgLoss * (1 - winRate))
        } else 0.0
        
        val largestWin = outcomes.maxOfOrNull { it.pnlR } ?: 0.0
        val largestLoss = outcomes.minOfOrNull { it.pnlR } ?: 0.0
        val averageWin = if (wins.isNotEmpty()) wins.map { it.pnlR }.average() else 0.0
        val averageLoss = if (losses.isNotEmpty()) losses.map { it.pnlR }.average() else 0.0
        
        // Calculate Sharpe ratio (simplified)
        val sharpeRatio = calculateSharpeRatio(outcomes.map { it.pnlR })
        
        // Calculate Calmar ratio
        val calmarRatio = if (maxDrawdown > 0) (netProfit / outcomes.size) / maxDrawdown else 0.0
        
        return Statistics(
            winRate = winRate,
            profitFactor = profitFactor,
            maxDrawdown = maxDrawdown,
            averageRR = averageRR,
            totalSignals = filteredSignals.size,
            activeSignals = filteredSignals.count { it.isActive() },
            profitableSignals = wins.size,
            losingSignals = losses.size,
            totalProfit = totalProfit,
            totalLoss = totalLoss,
            netProfit = netProfit,
            profitByPair = profitByPair,
            profitByTimeframe = profitByTimeframe.mapValues { (_, signals) ->
                signals.sumOf { it.outcome?.pnlR ?: 0.0 }
            },
            profitByMonth = monthlyProfit,
            currentStreak = currentStreak,
            maxWinStreak = maxWinStreak,
            maxLossStreak = maxLossStreak,
            largestWin = largestWin,
            largestLoss = largestLoss,
            averageWin = averageWin,
            averageLoss = averageLoss,
            expectancy = expectancy,
            sharpeRatio = sharpeRatio,
            calmarRatio = calmarRatio,
            period = timeRange
        )
    }
    
    private fun filterSignalsByTimeRange(signals: List<Signal>, timeRange: TimeRange): List<Signal> {
        val now = System.currentTimeMillis()
        val cutoffTime = when (timeRange) {
            TimeRange.DAY -> now - 24 * 60 * 60 * 1000L
            TimeRange.WEEK -> now - 7 * 24 * 60 * 60 * 1000L
            TimeRange.MONTH -> now - 30 * 24 * 60 * 60 * 1000L
            TimeRange.QUARTER -> now - 90 * 24 * 60 * 60 * 1000L
            TimeRange.YEAR -> now - 365 * 24 * 60 * 60 * 1000L
            TimeRange.ALL -> 0L
        }
        
        return signals.filter { it.timestamp >= cutoffTime }
    }
    
    private fun createEmptyStatistics(timeRange: TimeRange): Statistics {
        return Statistics(
            winRate = 0.0,
            profitFactor = 0.0,
            maxDrawdown = 0.0,
            averageRR = 0.0,
            totalSignals = 0,
            activeSignals = 0,
            profitableSignals = 0,
            losingSignals = 0,
            totalProfit = 0.0,
            totalLoss = 0.0,
            netProfit = 0.0,
            profitByPair = emptyMap(),
            profitByTimeframe = emptyMap(),
            profitByMonth = emptyList(),
            currentStreak = 0,
            maxWinStreak = 0,
            maxLossStreak = 0,
            largestWin = 0.0,
            largestLoss = 0.0,
            averageWin = 0.0,
            averageLoss = 0.0,
            expectancy = 0.0,
            sharpeRatio = 0.0,
            calmarRatio = 0.0,
            period = timeRange
        )
    }
    
    private fun calculateStreaks(returns: List<Double>): Triple<Int, Int, Int> {
        if (returns.isEmpty()) return Triple(0, 0, 0)
        
        var currentStreak = 0
        var currentType = 0 // 0 = none, 1 = win, -1 = loss
        var maxWinStreak = 0
        var maxLossStreak = 0
        var currentWinStreak = 0
        var currentLossStreak = 0
        
        for (returnValue in returns) {
            val type = if (returnValue > 0) 1 else -1
            
            if (type == currentType) {
                if (type == 1) {
                    currentWinStreak++
                } else {
                    currentLossStreak++
                }
            } else {
                currentWinStreak = if (type == 1) 1 else 0
                currentLossStreak = if (type == -1) 1 else 0
                currentType = type
            }
            
            maxWinStreak = max(maxWinStreak, currentWinStreak)
            maxLossStreak = max(maxLossStreak, currentLossStreak)
        }
        
        currentStreak = if (currentType == 1) currentWinStreak else -currentLossStreak
        
        return Triple(currentStreak, maxWinStreak, maxLossStreak)
    }
    
    private fun calculateMonthlyProfit(signals: List<Signal>): List<com.cryptosignal.assistant.domain.models.MonthlyProfit> {
        val monthlyData = mutableMapOf<String, MutableList<Signal>>()
        
        for (signal in signals) {
            if (signal.outcome != null) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = signal.timestamp
                }
                val key = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
                monthlyData.getOrPut(key) { mutableListOf() }.add(signal)
            }
        }
        
        return monthlyData.map { (key, monthSignals) ->
            val (year, month) = key.split("-").let { 
                it[0].toInt() to it[1].toInt() 
            }
            
            val profit = monthSignals.sumOf { it.outcome?.pnlR ?: 0.0 }
            val winRate = if (monthSignals.isNotEmpty()) {
                val wins = monthSignals.count { (it.outcome?.pnlR ?: 0.0) > 0 }
                wins.toDouble() / monthSignals.size
            } else 0.0
            
            com.cryptosignal.assistant.domain.models.MonthlyProfit(
                year = year,
                month = month,
                profit = profit,
                winRate = winRate,
                signalsCount = monthSignals.size
            )
        }.sortedBy { it.year * 12 + it.month }
    }
    
    private fun calculateSharpeRatio(returns: List<Double>): Double {
        if (returns.isEmpty()) return 0.0
        
        val avgReturn = returns.average()
        val variance = returns.map { (it - avgReturn).pow(2) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        
        return if (stdDev > 0) avgReturn / stdDev else 0.0
    }
}