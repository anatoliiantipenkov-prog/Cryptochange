package com.cryptosignal.assistant.data.local.database.converters

import androidx.room.TypeConverter
import com.cryptosignal.assistant.domain.models.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // Direction
    @TypeConverter
    fun fromDirection(direction: Direction): String {
        return direction.name
    }
    
    @TypeConverter
    fun toDirection(directionString: String): Direction {
        return Direction.valueOf(directionString)
    }
    
    // SignalStatus
    @TypeConverter
    fun fromSignalStatus(status: SignalStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toSignalStatus(statusString: String): SignalStatus {
        return SignalStatus.valueOf(statusString)
    }
    
    // Timeframe
    @TypeConverter
    fun fromTimeframe(timeframe: Timeframe): String {
        return timeframe.name
    }
    
    @TypeConverter
    fun toTimeframe(timeframeString: String): Timeframe {
        return Timeframe.valueOf(timeframeString)
    }
    
    // SignalOutcome
    @TypeConverter
    fun fromSignalOutcome(outcome: SignalOutcome?): String? {
        return outcome?.let { json.encodeToString(it) }
    }
    
    @TypeConverter
    fun toSignalOutcome(outcomeString: String?): SignalOutcome? {
        return outcomeString?.let { json.decodeFromString(it) }
    }
    
    // ExitReason
    @TypeConverter
    fun fromExitReason(reason: ExitReason): String {
        return reason.name
    }
    
    @TypeConverter
    fun toExitReason(reasonString: String): ExitReason {
        return ExitReason.valueOf(reasonString)
    }
    
    // List<String> for trading pairs
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return json.encodeToString(list)
    }
    
    @TypeConverter
    fun toStringList(listString: String): List<String> {
        return json.decodeFromString(listString)
    }
}