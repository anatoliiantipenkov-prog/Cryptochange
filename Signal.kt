package com.cryptosignal.assistant.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Signal(
    val id: String,
    val pair: String,
    val direction: Direction,
    val entryPrice: Double,
    val entryRange: PriceRange?,
    val stopLoss: Double,
    val takeProfit: Double,
    val leverage: Double,
    val confidence: Double,
    val timeframe: Timeframe,
    val horizon: String,
    val comment: String,
    val riskReward: Double,
    val timestamp: Long,
    val status: SignalStatus,
    val outcome: SignalOutcome? = null
) {
    fun isActive(): Boolean = status == SignalStatus.ACTIVE
    fun isCompleted(): Boolean = status == SignalStatus.COMPLETED
    fun isLong(): Boolean = direction == Direction.LONG
    fun isShort(): Boolean = direction == Direction.SHORT
    
    fun calculateCurrentPnL(currentPrice: Double): Double {
        return if (isLong()) {
            (currentPrice - entryPrice) / entryPrice
        } else {
            (entryPrice - currentPrice) / entryPrice
        }
    }
    
    fun isStopLossHit(currentPrice: Double): Boolean {
        return if (isLong()) {
            currentPrice <= stopLoss
        } else {
            currentPrice >= stopLoss
        }
    }
    
    fun isTakeProfitHit(currentPrice: Double): Boolean {
        return if (isLong()) {
            currentPrice >= takeProfit
        } else {
            currentPrice <= takeProfit
        }
    }
}

enum class Direction {
    LONG, SHORT
}

enum class SignalStatus {
    ACTIVE, COMPLETED, CANCELLED
}

@Serializable
data class SignalOutcome(
    val exitPrice: Double,
    val exitTimestamp: Long,
    val pnlPercent: Double,
    val pnlR: Double,
    val reason: ExitReason
)

enum class ExitReason {
    TAKE_PROFIT, STOP_LOSS, TIMEOUT, MANUAL
}

@Serializable
data class PriceRange(
    val min: Double,
    val max: Double
)

enum class Timeframe(val minutes: Int) {
    M1(1),
    M5(5),
    M15(15),
    M30(30),
    H1(60),
    H2(120),
    H4(240),
    D1(1440);
    
    companion object {
        fun fromString(value: String): Timeframe {
            return values().find { it.name == value } ?: H1
        }
    }
}