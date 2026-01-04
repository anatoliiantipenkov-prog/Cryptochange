# Технологический стек Crypto Signal Assistant

## 1. Основной стек разработки

### 1.1 Язык программирования
- **Kotlin 1.9.0+** - основной язык разработки
  - Современный, типобезопасный язык
  - Полная совместимость с Java
  - Отличная поддержка корутин для асинхронных операций
  - Официальный язык для Android от Google

### 1.2 UI Framework
- **Jetpack Compose 1.5.0+** - современный декларативный UI
  - Минимизирует количество кода по сравнению с XML
  - Реактивное обновление UI
  - Отличная производительность
  - Простое тестирование UI компонентов

### 1.3 Архитектура и Dependency Injection
- **Hilt 2.47+** - система внедрения зависимостей от Google
  - Упрощает работу с Dagger 2
  - Компиляция времени (без runtime overhead)
  - Отличная интеграция с Android компонентами

### 1.4 Асинхронное программирование
- **Kotlin Coroutines 1.7.3+**
  - Легковесные потоки
  - Отличная читаемость асинхронного кода
  - Структурированное concurrency
  - Интеграция с Flow для реактивных потоков

## 2. Работа с данными

### 2.1 Локальная база данных
- **Room 2.6.0+** - абстракция над SQLite
  - Compile-time проверка SQL запросов
  - Миграции схемы
  - Интеграция с Kotlin Coroutines
  - Поддержка Flow для реактивных обновлений

### 2.2 Сетевой слой
- **Retrofit 2.9.0+** - HTTP клиент
  - Типобезопасные API вызовы
  - Интеграция с корутинами
  - Поддержка различных конвертеров (Gson, Moshi)

- **OkHttp 4.11.0+** - низкоуровневый HTTP клиент
  - Эффективное соединение
  - Кэширование ответов
  - Интерцепторы для логирования
  - Поддержка WebSocket

### 2.3 Сериализация
- **Kotlinx Serialization 1.6.0+**
  - Нативная поддержка Kotlin
  - Производительность (compile-time codegen)
  - Поддержка различных форматов (JSON, ProtoBuf)

## 3. Фоновые процессы

### 3.1 Фоновая синхронизация
- **WorkManager 2.8.1+** - универсальная система фоновых задач
  - Гарантированное выполнение задач
  - Поддержка ограничений (WiFi, зарядка)
  - Периодические и одноразовые задачи
  - Совместимость с разными версиями Android

### 3.2 Уведомления
- **NotificationCompat** - система уведомлений
  - Совместимость с разными версиями Android
  - Каналы уведомлений (Android 8.0+)
  - Расширенные уведомления с действиями

## 4. AI/ML компоненты

### 4.1 Локальные ML модели
- **TensorFlow Lite 2.13.0+** - мобильная ML платформа
  - Выполнение моделей на устройстве
  - GPU делегирование для ускорения
  - Квантование моделей для уменьшения размера
  - Поддержка различных типов моделей

### 4.2 Математические вычисления
- **EJML (Efficient Java Matrix Library)** - линейная алгебра
  - Эффективные матричные операции
  - Подходит для технических индикаторов
  - Минимальный размер библиотеки

### 4.3 Альтернатива: собственная реализация
- Для простых моделей можно использовать собственную реализацию
- Линейная регрессия, деревья решений
- Минимизация зависимостей
- Более контроля над оптимизацией

## 5. Графика и визуализация

### 5.1 Графики
- **MPAndroidChart 3.1.0+** - библиотека для графиков
  - Широкий выбор типов графиков
  - Анимации и взаимодействие
  - Поддержка realtime данных
  - Кастомизация внешнего вида

### 5.2 Compose Charts (альтернатива)
- **Compose Charts** - нативные графики для Compose
  - Более легковесные
  - Нативная интеграция с Compose
  - Меньше зависимостей

## 6. Логирование и отладка

### 6.1 Логирование
- **Timber 5.0.1+** - логирование
  - Удобный API
  - Автоматическое определение тегов
  - Поддержка различных уровней логов
  - Легко настраиваемый

### 6.2 Crash Reporting
- **ACRA 5.11.0+** - локальный отчет о сбоях
  - Локальное хранение отчетов
  - Без передачи данных третьим лицам
  - Настраиваемая отправка

