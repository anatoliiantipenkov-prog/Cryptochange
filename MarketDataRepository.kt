package com.cryptosignal.assistant.domain.repository

import com.cryptosignal.assistant.domain.models.Candle
import com.cryptosignal.assistant.domain.models.MarketData
import com.cryptosignal.assistant.domain.models.Ticker
import com.cryptosignal.assistant.domain.models.Timeframe

interface MarketDataRepository {
    
    suspend fun getKlines(
        pair: String,
        timeframe: Timeframe,
        limit: Int = 100
    ): List<Candle>
    
    suspend fun getTicker(pair: String): Ticker
    
    suspend fun getOrderBook(pair: String, depth: Int = 20): com.cryptosignal.assistant.domain.models.OrderBook?
    
    suspend fun getFundingRate(pair: String): Double?
    
    suspend fun getOpenInterest(pair: String): Double?
    
    suspend fun getMarketData(
        pair: String,
        timeframe: Timeframe,
        candleLimit: Int = 100
    ): MarketData
    
    suspend fun getMultipleMarketData(
        pairs: List<String>,
        timeframe: Timeframe,
        candleLimit: Int = 50
    ): Map<String, MarketData>
    
    suspend fun getAvailablePairs(): List<String>
    
    suspend fun isPairAvailable(pair: String): Boolean
}