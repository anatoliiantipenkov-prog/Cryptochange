package com.cryptosignal.assistant.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Statistics(
    val winRate: Double,
    val profitFactor: Double,
    val maxDrawdown: Double,
    val averageRR: Double,
    val totalSignals: Int,
    val activeSignals: Int,
    val profitableSignals: Int,
    val losingSignals: Int,
    val totalProfit: Double,
    val totalLoss: Double,
    val netProfit: Double,
    val profitByPair: Map<String, Double>,
    val profitByTimeframe: Map<Timeframe, Double>,
    val profitByMonth: List<MonthlyProfit>,
    val currentStreak: Int,
    val maxWinStreak: Int,
    val maxLossStreak: Int,
    val largestWin: Double,
    val largestLoss: Double,
    val averageWin: Double,
    val averageLoss: Double,
    val expectancy: Double,
    val sharpeRatio: Double,
    val calmarRatio: Double,
    val period: TimeRange = TimeRange.ALL
)

@Serializable
data class MonthlyProfit(
    val year: Int,
    val month: Int,
    val profit: Double,
    val winRate: Double,
    val signalsCount: Int
)

enum class TimeRange {
    DAY, WEEK, MONTH, QUARTER, YEAR, ALL
}

@Serializable
data class RiskMetrics(
    val riskPerTrade: Double = 0.02,
    val maxDrawdownLimit: Double = 0.20,
    val currentDrawdown: Double = 0.0,
    val isInHighRiskZone: Boolean = false,
    val recommendedAggressiveness: AggressivenessLevel = AggressivenessLevel.BALANCED
)

enum class AggressivenessLevel {
    CONSERVATIVE, BALANCED, AGGRESSIVE
}