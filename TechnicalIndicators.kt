package com.cryptosignal.assistant.domain.services

import com.cryptosignal.assistant.domain.models.Candle
import kotlin.math.*

class TechnicalIndicators {
    
    // Simple Moving Average
    fun calculateSMA(prices: List<Double>, period: Int): List<Double> {
        if (prices.size < period) return emptyList()
        
        val sma = mutableListOf<Double>()
        for (i in period - 1 until prices.size) {
            val sum = prices.subList(i - period + 1, i + 1).sum()
            sma.add(sum / period)
        }
        return sma
    }
    
    // Exponential Moving Average
    fun calculateEMA(prices: List<Double>, period: Int): List<Double> {
        if (prices.size < period) return emptyList()
        
        val ema = mutableListOf<Double>()
        val multiplier = 2.0 / (period + 1)
        
        // First EMA is SMA
        val firstSMA = prices.take(period).average()
        ema.add(firstSMA)
        
        // Calculate subsequent EMAs
        for (i in period until prices.size) {
            val currentEma = (prices[i] * multiplier) + (ema.last() * (1 - multiplier))
            ema.add(currentEma)
        }
        
        return ema
    }
    
    // Relative Strength Index
    fun calculateRSI(prices: List<Double>, period: Int = 14): List<Double> {
        if (prices.size < period + 1) return emptyList()
        
        val rsi = mutableListOf<Double>()
        val gains = mutableListOf<Double>()
        val losses = mutableListOf<Double>()
        
        // Calculate price changes
        for (i in 1 until prices.size) {
            val change = prices[i] - prices[i - 1]
            gains.add(if (change > 0) change else 0.0)
            losses.add(if (change < 0) abs(change) else 0.0)
        }
        
        // Calculate initial average gain/loss
        var avgGain = gains.take(period).average()
        var avgLoss = losses.take(period).average()
        
        // Calculate initial RSI
        if (avgLoss == 0.0) {
            rsi.add(100.0)
        } else {
            val rs = avgGain / avgLoss
            rsi.add(100 - (100 / (1 + rs)))
        }
        
        // Calculate subsequent RSIs
        for (i in period until gains.size) {
            avgGain = ((avgGain * (period - 1)) + gains[i]) / period
            avgLoss = ((avgLoss * (period - 1)) + losses[i]) / period
            
            if (avgLoss == 0.0) {
                rsi.add(100.0)
            } else {
                val rs = avgGain / avgLoss
                rsi.add(100 - (100 / (1 + rs)))
            }
        }
        
        return rsi
    }
    
    // MACD (Moving Average Convergence Divergence)
    fun calculateMACD(
        prices: List<Double>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): MACDResult {
        val fastEMA = calculateEMA(prices, fastPeriod)
        val slowEMA = calculateEMA(prices, slowPeriod)
        
        if (fastEMA.isEmpty() || slowEMA.isEmpty()) {
            return MACDResult(emptyList(), emptyList(), emptyList())
        }
        
        // Align EMAs (slow EMA starts later)
        val startIndex = slowPeriod - fastPeriod
        val alignedFastEMA = if (startIndex >= 0 && startIndex < fastEMA.size) {
            fastEMA.subList(startIndex, fastEMA.size)
        } else {
            fastEMA
        }
        
        val alignedSlowEMA = slowEMA
        
        // Calculate MACD line
        val macdLine = mutableListOf<Double>()
        val minSize = min(alignedFastEMA.size, alignedSlowEMA.size)
        for (i in 0 until minSize) {
            macdLine.add(alignedFastEMA[i] - alignedSlowEMA[i])
        }
        
        // Calculate signal line (EMA of MACD)
        val signalLine = calculateEMA(macdLine, signalPeriod)
        
        // Calculate histogram
        val histogram = mutableListOf<Double>()
        val signalStartIndex = macdLine.size - signalLine.size
        for (i in signalLine.indices) {
            histogram.add(macdLine[signalStartIndex + i] - signalLine[i])
        }
        
        return MACDResult(macdLine, signalLine, histogram)
    }
    
    // Average True Range
    fun calculateATR(candles: List<Candle>, period: Int = 14): List<Double> {
        if (candles.size < period + 1) return emptyList()
        
        val trValues = mutableListOf<Double>()
        
        // Calculate True Range for each candle
        for (i in 1 until candles.size) {
            val current = candles[i]
            val previous = candles[i - 1]
            
            val tr1 = current.high - current.low
            val tr2 = abs(current.high - previous.close)
            val tr3 = abs(current.low - previous.close)
            
            trValues.add(max(max(tr1, tr2), tr3))
        }
        
        // Calculate ATR using SMA initially, then EMA
        val atr = mutableListOf<Double>()
        
        // Initial ATR is SMA of first N TR values
        val initialATR = trValues.take(period).average()
        atr.add(initialATR)
        
        // Subsequent ATRs use EMA formula
        val multiplier = 1.0 / period
        for (i in period until trValues.size) {
            val currentATR = (atr.last() * (1 - multiplier)) + (trValues[i] * multiplier)
            atr.add(currentATR)
        }
        
        return atr
    }
    
