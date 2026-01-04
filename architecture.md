# Архитектура Crypto Signal Assistant

## Общая схема архитектуры

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER INTERFACE                           │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │ Main Screen │  │ Statistics   │  │ Settings           │    │
│  │ (Signals)   │  │ Screen       │  │ Screen             │    │
│  └─────────────┘  └──────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │ SignalCard  │  │ ChartViews   │  │ ViewModels         │    │
│  │ Components  │  │ Statistics   │  │ (State Management) │    │
│  └─────────────┘  └──────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                              │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │ Signal      │  │ Risk         │  │ Statistics         │    │
│  │ Generator   │  │ Manager      │  │ Calculator         │    │
│  └─────────────┘  └──────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    DATA LAYER                                  │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │ MEXC API    │  │ Local        │  │ Background         │    │
│  │ Client      │  │ Database     │  │ Sync Service       │    │
│  └─────────────┘  └──────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

## Детальная архитектура компонентов

### 1. User Interface Layer

#### MainActivity
```kotlin
class MainActivity : ComponentActivity() {
    // Navigation host
    // Theme setup
    // Permission handling
}
```

#### Screens
```kotlin
// Главный экран со списком сигналов
@Composable
fun SignalsScreen(
    viewModel: SignalsViewModel,
    onSignalClick: (Signal) -> Unit
)

// Экран статистики
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel
)

// Экран настроек
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
)
```

#### Components
```kotlin
// Карточка сигнала
@Composable
fun SignalCard(
    signal: Signal,
    onClick: () -> Unit
)

// График доходности
@Composable
fun ProfitChart(
    data: List<DataPoint>
)

// Статистические карточки
@Composable
fun MetricCard(
    title: String,
    value: String,
    change: String?
)
```

### 2. Presentation Layer (ViewModels)

#### SignalsViewModel
```kotlin
class SignalsViewModel @Inject constructor(
    private val getSignals: GetSignalsUseCase,
    private val observeSignals: ObserveSignalsUseCase
) : ViewModel() {
    
    val signals: StateFlow<List<Signal>> = _signals
    val activeSignals: StateFlow<List<Signal>> = _activeSignals
    
    fun loadSignals()
    fun refreshSignals()
    fun filterByStatus(status: SignalStatus)
}
```

#### StatisticsViewModel
```kotlin
class StatisticsViewModel @Inject constructor(
    private val calculateStats: CalculateStatisticsUseCase,
    private val getHistory: GetSignalHistoryUseCase
) : ViewModel() {
    
    val statistics: StateFlow<Statistics> = _statistics
    val chartData: StateFlow<List<DataPoint>> = _chartData
    
    fun loadStatistics()
    fun updateTimeRange(range: TimeRange)
}
```

#### SettingsViewModel
```kotlin
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    val settings: StateFlow<AppSettings> = _settings
    
    fun updateTradingPairs(pairs: List<String>)
    fun toggleNotifications(enabled: Boolean)
    fun updateAggressiveness(level: AggressivenessLevel)
}
```

### 3. Domain Layer (Use Cases)

#### Signal Generation
```kotlin
class GenerateSignalsUseCase @Inject constructor(
    private val signalGenerator: SignalGenerator
) {
    suspend operator fun invoke(marketData: MarketData): List<Signal>
}

class EvaluateSignalUseCase @Inject constructor(
    private val riskManager: RiskManager
) {
    suspend operator fun invoke(signal: Signal): EvaluatedSignal
}

class ExecuteSignalUseCase @Inject constructor(
    private val signalRepository: SignalRepository
) {
    suspend operator fun invoke(signal: Signal)
}
```

#### Statistics
```kotlin
class CalculateStatisticsUseCase @Inject constructor(
    private val statisticsCalculator: StatisticsCalculator
) {
    suspend operator fun invoke(
        period: TimeRange
    ): Statistics
}

class GetSignalHistoryUseCase @Inject constructor(
    private val signalRepository: SignalRepository
) {
    suspend operator fun invoke(): List<HistoricalSignal>
}
```

#### Risk Management
```kotlin
class CalculatePositionSizeUseCase @Inject constructor(
    private val riskManager: RiskManager
) {
    suspend operator fun invoke(
        signal: Signal,
        accountSize: Double
    ): PositionSize
}

class CheckRiskLimitsUseCase @Inject constructor(
    private val riskManager: RiskManager
) {
    suspend operator fun invoke(): RiskStatus
}
```

### 4. Domain Models

#### Signal
```kotlin
data class Signal(
    val id: String,
    val pair: TradingPair,
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
    val status: SignalStatus
)
```

#### Market Data
```kotlin
data class MarketData(
    val pair: TradingPair,
    val candles: List<Candle>,
    val volume: VolumeData,
    val orderBook: OrderBook?,
    val fundingRate: Double?,
    val openInterest: Double?
)
```

#### Statistics
```kotlin
data class Statistics(
    val winRate: Double,
    val profitFactor: Double,
    val maxDrawdown: Double,
    val averageRR: Double,
    val totalSignals: Int,
    val activeSignals: Int,
    val profitByPair: Map<String, Double>,
    val profitByTimeframe: Map<Timeframe, Double>
)
```

### 5. Data Layer

