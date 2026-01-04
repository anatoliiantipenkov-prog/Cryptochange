package com.cryptosignal.assistant.data.api

import com.cryptosignal.assistant.data.api.dto.MEXCCandleDto
import com.cryptosignal.assistant.data.api.dto.MEXCOrderBookDto
import com.cryptosignal.assistant.data.api.dto.MEXCTickerDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MEXCApiClient {
    
    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100,
        @Query("startTime") startTime: Long? = null,
        @Query("endTime") endTime: Long? = null
    ): List<MEXCCandleDto>
    
    @GET("api/v3/ticker/24hr")
    suspend fun getTicker24h(
        @Query("symbol") symbol: String
    ): MEXCTickerDto
    
    @GET("api/v3/ticker/24hr")
    suspend fun getAllTickers24h(): List<MEXCTickerDto>
    
    @GET("api/v3/depth")
    suspend fun getOrderBook(
        @Query("symbol") symbol: String,
        @Query("limit") limit: Int = 20
    ): MEXCOrderBookDto
    
    @GET("api/v3/exchangeInfo")
    suspend fun getExchangeInfo(): MEXCExchangeInfoDto
    
    // Futures endpoints
    @GET("fapi/v1/klines")
    suspend fun getFuturesKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100
    ): List<MEXCCandleDto>
    
    @GET("fapi/v1/ticker/24hr")
    suspend fun getFuturesTicker24h(
        @Query("symbol") symbol: String
    ): MEXCTickerDto
    
    @GET("fapi/v1/depth")
    suspend fun getFuturesOrderBook(
        @Query("symbol") symbol: String,
        @Query("limit") limit: Int = 20
    ): MEXCOrderBookDto
    
    @GET("fapi/v1/fundingRate")
    suspend fun getFundingRate(
        @Query("symbol") symbol: String,
        @Query("limit") limit: Int = 100
    ): List<MEXCFundingRateDto>
    
    @GET("fapi/v1/openInterest")
    suspend fun getOpenInterest(
        @Query("symbol") symbol: String
    ): MEXCOpenInterestDto
}

// Data Transfer Objects
data class MEXCExchangeInfoDto(
    val timezone: String,
    val serverTime: Long,
    val rateLimits: List<MEXCRateLimit>,
    val symbols: List<MEXCSymbolInfo>
)

data class MEXCRateLimit(
    val rateLimitType: String,
    val interval: String,
    val intervalNum: Int,
    val limit: Int
)

data class MEXCSymbolInfo(
    val symbol: String,
    val status: String,
    val baseAsset: String,
    val baseAssetPrecision: Int,
    val quoteAsset: String,
    val quotePrecision: Int,
    val quoteAssetPrecision: Int,
    val orderTypes: List<String>,
    val icebergAllowed: Boolean,
    val ocoAllowed: Boolean,
    val quoteOrderQtyMarketAllowed: Boolean,
    val allowTrailingStop: Boolean,
    val isSpotTradingAllowed: Boolean,
    val isMarginTradingAllowed: Boolean,
    val filters: List<Map<String, Any>>,
    val permissions: List<String>
)

data class MEXCCandleDto(
    val openTime: Long,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val volume: String,
    val closeTime: Long,
    val quoteAssetVolume: String,
    val numberOfTrades: Int,
    val takerBuyBaseAssetVolume: String,
    val takerBuyQuoteAssetVolume: String
)

data class MEXCTickerDto(
    val symbol: String,
    val priceChange: String,
    val priceChangePercent: String,
    val weightedAvgPrice: String,
    val prevClosePrice: String,
    val lastPrice: String,
    val lastQty: String,
    val bidPrice: String,
    val bidQty: String,
    val askPrice: String,
    val askQty: String,
    val openPrice: String,
    val highPrice: String,
    val lowPrice: String,
    val volume: String,
    val quoteVolume: String,
    val openTime: Long,
    val closeTime: Long,
    val firstId: Long,
    val lastId: Long,
    val count: Int
)

data class MEXCOrderBookDto(
    val lastUpdateId: Long,
    val bids: List<List<String>>,
    val asks: List<List<String>>
)

data class MEXCFundingRateDto(
    val symbol: String,
    val fundingRate: String,
    val fundingTime: Long
)

data class MEXCOpenInterestDto(
    val symbol: String,
    val openInterest: String,
    val timestamp: Long
)