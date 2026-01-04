# Crypto Signal Assistant

AI-powered Android application for generating cryptocurrency trading signals for MEXC exchange.

## Features

- **AI-Powered Signal Generation**: Uses machine learning and technical analysis to generate trading signals
- **Risk Management**: Strict 2% risk per trade, 20% maximum drawdown limits
- **Multiple Timeframes**: Supports various timeframes from 1m to 1d
- **Real-time Market Data**: Fetches live data from MEXC API
- **Comprehensive Statistics**: Track performance with detailed metrics
- **Local Data Storage**: All data stored locally for privacy and security
- **Push Notifications**: Get notified of new signals and important events

## Architecture

The app follows Clean Architecture principles with:
- **Presentation Layer**: Jetpack Compose UI with MVVM pattern
- **Domain Layer**: Business logic, use cases, and domain models
- **Data Layer**: Repositories, API clients, and local database

## Technology Stack

- **Language**: Kotlin 1.9.0+
- **UI Framework**: Jetpack Compose 1.5.0+
- **DI**: Hilt 2.47+
- **Database**: Room 2.6.0+
- **Networking**: Retrofit 2.9.0+, OkHttp 4.11.0+
- **Background Tasks**: WorkManager 2.8.1+
- **ML**: TensorFlow Lite 2.13.0+
- **Charts**: MPAndroidChart 3.1.0+

## Project Structure

```
app/src/main/kotlin/com/cryptosignal/assistant/
├── data/
│   ├── api/              # API clients and DTOs
│   ├── local/            # Local database
│   └── repository/       # Repository implementations
├── domain/
│   ├── models/           # Domain models
│   ├── repository/       # Repository interfaces
│   └── services/         # Business logic services
├── presentation/
│   ├── screens/          # UI screens
│   ├── components/       # Reusable UI components
│   ├── navigation/       # Navigation logic
│   └── theme/            # App theme and styling
├── services/             # Background services
└── di/                   # Dependency injection modules
```

## Building the App

### Prerequisites

- Android Studio Giraffe or later
- JDK 17
- Android SDK 34

### Build Instructions

1. Clone the repository:
```bash
git clone <repository-url>
cd crypto-signal-assistant
```

2. Open the project in Android Studio

3. Build the project:
```bash
./gradlew assembleDebug
```

4. Install on device:
```bash
./gradlew installDebug
```

### Release Build

To create a release build:

```bash
./gradlew assembleRelease
```

The APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

## Configuration

### Risk Parameters

The app enforces strict risk management:
- Risk per trade: 2% of account
- Maximum drawdown: 20%
- Minimum R/R ratio: 1.5
- Maximum leverage: 5x

### Trading Pairs

Default supported pairs:
- BTC/USDT
- ETH/USDT
- ADA/USDT
- SOL/USDT
- DOT/USDT
- AVAX/USDT

## API Integration

The app integrates with MEXC API for:
- Real-time market data
- Historical candlestick data
- Order book information
- Futures data (funding rate, open interest)

## Privacy & Security

- All data stored locally on device
- No personal information transmitted
- No API keys required (uses public endpoints only)
- Optional PIN/biometric protection

## Future Enhancements

- Additional exchanges (Binance, Bybit)
- Advanced ML models
- Portfolio management features
- Social trading features
- Advanced charting tools

## Disclaimer

This application is for educational and informational purposes only. Cryptocurrency trading involves substantial risk of loss. Past performance does not guarantee future results. The signals provided by this application are not financial advice. Always conduct your own research and never trade with money you cannot afford to lose.

## License

This project is proprietary software. All rights reserved.