#### API Clients
```kotlin
interface MEXCApiClient {
    suspend fun getKlines(
        pair: String,
        interval: String,
        limit: Int
    ): List<Candle>
    
    suspend fun getTicker(pair: String): Ticker
    suspend fun getOrderBook(pair: String, depth: Int): OrderBook
    suspend fun getFundingRate(pair: String): FundingRate
}

interface NewsApiClient {
    suspend fun getLatestNews(): List<NewsItem>
}
```

#### Database
```kotlin
@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey val id: String,
    val pair: String,
    val direction: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val leverage: Double,
    val confidence: Double,
    val timeframe: String,
    val comment: String,
    val timestamp: Long,
    val status: String
)

@Dao
interface SignalDao {
    @Query("SELECT * FROM signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<SignalEntity>>
    
    @Insert
    suspend fun insertSignal(signal: SignalEntity)
    
    @Update
    suspend fun updateSignal(signal: SignalEntity)
}
```

#### Repository
```kotlin
class SignalRepositoryImpl @Inject constructor(
    private val signalDao: SignalDao,
    private val mexcApi: MEXCApiClient,
    private val signalMapper: SignalMapper
) : SignalRepository {
    
    override fun getSignals(): Flow<List<Signal>>
    override suspend fun saveSignal(signal: Signal)
    override suspend fun updateSignalStatus(
        id: String, 
        status: SignalStatus
    )
}
```

### 6. Services

#### SignalGenerator
```kotlin
class SignalGenerator @Inject constructor(
    private val technicalIndicators: TechnicalIndicators,
    private val mlModel: MLModel,
    private val marketDataProvider: MarketDataProvider
) {
    suspend fun generateSignals(marketData: MarketData): List<Signal> {
        // 1. Calculate technical indicators
        // 2. Apply ML model for prediction
        // 3. Check trading patterns
        // 4. Generate signals
        // 5. Calculate risk/reward
    }
}
```

#### RiskManager
```kotlin
class RiskManager @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val statisticsCalculator: StatisticsCalculator
) {
    fun calculatePositionSize(
        signal: Signal,
        accountSize: Double
    ): PositionSize
    
    fun checkRiskLimits(): RiskStatus
    fun adaptToDrawdown(currentDrawdown: Double)
}
```

#### BackgroundSyncService
```kotlin
class BackgroundSyncService : Service() {
    private val syncInterval = 15 * 60 * 1000L // 15 minutes
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Fetch latest market data
        // 2. Generate new signals if any
        // 3. Send notifications
        // 4. Schedule next sync
    }
}
```

#### NotificationService
```kotlin
class NotificationService @Inject constructor(
    private val context: Context
) {
    fun showSignalNotification(signal: Signal) {
        // Create notification with signal details
    }
    
    fun showNoSignalNotification() {
        // Create notification about no entry points
    }
}
```

### 7. Technical Analysis Module

#### TechnicalIndicators
```kotlin
class TechnicalIndicators {
    fun calculateSMA(data: List<Double>, period: Int): List<Double>
    fun calculateEMA(data: List<Double>, period: Int): List<Double>
    fun calculateRSI(data: List<Double>, period: Int = 14): List<Double>
    fun calculateMACD(data: List<Double>): MACDResult
    fun calculateATR(high: List<Double>, low: List<Double>, close: List<Double>): List<Double>
    fun calculateBollingerBands(data: List<Double>, period: Int = 20): BollingerBands
    fun findSupportResistance(data: List<Double>): SupportResistanceLevels
}
```

#### MLModel
```kotlin
class MLModel {
    fun predictDirection(features: DoubleArray): PredictionResult
    fun calculateConfidence(features: DoubleArray): Double
    fun updateModel(feedback: ModelFeedback)
}
```

### 8. Dependency Injection

#### AppModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMEXCApi(): MEXCApiClient
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase
    
    @Provides
    @Singleton
    fun provideSignalGenerator(): SignalGenerator
}
```

### 9. Configuration

#### AppConfig
```kotlin
object AppConfig {
    const val RISK_PER_TRADE = 0.02 // 2%
    const val MAX_DRAWDOWN = 0.20 // 20%
    const val MIN_SIGNAL_INTERVAL = 15 * 60 * 1000L // 15 minutes
    const val SYNC_INTERVAL = 5 * 60 * 1000L // 5 minutes
    
    val DEFAULT_PAIRS = listOf(
        "BTC/USDT", "ETH/USDT", "ADA/USDT", 
        "SOL/USDT", "DOT/USDT", "AVAX/USDT"
    )
}
```

### 10. Error Handling

#### Error Types
```kotlin
sealed class AppError {
    data class NetworkError(val message: String) : AppError()
    data class ApiError(val code: Int, val message: String) : AppError()
    data class DatabaseError(val message: String) : AppError()
    data class SignalGenerationError(val message: String) : AppError()
}
```

#### Error Handler
```kotlin
class ErrorHandler @Inject constructor(
    private val context: Context
) {
    fun handleError(error: AppError): String {
        return when (error) {
            is NetworkError -> context.getString(R.string.error_network)
            is ApiError -> context.getString(R.string.error_api, error.code)
            is DatabaseError -> context.getString(R.string.error_database)
            is SignalGenerationError -> context.getString(R.string.error_signal)
        }
    }
}
```

---

*Эта архитектура обеспечивает модульность, тестируемость и масштабируемость приложения.*