package com.cryptosignal.assistant.domain.services.managers

import com.cryptosignal.assistant.domain.models.*
import com.cryptosignal.assistant.domain.repository.SignalRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RiskManager @Inject constructor(
    private val signalRepository: SignalRepository
) {
    
    companion object {
        private const val RISK_PER_TRADE = 0.02 // 2%
        private const val MAX_DRAWDOWN = 0.20 // 20%
        private const val MIN_RR_RATIO = 1.5
        private const val MAX_LEVERAGE = 5.0
        private const val ADAPTATION_THRESHOLD = 0.05 // 5% drawdown triggers adaptation
    }
    
    data class PositionSize(
        val positionSize: Double, // In USD
        val riskAmount: Double,   // Risk amount in USD
        val leverage: Double,
        val stopLossDistance: Double, // In percentage
        val isValid: Boolean
    )
    
    suspend fun evaluateSignal(
        signal: Signal,
        marketData: MarketData
    ): Signal? {
        try {
            // Calculate position size based on risk parameters
            val positionSize = calculatePositionSize(
                signal = signal,
                accountSize = 10000.0, // This should come from settings/user input
                marketData = marketData
            )
            
            if (!positionSize.isValid) {
                Timber.w("Signal rejected due to invalid position size")
                return null
            }
            
            // Check if signal meets minimum R/R requirement
            if (signal.riskReward < MIN_RR_RATIO) {
                Timber.w("Signal rejected due to low R/R: ${signal.riskReward} < $MIN_RR_RATIO")
                return null
            }
            
            // Check recent signal frequency
            val hasRecentSignal = signalRepository.hasRecentSignal(
                pair = signal.pair,
                minInterval = 15 * 60 * 1000L // 15 minutes
            )
            
            if (hasRecentSignal) {
                Timber.w("Signal rejected due to recent signal on same pair")
                return null
            }
            
            // Check maximum number of concurrent signals
            val activeSignals = signalRepository.getActiveSignals().first()
            if (activeSignals.size >= 5) { // Max 5 concurrent signals
                Timber.w("Signal rejected due to maximum concurrent signals reached")
                return null
            }
            
            // Apply adaptive risk management
            val adaptedSignal = applyAdaptiveRiskManagement(signal)
            
            return adaptedSignal
            
        } catch (e: Exception) {
            Timber.e(e, "Error evaluating signal risk")
            return null
        }
    }
    
    suspend fun calculatePositionSize(
        signal: Signal,
        accountSize: Double,
        marketData: MarketData
    ): PositionSize {
        try {
            val currentPrice = marketData.candles.last().close
            val riskAmount = accountSize * RISK_PER_TRADE
            
            // Calculate stop loss distance in percentage
            val stopLossDistance = abs(
                when (signal.direction) {
                    Direction.LONG -> (signal.entryPrice - signal.stopLoss) / signal.entryPrice
                    Direction.SHORT -> (signal.stopLoss - signal.entryPrice) / signal.entryPrice
                }
            ) * 100
            
            if (stopLossDistance == 0.0) {
                return PositionSize(0.0, 0.0, 1.0, 0.0, false)
            }
            
            // Calculate position size based on risk
            val positionSizeInUSD = riskAmount / (stopLossDistance / 100)
            
            // Adjust leverage based on confidence and volatility
            val adjustedLeverage = calculateOptimalLeverage(
                confidence = signal.confidence,
                stopLossDistance = stopLossDistance,
                volatility = marketData.candles.let { candles ->
                    val closes = candles.map { it.close }
                    calculateVolatility(closes)
                }
            )
            
            // Validate position size
            val isValid = validatePositionSize(
                positionSize = positionSizeInUSD,
                accountSize = accountSize,
                leverage = adjustedLeverage
            )
            
            return PositionSize(
                positionSize = positionSizeInUSD,
                riskAmount = riskAmount,
                leverage = adjustedLeverage,
                stopLossDistance = stopLossDistance,
                isValid = isValid
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error calculating position size")
            return PositionSize(0.0, 0.0, 1.0, 0.0, false)
        }
    }
    
    suspend fun checkRiskLimits(): RiskStatus {
        try {
            val completedSignals = signalRepository.getCompletedSignals().first()
            
            if (completedSignals.isEmpty()) {
                return RiskStatus(
                    currentDrawdown = 0.0,
                    maxDrawdown = 0.0,
                    isWithinLimits = true,
                    recommendedAggressiveness = AggressivenessLevel.BALANCED
                )
            }
            
            // Calculate cumulative returns
            var cumulativeReturn = 0.0
            var peak = 0.0
            var maxDrawdown = 0.0
            val returns = mutableListOf<Double>()
            
            for (signal in completedSignals.sortedBy { it.timestamp }) {
                val outcome = signal.outcome
                if (outcome != null) {
                    cumulativeReturn += outcome.pnlR
                    returns.add(outcome.pnlR)
                    
                    // Update peak and calculate drawdown
                    if (cumulativeReturn > peak) {
                        peak = cumulativeReturn
                    }
                    
                    val drawdown = peak - cumulativeReturn
                    if (drawdown > maxDrawdown) {
                        maxDrawdown = drawdown
                    }
                }
            }
            
            val currentDrawdown = peak - cumulativeReturn
            val isWithinLimits = currentDrawdown <= MAX_DRAWDOWN
            
            // Determine recommended aggressiveness
            val recommendedAggressiveness = when {
                currentDrawdown > MAX_DRAWDOWN * 0.75 -> AggressivenessLevel.CONSERVATIVE
                currentDrawdown > MAX_DRAWDOWN * 0.5 -> AggressivenessLevel.BALANCED
                else -> AggressivenessLevel.AGGRESSIVE
            }
            
            return RiskStatus(
                currentDrawdown = currentDrawdown,
                maxDrawdown = maxDrawdown,
                isWithinLimits = isWithinLimits,
                recommendedAggressiveness = recommendedAggressiveness,
                winRate = calculateWinRate(returns),
                profitFactor = calculateProfitFactor(returns),
                expectancy = calculateExpectancy(returns)
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error checking risk limits")
            return RiskStatus(
                currentDrawdown = 0.0,
                maxDrawdown = 0.0,
                isWithinLimits = true,
                recommendedAggressiveness = AggressivenessLevel.BALANCED
            )
        }
    }
    
    suspend fun adaptToMarketConditions() {
        try {
            val riskStatus = checkRiskLimits()
            
            if (!riskStatus.isWithinLimits) {
                Timber.w("Risk limits exceeded! Current drawdown: ${riskStatus.currentDrawdown}")
                // Implement emergency measures
                applyEmergencyMeasures()
            }
            
            // Apply adaptive measures based on drawdown
            when {
                riskStatus.currentDrawdown > MAX_DRAWDOWN * 0.75 -> {
                    // High risk - reduce position sizes, increase R/R requirements
                    applyHighRiskMeasures()
                }
                riskStatus.currentDrawdown > MAX_DRAWDOWN * 0.5 -> {
                    // Medium risk - moderate adjustments
                    applyMediumRiskMeasures()
                }
                else -> {
                    // Normal operation
                    applyNormalRiskMeasures()
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error adapting to market conditions")
        }
    }
    
    private fun calculateOptimalLeverage(
        confidence: Double,
        stopLossDistance: Double,
        volatility: Double
    ): Double {
        // Base leverage on confidence
        var leverage = 1.0 + (confidence * 2.0)
        
        // Adjust for stop loss distance (tighter stops need lower leverage)
        val slAdjustment = when {
            stopLossDistance < 1.0 -> 0.5 // Very tight stop
            stopLossDistance < 2.0 -> 0.8 // Tight stop
            stopLossDistance < 5.0 -> 1.0 // Normal stop
            else -> 1.2 // Wide stop allows higher leverage
        }
        leverage *= slAdjustment
        
        // Adjust for volatility (high volatility needs lower leverage)
        val volatilityAdjustment = 1.0 / (1.0 + volatility * 10)
        leverage *= volatilityAdjustment
        
        // Apply caps
        return leverage.coerceIn(1.0, MAX_LEVERAGE)
    }
    
    private fun validatePositionSize(
        positionSize: Double,
        accountSize: Double,
        leverage: Double
    ): Boolean {
        val maxPositionSize = accountSize * leverage
        return positionSize <= maxPositionSize && positionSize > 0
    }
    
    private fun calculateVolatility(prices: List<Double>): Double {
        if (prices.size < 2) return 0.0
        
        val returns = mutableListOf<Double>()
        for (i in 1 until prices.size) {
            returns.add((prices[i] - prices[i-1]) / prices[i-1])
        }
        
        val meanReturn = returns.average()
        val variance = returns.map { (it - meanReturn).pow(2) }.average()
        
        return sqrt(variance)
    }
    
    private fun calculateWinRate(returns: List<Double>): Double {
        if (returns.isEmpty()) return 0.0
        val wins = returns.count { it > 0 }
        return wins.toDouble() / returns.size
    }
    
    private fun calculateProfitFactor(returns: List<Double>): Double {
        val totalProfit = returns.filter { it > 0 }.sum()
        val totalLoss = abs(returns.filter { it < 0 }.sum())
        
        return if (totalLoss == 0.0) Double.POSITIVE_INFINITY else totalProfit / totalLoss
    }
    
    private fun calculateExpectancy(returns: List<Double>): Double {
        if (returns.isEmpty()) return 0.0
        return returns.average()
    }
    
    private fun applyAdaptiveRiskManagement(signal: Signal): Signal {
        // Adjust leverage based on current market conditions
        val adjustedLeverage = signal.leverage * getLeverageMultiplier()
        
        return signal.copy(
            leverage = adjustedLeverage.coerceIn(1.0, MAX_LEVERAGE)
        )
    }
    
    private fun getLeverageMultiplier(): Double {
        // This would be based on current market volatility, correlation, etc.
        // For now, return 1.0 (no adjustment)
        return 1.0
    }
    
    private suspend fun applyEmergencyMeasures() {
        // Implement emergency risk measures
        Timber.w("Applying emergency risk measures")
        
        // Close high-risk positions
        // Reduce exposure
        // Increase stop losses
    }
    
    private fun applyHighRiskMeasures() {
        // Reduce position sizes by 50%
        // Increase minimum R/R to 2.5
        // Reduce maximum leverage to 2x
        Timber.i("Applying high risk measures")
    }
    
    private fun applyMediumRiskMeasures() {
        // Reduce position sizes by 25%
        // Increase minimum R/R to 2.0
        // Reduce maximum leverage to 3x
        Timber.i("Applying medium risk measures")
    }
    
    private fun applyNormalRiskMeasures() {
        // Normal operation parameters
        Timber.i("Applying normal risk measures")
    }
}

// Data classes
data class RiskStatus(
    val currentDrawdown: Double,
    val maxDrawdown: Double,
    val isWithinLimits: Boolean,
    val recommendedAggressiveness: AggressivenessLevel,
    val winRate: Double = 0.0,
    val profitFactor: Double = 0.0,
    val expectancy: Double = 0.0
)