## 7. Тестирование

### 7.1 Unit тесты
- **JUnit 5.10.0+** - фреймворк для тестирования
- **MockK 1.13.7+** - мокинг для Kotlin
- **Truth 1.1.5+** - удобные assertions

### 7.2 UI тесты
- **Espresso 3.5.1+** - UI тестирование
- **Compose Test** - тестирование Compose UI
- **Robolectric 4.10.3+** - тестирование в JVM

## 8. Инструменты разработки

### 8.1 IDE
- **Android Studio Giraffe+** - официальная IDE
  - Полная поддержка Kotlin и Compose
  - Отличный дебаггер
  - Profiler для анализа производительности
  - Device Manager для тестирования

### 8.2 Система сборки
- **Gradle 8.0+** - система сборки
  - Kotlin DSL для build scripts
  - Dependency management
  - Product flavors для разных версий

### 8.3 CI/CD (опционально)
- **GitHub Actions** или **GitLab CI**
- Автоматическая сборка APK
- Запуск тестов
- Статический анализ кода

## 9. Минимальные требования

### 9.1 Android версия
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

### 9.2 Размер приложения
- **APK размер**: < 25 MB
- **Установленное приложение**: < 50 MB
- **База данных**: динамически растет (до 100 MB)

## 10. Зависимости build.gradle (Module)

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'
    id 'dagger.hilt.android.plugin'
    id 'kotlinx-serialization'
}

dependencies {
    // Core
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    
    // Compose
    implementation 'androidx.activity:activity-compose:1.8.0'
    implementation platform('androidx.compose:compose-bom:2023.10.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    
    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
    
    // DI
    implementation 'com.google.dagger:hilt-android:2.47'
    kapt 'com.google.dagger:hilt-compiler:2.47'
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
    
    // Networking
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
    
    // Database
    implementation 'androidx.room:room-runtime:2.6.0'
    implementation 'androidx.room:room-ktx:2.6.0'
    kapt 'androidx.room:room-compiler:2.6.0'
    
    // Background tasks
    implementation 'androidx.work:work-runtime-ktx:2.8.1'
    
    // ML
    implementation 'org.tensorflow:tensorflow-lite:2.13.0'
    implementation 'org.tensorflow:tensorflow-lite-gpu:2.13.0'
    implementation 'org.tensorflow:tensorflow-lite-support:0.4.3'
    
    // Charts
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
    
    // Logging
    implementation 'com.jakewharton.timber:timber:5.0.1'
    
    // Serialization
    implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0'
    
    // Math
    implementation 'org.ejml:ejml-all:0.43'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'io.mockk:mockk:1.13.7'
    testImplementation 'com.google.truth:truth:1.1.5'
    
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation platform('androidx.compose:compose-bom:2023.10.00')
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
    
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
}
```

## 11. Преимущества выбранного стека

### 11.1 Современность
- Использует последние технологии Google
- Jetpack Compose - будущее Android разработки
- Kotlin - официальный язык Android

### 11.2 Производительность
- Compile-time DI (Hilt)
- Эффективные корутины
- Локальные вычисления

### 11.3 Масштабируемость
- Модульная архитектура
- Четкое разделение ответственности
- Легко добавлять новые функции

### 11.4 Тестируемость
- DI упрощает тестирование
- Чистая архитектура
- Мокинг зависимостей

### 11.5 Поддержка сообщества
- Большое сообщество Kotlin/Android
- Хорошая документация
- Множество примеров и туториалов

## 12. Альтернативные решения

### 12.1 Flutter/Dart
**Плюсы:**
- Кроссплатформенность
- Единый кодбейз

**Минусы:**
- Меньше нативных библиотек
- Больше размер APK
- Сложнее с ML

### 12.2 React Native
**Плюсы:**
- JavaScript экосистема
- Быстрая разработка

**Минусы:**
- Производительность
- Сложности с ML
- Меньше контроля

### 12.3 Native (C++)
**Плюсы:**
- Максимальная производительность
- Полный контроль

**Минусы:**
- Сложная разработка
- Долгая разработка
- Сложность с UI

**Вывод:** Выбранный стек оптимален для задачи, обеспечивая баланс между производительностью, скоростью разработки и поддержкой современных технологий.