    // Bollinger Bands
    fun calculateBollingerBands(
        prices: List<Double>,
        period: Int = 20,
        multiplier: Double = 2.0
    ): BollingerBands {
        val sma = calculateSMA(prices, period)
        val upperBand = mutableListOf<Double>()
        val lowerBand = mutableListOf<Double>()
        
        for (i in 0 until sma.size) {
            val startIndex = i
            val endIndex = i + period
            if (endIndex > prices.size) break
            
            val periodPrices = prices.subList(startIndex, endIndex)
            val mean = periodPrices.average()
            val variance = periodPrices.map { (it - mean).pow(2) }.average()
            val stdDev = sqrt(variance)
            
            upperBand.add(sma[i] + (stdDev * multiplier))
            lowerBand.add(sma[i] - (stdDev * multiplier))
        }
        
        return BollingerBands(sma, upperBand, lowerBand)
    }
    
    // Stochastic Oscillator
    fun calculateStochastic(
        candles: List<Candle>,
        kPeriod: Int = 14,
        dPeriod: Int = 3
    ): StochasticResult {
        if (candles.size < kPeriod) return StochasticResult(emptyList(), emptyList())
        
        val kValues = mutableListOf<Double>()
        
        for (i in kPeriod - 1 until candles.size) {
            val current = candles[i]
            val periodCandles = candles.subList(i - kPeriod + 1, i + 1)
            
            val lowestLow = periodCandles.minOf { it.low }
            val highestHigh = periodCandles.maxOf { it.high }
            
            val kPercent = if (highestHigh - lowestLow != 0.0) {
                ((current.close - lowestLow) / (highestHigh - lowestLow)) * 100
            } else {
                50.0
            }
            
            kValues.add(kPercent)
        }
        
        // Calculate D values (SMA of K)
        val dValues = calculateSMA(kValues, dPeriod)
        
        return StochasticResult(kValues, dValues)
    }
    
    // Support and Resistance levels
    fun findSupportResistance(
        candles: List<Candle>,
        lookback: Int = 10,
        tolerance: Double = 0.02
    ): SupportResistanceLevels {
        val highs = candles.map { it.high }
        val lows = candles.map { it.low }
        
        val resistanceLevels = mutableSetOf<Double>()
        val supportLevels = mutableSetOf<Double>()
        
        // Find resistance levels (local maxima)
        for (i in lookback until highs.size - lookback) {
            val current = highs[i]
            val isMaxima = highs.subList(i - lookback, i + lookback + 1).all { it <= current }
            
            if (isMaxima) {
                // Check if this level is close to existing level
                val isUnique = resistanceLevels.none { abs(it - current) / current <= tolerance }
                if (isUnique) {
                    resistanceLevels.add(current)
                }
            }
        }
        
        // Find support levels (local minima)
        for (i in lookback until lows.size - lookback) {
            val current = lows[i]
            val isMinima = lows.subList(i - lookback, i + lookback + 1).all { it >= current }
            
            if (isMinima) {
                // Check if this level is close to existing level
                val isUnique = supportLevels.none { abs(it - current) / current <= tolerance }
                if (isUnique) {
                    supportLevels.add(current)
                }
            }
        }
        
        return SupportResistanceLevels(
            resistanceLevels.sorted(),
            supportLevels.sorted()
        )
    }
    
    // Volume analysis
    fun calculateVolumeIndicators(volumes: List<Double>): VolumeAnalysis {
        val avgVolume = volumes.average()
        val currentVolume = volumes.lastOrNull() ?: 0.0
        val volumeRatio = if (avgVolume != 0.0) currentVolume / avgVolume else 1.0
        
        // Volume moving average
        val volumeSMA20 = if (volumes.size >= 20) {
            volumes.takeLast(20).average()
        } else {
            avgVolume
        }
        
        return VolumeAnalysis(
            currentVolume = currentVolume,
            averageVolume = avgVolume,
            volumeSMA20 = volumeSMA20,
            volumeRatio = volumeRatio,
            isHighVolume = volumeRatio > 1.5,
            isLowVolume = volumeRatio < 0.5
        )
    }
    
    // Volatility calculation
    fun calculateVolatility(prices: List<Double>, period: Int = 20): VolatilityMetrics {
        val returns = mutableListOf<Double>()
        for (i in 1 until prices.size) {
            returns.add((prices[i] - prices[i-1]) / prices[i-1])
        }
        
        if (returns.size < period) {
            return VolatilityMetrics(0.0, 0.0, 0.0, 0.0)
        }
        
        val recentReturns = returns.takeLast(period)
        val meanReturn = recentReturns.average()
        val variance = recentReturns.map { (it - meanReturn).pow(2) }.average()
        val volatility = sqrt(variance)
        val annualizedVolatility = volatility * sqrt(365.0 * 24.0 * 60.0) // Assuming minute data
        
        return VolatilityMetrics(
            currentVolatility = volatility,
            annualizedVolatility = annualizedVolatility,
            meanReturn = meanReturn,
            variance = variance
        )
    }
}

// Data classes for indicator results
data class MACDResult(
    val macdLine: List<Double>,
    val signalLine: List<Double>,
    val histogram: List<Double>
)

data class BollingerBands(
    val middle: List<Double>,
    val upper: List<Double>,
    val lower: List<Double>
)

data class StochasticResult(
    val kValues: List<Double>,
    val dValues: List<Double>
)

data class SupportResistanceLevels(
    val resistance: List<Double>,
    val support: List<Double>
)

data class VolumeAnalysis(
    val currentVolume: Double,
    val averageVolume: Double,
    val volumeSMA20: Double,
    val volumeRatio: Double,
    val isHighVolume: Boolean,
    val isLowVolume: Boolean
)

data class VolatilityMetrics(
    val currentVolatility: Double,
    val annualizedVolatility: Double,
    val meanReturn: Double,
    val variance: Double
)