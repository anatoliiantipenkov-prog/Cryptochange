package com.cryptosignal.assistant.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.cryptosignal.assistant.data.local.database.converters.Converters
import com.cryptosignal.assistant.domain.models.Direction
import com.cryptosignal.assistant.domain.models.SignalOutcome
import com.cryptosignal.assistant.domain.models.SignalStatus
import com.cryptosignal.assistant.domain.models.Timeframe

@Entity(tableName = "signals")
@TypeConverters(Converters::class)
data class SignalEntity(
    @PrimaryKey
    val id: String,
    
    val pair: String,
    
    val direction: Direction,
    
    val entryPrice: Double,
    
    val entryRangeMin: Double?,
    val entryRangeMax: Double?,
    
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
    
    val outcome: SignalOutcome? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val updatedAt: Long = System.currentTimeMillis()
)