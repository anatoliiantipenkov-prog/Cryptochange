package com.cryptosignal.assistant.domain.services

import com.cryptosignal.assistant.domain.models.*
import com.cryptosignal.assistant.domain.services.managers.RiskManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.*

class SignalGenerator @Inject constructor(
    private val technicalIndicators: TechnicalIndicators,
    private val riskManager: RiskManager,
    private val mlModel: MLModel
) {
    
    companion object {
        private const val MIN_CANDLES_FOR_ANALYSIS = 50
        private const val MIN_RR_RATIO = 1.5
        private const val MIN_CONFIDENCE = 0.6
        private const val MAX_SPREAD_PERCENT = 0.1
        private const val MIN_VOLUME_RATIO = 0.8
    }
    
    suspend fun generateSignals(marketData: MarketData): List<Signal> = withContext(Dispatchers.Default) {
        try {
            val signals = mutableListOf<Signal>()
            val candles = marketData.candles
            
            if (candles.size < MIN_CANDLES_FOR_ANALYSIS) {
                Timber.w("Insufficient candles for analysis: ${candles.size} < $MIN_CANDLES_FOR_ANALYSIS")
                return@withContext emptyList()
            }
            
            // Perform technical analysis
            val analysisResult = performTechnicalAnalysis(marketData)
            
            // Generate signals based on different strategies
            val trendFollowingSignals = generateTrendFollowingSignals(marketData, analysisResult)
            val meanReversionSignals = generateMeanReversionSignals(marketData, analysisResult)
            val breakoutSignals = generateBreakoutSignals(marketData, analysisResult)
            val mlSignals = generateMLBasedSignals(marketData, analysisResult)
            
            // Combine and filter signals
            val allSignals = trendFollowingSignals + meanReversionSignals + breakoutSignals + mlSignals
            val filteredSignals = filterAndRankSignals(allSignals, analysisResult)
            
            // Apply risk management
            val riskManagedSignals = filteredSignals.mapNotNull { signal ->
                riskManager.evaluateSignal(signal, marketData)
            }
            
            // Limit number of signals per pair
            val limitedSignals = riskManagedSignals.take(2)
            
            signals.addAll(limitedSignals)
            
            Timber.d("Generated ${signals.size} signals for ${marketData.pair}")
            signals
            
        } catch (e: Exception) {
            Timber.e(e, "Error generating signals for ${marketData.pair}")
            emptyList()
        }
    }
    
    private fun performTechnicalAnalysis(marketData: MarketData): TechnicalAnalysisResult {
        val candles = marketData.candles
        val closes = candles.map { it.close }
        val highs = candles.map { it.high }
        val lows = candles.map { it.low }
        val volumes = candles.map { it.volume }
        
        // Calculate indicators
        val sma20 = technicalIndicators.calculateSMA(closes, 20)
        val sma50 = technicalIndicators.calculateSMA(closes, 50)
        val sma200 = technicalIndicators.calculateSMA(closes, 200)
        
        val ema12 = technicalIndicators.calculateEMA(closes, 12)
        val ema26 = technicalIndicators.calculateEMA(closes, 26)
        
        val rsi = technicalIndicators.calculateRSI(closes, 14)
        val macd = technicalIndicators.calculateMACD(closes)
        
        val atr = technicalIndicators.calculateATR(candles, 14)
        val bollingerBands = technicalIndicators.calculateBollingerBands(closes, 20)
        val stochastic = technicalIndicators.calculateStochastic(candles)
        
        val supportResistance = technicalIndicators.findSupportResistance(candles)
        val volumeAnalysis = technicalIndicators.calculateVolumeIndicators(volumes)
        val volatility = technicalIndicators.calculateVolatility(closes)
        
        return TechnicalAnalysisResult(
            sma20 = sma20,
            sma50 = sma50,
            sma200 = sma200,
            ema12 = ema12,
            ema26 = ema26,
            rsi = rsi,
            macd = macd,
            atr = atr,
            bollingerBands = bollingerBands,
            stochastic = stochastic,
            supportResistance = supportResistance,
            volumeAnalysis = volumeAnalysis,
            volatility = volatility,
            currentPrice = closes.last(),
            currentVolume = volumes.last()
        )
    }
    
    private fun generateTrendFollowingSignals(
        marketData: MarketData,
        analysis: TechnicalAnalysisResult
    ): List<Signal> {
        val signals = mutableListOf<Signal>()
        val currentPrice = analysis.currentPrice
        
        // Golden Cross / Death Cross signals
        if (analysis.sma20.isNotEmpty() && analysis.sma50.isNotEmpty()) {
            val currentSMA20 = analysis.sma20.last()
            val currentSMA50 = analysis.sma50.last()
            val prevSMA20 = analysis.sma20.getOrNull(analysis.sma20.size - 2) ?: currentSMA20
            val prevSMA50 = analysis.sma50.getOrNull(analysis.sma50.size - 2) ?: currentSMA50
            
            // Golden Cross (bullish)
            if (prevSMA20 <= prevSMA50 && currentSMA20 > currentSMA50) {
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.LONG,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "Golden Cross",
                    confidence = 0.75
                )
                signal?.let { signals.add(it) }
            }
            
            // Death Cross (bearish)
            if (prevSMA20 >= prevSMA50 && currentSMA20 < currentSMA50) {
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.SHORT,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "Death Cross",
                    confidence = 0.75
                )
                signal?.let { signals.add(it) }
            }
        }
        
        // EMA crossover signals
        if (analysis.ema12.isNotEmpty() && analysis.ema26.isNotEmpty()) {
            val currentEMA12 = analysis.ema12.last()
            val currentEMA26 = analysis.ema26.last()
            val prevEMA12 = analysis.ema12.getOrNull(analysis.ema12.size - 2) ?: currentEMA12
            val prevEMA26 = analysis.ema26.getOrNull(analysis.ema26.size - 2) ?: currentEMA26
            
            // Bullish crossover
            if (prevEMA12 <= prevEMA26 && currentEMA12 > currentEMA26) {
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.LONG,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "EMA Bullish Cross",
                    confidence = 0.70
                )
                signal?.let { signals.add(it) }
            }
            
            // Bearish crossover
            if (prevEMA12 >= prevEMA26 && currentEMA12 < currentEMA26) {
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.SHORT,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "EMA Bearish Cross",
                    confidence = 0.70
                )
                signal?.let { signals.add(it) }
            }
        }
        
        return signals
    }
    
    private fun generateMeanReversionSignals(
        marketData: MarketData,
        analysis: TechnicalAnalysisResult
    ): List<Signal> {
        val signals = mutableListOf<Signal>()
        val currentPrice = analysis.currentPrice
        
        // RSI mean reversion
        if (analysis.rsi.isNotEmpty()) {
            val currentRSI = analysis.rsi.last()
            
            // Oversold - potential long
            if (currentRSI < 30) {
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.LONG,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "RSI Oversold",
                    confidence = 0.65
                )
                signal?.let { signals.add(it) }
            }
            
            // Overbought - potential short
            if (currentRSI > 70) {
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.SHORT,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "RSI Overbought",
                    confidence = 0.65
                )
                signal?.let { signals.add(it) }
            }
        }
        
        // Bollinger Bands mean reversion
        if (analysis.bollingerBands.lower.isNotEmpty() && analysis.bollingerBands.upper.isNotEmpty()) {
            val lowerBand = analysis.bollingerBands.lower.last()
            val upperBand = analysis.bollingerBands.upper.last()
            
            // Price at lower band - potential long
            if (currentPrice <= lowerBand * 1.01) { // 1% tolerance
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.LONG,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "BB Lower Band",
                    confidence = 0.68
                )
                signal?.let { signals.add(it) }
            }
            
            // Price at upper band - potential short
            if (currentPrice >= upperBand * 0.99) { // 1% tolerance
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.SHORT,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "BB Upper Band",
                    confidence = 0.68
                )
                signal?.let { signals.add(it) }
            }
        }
        
        return signals
    }
    
    private fun generateBreakoutSignals(
        marketData: MarketData,
        analysis: TechnicalAnalysisResult
    ): List<Signal> {
        val signals = mutableListOf<Signal>()
        val currentPrice = analysis.currentPrice
        
        // Support/Resistance breakout
        val supportLevels = analysis.supportResistance.support
        val resistanceLevels = analysis.supportResistance.resistance
        
        // Breakout above resistance
        for (resistance in resistanceLevels.reversed()) {
            if (currentPrice > resistance * 1.002 && currentPrice < resistance * 1.02) { // 0.2% breakout, max 2%
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.LONG,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "Resistance Breakout",
                    confidence = 0.72
                )
                signal?.let { signals.add(it) }
                break
            }
        }
        
        // Breakout below support
        for (support in supportLevels) {
            if (currentPrice < support * 0.998 && currentPrice > support * 0.98) { // 0.2% breakout, max 2%
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = Direction.SHORT,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "Support Breakout",
                    confidence = 0.72
                )
                signal?.let { signals.add(it) }
                break
            }
        }
        
        // Volume breakout
        if (analysis.volumeAnalysis.isHighVolume) {
            val recentCandles = marketData.candles.takeLast(10)
            val avgVolume = recentCandles.dropLast(1).map { it.volume }.average()
            val currentVolume = recentCandles.last().volume
            
            if (currentVolume > avgVolume * 2) {
                // Determine direction based on price action
                val priceChange = (marketData.candles.last().close - marketData.candles.last().open) / marketData.candles.last().open
                
                val direction = if (priceChange > 0) Direction.LONG else Direction.SHORT
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = direction,
                    entryPrice = currentPrice,
                    analysis = analysis,
                    strategy = "Volume Breakout",
                    confidence = 0.66
                )
                signal?.let { signals.add(it) }
            }
        }
        
        return signals
    }
    
    private fun generateMLBasedSignals(
        marketData: MarketData,
        analysis: TechnicalAnalysisResult
    ): List<Signal> {
        val signals = mutableListOf<Signal>()
        
        try {
            // Prepare features for ML model
            val features = prepareMLFeatures(marketData, analysis)
            
            // Get prediction from ML model
            val prediction = mlModel.predictDirection(features)
            val confidence = mlModel.calculateConfidence(features)
            
            if (confidence >= MIN_CONFIDENCE) {
                val direction = when (prediction) {
                    PredictionResult.UP -> Direction.LONG
                    PredictionResult.DOWN -> Direction.SHORT
                    PredictionResult.SIDEWAYS -> return emptyList()
                }
                
                val signal = createSignal(
                    pair = marketData.pair,
                    direction = direction,
                    entryPrice = analysis.currentPrice,
                    analysis = analysis,
                    strategy = "ML Model",
                    confidence = confidence
                )
                signal?.let { signals.add(it) }
            }
        } catch (e: Exception) {
            Timber.w(e, "ML model prediction failed")
        }
        
        return signals
    }
    
    private fun prepareMLFeatures(marketData: MarketData, analysis: TechnicalAnalysisResult): DoubleArray {
        val features = mutableListOf<Double>()
        val currentPrice = analysis.currentPrice
        
        // Price features
        val recentPrices = marketData.candles.takeLast(20).map { it.close }
        val priceReturns = recentPrices.zipWithNext { a, b -> (b - a) / a }
        features.addAll(priceReturns.takeLast(10))
        
        // Technical indicator features
        analysis.rsi.lastOrNull()?.let { features.add(it / 100.0) } ?: features.add(0.5)
        analysis.macd.histogram.lastOrNull()?.let { features.add(it / currentPrice) } ?: features.add(0.0)
        analysis.stochastic.kValues.lastOrNull()?.let { features.add(it / 100.0) } ?: features.add(0.5)
        
        // Volume features
        features.add(analysis.volumeAnalysis.volumeRatio)
        features.add(analysis.volumeAnalysis.currentVolume / analysis.volumeAnalysis.averageVolume)
        
        // Volatility features
        features.add(analysis.volatility.currentVolatility)
        features.add(analysis.volatility.annualizedVolatility)
        
        // Support/Resistance distance
        val nearestResistance = analysis.supportResistance.resistance.find { it > currentPrice }
        val nearestSupport = analysis.supportResistance.support.find { it < currentPrice }
        
        features.add(nearestResistance?.let { (it - currentPrice) / currentPrice } ?: 0.1)
        features.add(nearestSupport?.let { (currentPrice - it) / currentPrice } ?: 0.1)
        
        return features.toDoubleArray()
    }
    
    private fun createSignal(
        pair: String,
        direction: Direction,
        entryPrice: Double,
        analysis: TechnicalAnalysisResult,
        strategy: String,
        confidence: Double
    ): Signal? {
        try {
            // Calculate stop loss using ATR
            val atr = analysis.atr.lastOrNull() ?: return null
            val stopLoss = when (direction) {
                Direction.LONG -> entryPrice - (atr * 2)
                Direction.SHORT -> entryPrice + (atr * 2)
            }
            
            // Find support/resistance for take profit
            val takeProfit = calculateTakeProfit(
                direction = direction,
                entryPrice = entryPrice,
                stopLoss = stopLoss,
                analysis = analysis
            )
            
            // Calculate R/R ratio
            val risk = abs(entryPrice - stopLoss)
            val reward = abs(takeProfit - entryPrice)
            val riskReward = reward / risk
            
            // Filter out low R/R signals
            if (riskReward < MIN_RR_RATIO) {
                Timber.d("Signal filtered due to low R/R: $riskReward < $MIN_RR_RATIO")
                return null
            }
            
            // Calculate recommended leverage
            val leverage = calculateLeverage(confidence, riskReward, analysis.volatility)
            
            // Generate comment
            val comment = generateSignalComment(direction, strategy, analysis)
            
            return Signal(
                id = UUID.randomUUID().toString(),
                pair = pair,
                direction = direction,
                entryPrice = entryPrice,
                entryRange = null, // Could be added for limit orders
                stopLoss = stopLoss,
                takeProfit = takeProfit,
                leverage = leverage,
                confidence = confidence,
                timeframe = Timeframe.H1, // Default, could be parameterized
                horizon = "4-8 часов", // Default horizon
                comment = comment,
                riskReward = riskReward,
                timestamp = System.currentTimeMillis(),
                status = SignalStatus.ACTIVE
            )
        } catch (e: Exception) {
            Timber.e(e, "Error creating signal")
            return null
        }
    }
    
    private fun calculateTakeProfit(
        direction: Direction,
        entryPrice: Double,
        stopLoss: Double,
        analysis: TechnicalAnalysisResult
    ): Double {
        val risk = abs(entryPrice - stopLoss)
        val minReward = risk * MIN_RR_RATIO
        
        return when (direction) {
            Direction.LONG -> {
                // Look for resistance levels above entry
                val resistances = analysis.supportResistance.resistance
                    .filter { it > entryPrice }
                    .sorted()
                
                resistances.firstOrNull { it >= entryPrice + minReward }
                    ?: entryPrice + minReward
            }
            Direction.SHORT -> {
                // Look for support levels below entry
                val supports = analysis.supportResistance.support
                    .filter { it < entryPrice }
                    .sortedDescending()
                
                supports.firstOrNull { it <= entryPrice - minReward }
                    ?: entryPrice - minReward
            }
        }
    }
    
    private fun calculateLeverage(
        confidence: Double,
        riskReward: Double,
        volatility: VolatilityMetrics
    ): Double {
        // Base leverage on confidence and R/R
        var leverage = 1.0 + (confidence * 2.0) + (riskReward * 0.5)
        
        // Adjust for volatility (lower leverage for high volatility)
        val volatilityAdjustment = 1.0 / (1.0 + volatility.currentVolatility * 100)
        leverage *= volatilityAdjustment
        
        // Cap at reasonable levels
        return leverage.coerceIn(1.0, 5.0)
    }
    
    private fun generateSignalComment(
        direction: Direction,
        strategy: String,
        analysis: TechnicalAnalysisResult
    ): String {
        val currentRSI = analysis.rsi.lastOrNull()?.let { "RSI: ${it.toInt()}" } ?: ""
        val currentVolume = if (analysis.volumeAnalysis.isHighVolume) "высокий объем" else ""
        
        return buildString {
            append("$strategy. ")
            if (currentRSI.isNotEmpty()) append("$currentRSI. ")
            if (currentVolume.isNotEmpty()) append("$currentVolume. ")
            
            when (direction) {
                Direction.LONG -> append("Ожидается рост.")
                Direction.SHORT -> append("Ожидается падение.")
            }
        }.trim()
    }
    
    private fun filterAndRankSignals(
        signals: List<Signal>,
        analysis: TechnicalAnalysisResult
    ): List<Signal> {
        return signals
            .filter { signal ->
                // Filter by confidence
                signal.confidence >= MIN_CONFIDENCE &&
                // Filter by R/R
                signal.riskReward >= MIN_RR_RATIO &&
                // Filter by spread (if we had order book data)
                true
            }
            .sortedByDescending { signal ->
                // Rank by confidence * R/R ratio
                signal.confidence * signal.riskReward
            }
    }
}

