package com.cryptosignal.assistant.data.local.database.dao

import androidx.room.*
import com.cryptosignal.assistant.data.local.database.entity.SignalEntity
import com.cryptosignal.assistant.domain.models.Direction
import com.cryptosignal.assistant.domain.models.SignalStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity)
    
    @Update
    suspend fun updateSignal(signal: SignalEntity)
    
    @Query("SELECT * FROM signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<SignalEntity>>
    
    @Query("SELECT * FROM signals WHERE status = :status ORDER BY timestamp DESC")
    fun getSignalsByStatus(status: SignalStatus): Flow<List<SignalEntity>>
    
    @Query("SELECT * FROM signals WHERE direction = :direction ORDER BY timestamp DESC")
    fun getSignalsByDirection(direction: Direction): Flow<List<SignalEntity>>
    
    @Query("SELECT * FROM signals WHERE pair = :pair ORDER BY timestamp DESC")
    fun getSignalsByPair(pair: String): Flow<List<SignalEntity>>
    
    @Query("SELECT * FROM signals WHERE pair = :pair AND status = :status ORDER BY timestamp DESC")
    fun getSignalsByPairAndStatus(pair: String, status: SignalStatus): Flow<List<SignalEntity>>
    
    @Query("SELECT * FROM signals WHERE id = :id")
    suspend fun getSignalById(id: String): SignalEntity?
    
    @Query("SELECT * FROM signals WHERE status = 'ACTIVE' ORDER BY timestamp DESC")
    fun getActiveSignals(): Flow<List<SignalEntity>>
    
    @Query("SELECT * FROM signals WHERE status = 'COMPLETED' ORDER BY timestamp DESC")
    fun getCompletedSignals(): Flow<List<SignalEntity>>
    
    @Query("DELETE FROM signals WHERE id = :id")
    suspend fun deleteSignal(id: String)
    
    @Query("DELETE FROM signals")
    suspend fun clearAllSignals()
    
    @Query("SELECT COUNT(*) FROM signals")
    suspend fun getSignalsCount(): Int
    
    @Query("SELECT COUNT(*) FROM signals WHERE status = 'ACTIVE'")
    suspend fun getActiveSignalsCount(): Int
    
    @Query("SELECT COUNT(*) FROM signals WHERE status = 'COMPLETED'")
    suspend fun getCompletedSignalsCount(): Int
    
    @Query("SELECT COUNT(*) FROM signals WHERE pair = :pair AND timestamp > :sinceTimestamp")
    suspend fun getRecentSignalsCount(pair: String, sinceTimestamp: Long): Int
    
    @Query("SELECT MAX(timestamp) FROM signals")
    suspend fun getLastSignalTimestamp(): Long?
    
    @Query("SELECT * FROM signals WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getSignalsInTimeRange(startTime: Long, endTime: Long): Flow<List<SignalEntity>>
    
    @Query("""
        SELECT * FROM signals 
        WHERE pair = :pair 
        AND direction = :direction 
        AND timestamp > :sinceTimestamp 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    suspend fun getLastSignalForPairAndDirection(
        pair: String, 
        direction: Direction, 
        sinceTimestamp: Long
    ): SignalEntity?
    
    @Query("""
        UPDATE signals 
        SET status = :newStatus, 
            outcome = :outcome,
            updatedAt = :updatedAt 
        WHERE id = :id
    """)
    suspend fun updateSignalStatus(
        id: String,
        newStatus: SignalStatus,
        outcome: com.cryptosignal.assistant.domain.models.SignalOutcome?,
        updatedAt: Long = System.currentTimeMillis()
    )
}