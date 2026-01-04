package com.cryptosignal.assistant.data.repository

import com.cryptosignal.assistant.data.local.database.dao.SignalDao
import com.cryptosignal.assistant.data.local.database.entity.SignalEntity
import com.cryptosignal.assistant.domain.models.*
import com.cryptosignal.assistant.domain.repository.SignalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SignalRepositoryImpl @Inject constructor(
    private val signalDao: SignalDao
) : SignalRepository {
    
    override fun getSignals(): Flow<List<Signal>> {
        return signalDao.getAllSignals().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getActiveSignals(): Flow<List<Signal>> {
        return signalDao.getActiveSignals().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getCompletedSignals(): Flow<List<Signal>> {
        return signalDao.getCompletedSignals().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getSignalsByPair(pair: String): Flow<List<Signal>> {
        return signalDao.getSignalsByPair(pair).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getSignalsByDirection(direction: String): Flow<List<Signal>> {
        val dir = Direction.valueOf(direction.uppercase())
        return signalDao.getSignalsByDirection(dir).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getSignalById(id: String): Signal? {
        return signalDao.getSignalById(id)?.toDomainModel()
    }
    
    override suspend fun saveSignal(signal: Signal) {
        signalDao.insertSignal(signal.toEntity())
    }
    
    override suspend fun updateSignalStatus(
        id: String,
        status: SignalStatus,
        outcome: SignalOutcome?
    ) {
        signalDao.updateSignalStatus(id, status, outcome)
    }
    
    override suspend fun deleteSignal(id: String) {
        signalDao.deleteSignal(id)
    }
    
    override suspend fun clearAllSignals() {
        signalDao.clearAllSignals()
    }
    
    override suspend fun getLastSignalTimestamp(): Long? {
        return signalDao.getLastSignalTimestamp()
    }
    
    override suspend fun hasRecentSignal(pair: String, minInterval: Long): Boolean {
        val sinceTimestamp = System.currentTimeMillis() - minInterval
        val count = signalDao.getRecentSignalsCount(pair, sinceTimestamp)
        return count > 0
    }
}

// Extension functions for mapping
private fun SignalEntity.toDomainModel(): Signal {
    return Signal(
        id = id,
        pair = pair,
        direction = direction,
        entryPrice = entryPrice,
        entryRange = if (entryRangeMin != null && entryRangeMax != null) {
            PriceRange(entryRangeMin, entryRangeMax)
        } else null,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        leverage = leverage,
        confidence = confidence,
        timeframe = timeframe,
        horizon = horizon,
        comment = comment,
        riskReward = riskReward,
        timestamp = timestamp,
        status = status,
        outcome = outcome
    )
}

private fun Signal.toEntity(): SignalEntity {
    return SignalEntity(
        id = id,
        pair = pair,
        direction = direction,
        entryPrice = entryPrice,
        entryRangeMin = entryRange?.min,
        entryRangeMax = entryRange?.max,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        leverage = leverage,
        confidence = confidence,
        timeframe = timeframe,
        horizon = horizon,
        comment = comment,
        riskReward = riskReward,
        timestamp = timestamp,
        status = status,
        outcome = outcome
    )
}