// Data classes for analysis results
data class TechnicalAnalysisResult(
    val sma20: List<Double>,
    val sma50: List<Double>,
    val sma200: List<Double>,
    val ema12: List<Double>,
    val ema26: List<Double>,
    val rsi: List<Double>,
    val macd: com.cryptosignal.assistant.domain.services.MACDResult,
    val atr: List<Double>,
    val bollingerBands: com.cryptosignal.assistant.domain.services.BollingerBands,
    val stochastic: com.cryptosignal.assistant.domain.services.StochasticResult,
    val supportResistance: com.cryptosignal.assistant.domain.services.SupportResistanceLevels,
    val volumeAnalysis: com.cryptosignal.assistant.domain.services.VolumeAnalysis,
    val volatility: com.cryptosignal.assistant.domain.services.VolatilityMetrics,
    val currentPrice: Double,
    val currentVolume: Double
)

// Simple ML model interface (placeholder for now)
class MLModel {
    
    fun predictDirection(features: DoubleArray): PredictionResult {
        // Simple rule-based logic for now
        // In production, this would use a trained TensorFlow Lite model
        
        val rsiIndex = 10 // Index of RSI in features
        val volumeRatioIndex = 20 // Index of volume ratio
        
        val rsi = if (features.size > rsiIndex) features[rsiIndex] * 100 else 50.0
        val volumeRatio = if (features.size > volumeRatioIndex) features[volumeRatioIndex] else 1.0
        
        return when {
            rsi < 30 && volumeRatio > 1.2 -> PredictionResult.UP
            rsi > 70 && volumeRatio > 1.2 -> PredictionResult.DOWN
            else -> PredictionResult.SIDEWAYS
        }
    }
    
    fun calculateConfidence(features: DoubleArray): Double {
        // Simple confidence calculation
        // In production, this would use model's probability output
        
        return 0.65 // Default confidence
    }
}

enum class PredictionResult {
    UP, DOWN, SIDEWAYS
}