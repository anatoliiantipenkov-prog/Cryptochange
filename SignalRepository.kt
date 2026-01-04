package com.cryptosignal.assistant.domain.repository

import com.cryptosignal.assistant.domain.models.Signal
import com.cryptosignal.assistant.domain.models.SignalStatus
import kotlinx.coroutines.flow.Flow

interface SignalRepository {
    
    fun getSignals(): Flow<List<Signal>>
    
    fun getActiveSignals(): Flow<List<Signal>>
    
    fun getCompletedSignals(): Flow<List<Signal>>
    
    fun getSignalsByPair(pair: String): Flow<List<Signal>>
    
    fun getSignalsByDirection(direction: String): Flow<List<Signal>>
    
    suspend fun getSignalById(id: String): Signal?
    
    suspend fun saveSignal(signal: Signal)
    
    suspend fun updateSignalStatus(id: String, status: SignalStatus, outcome: com.cryptosignal.assistant.domain.models.SignalOutcome? = null)
    
    suspend fun deleteSignal(id: String)
    
    suspend fun clearAllSignals()
    
    suspend fun getLastSignalTimestamp(): Long?
    
    suspend fun hasRecentSignal(pair: String, minInterval: Long): Boolean
}