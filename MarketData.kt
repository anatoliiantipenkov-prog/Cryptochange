package com.cryptosignal.assistant.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class MarketData(
    val pair: String,
    val candles: List<Candle>,
    val volume: VolumeData,
    val orderBook: OrderBook? = null,
    val fundingRate: Double? = null,
    val openInterest: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Candle(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val closeTime: Long,
    val quoteAssetVolume: Double,
    val numberOfTrades: Int,
    val takerBuyBaseAssetVolume: Double,
    val takerBuyQuoteAssetVolume: Double
) {
    fun getPriceRange(): Double = high - low
    fun getBodySize(): Double = Math.abs(close - open)
    fun isBullish(): Boolean = close > open
    fun isBearish(): Boolean = close < open
}

@Serializable
data class VolumeData(
    val baseVolume: Double,
    val quoteVolume: Double,
    val average24h: Double,
    val change24h: Double
)

@Serializable
data class OrderBook(
    val bids: List<OrderBookEntry>,
    val asks: List<OrderBookEntry>,
    val timestamp: Long
)

@Serializable
data class OrderBookEntry(
    val price: Double,
    val quantity: Double
)

@Serializable
data class Ticker(
    val symbol: String,
    val price: Double,
    val priceChange24h: Double,
    val priceChangePercent24h: Double,
    val weightedAvgPrice: Double,
    val prevClosePrice: Double,
    val lastPrice: Double,
    val lastQty: Double,
    val bidPrice: Double,
    val bidQty: Double,
    val askPrice: Double,
    val askQty: Double,
    val openPrice: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val volume: Double,
    val quoteVolume: Double,
    val openTime: Long,
    val closeTime: Long,
    val firstId: Long,
    val lastId: Long,
    val count: Int
)