package com.cryptosignal.assistant.di

import android.content.Context
import androidx.room.Room
import com.cryptosignal.assistant.BuildConfig
import com.cryptosignal.assistant.data.api.MEXCApiClient
import com.cryptosignal.assistant.data.local.database.AppDatabase
import com.cryptosignal.assistant.data.repository.SignalRepositoryImpl
import com.cryptosignal.assistant.domain.repository.SignalRepository
import com.cryptosignal.assistant.domain.services.SignalGenerator
import com.cryptosignal.assistant.domain.services.TechnicalIndicators
import com.cryptosignal.assistant.domain.services.managers.RiskManager
import com.cryptosignal.assistant.domain.services.managers.MLModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "crypto_signal_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideSignalDao(database: AppDatabase) = database.signalDao()
    
    @Provides
    @Singleton
    fun provideSignalRepository(signalDao: SignalDao): SignalRepository {
        return SignalRepositoryImpl(signalDao)
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Timber.tag("API").d(message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        
        return builder.build()
    }
    
    @Provides
    @Singleton
    fun provideMEXCApiClient(okHttpClient: OkHttpClient): MEXCApiClient {
        return Retrofit.Builder()
            .baseUrl("https://api.mexc.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MEXCApiClient::class.java)
    }
    
    @Provides
    @Singleton
    fun provideTechnicalIndicators(): TechnicalIndicators {
        return TechnicalIndicators()
    }
    
    @Provides
    @Singleton
    fun provideMLModel(): MLModel {
        return MLModel()
    }
    
    @Provides
    @Singleton
    fun provideRiskManager(signalRepository: SignalRepository): RiskManager {
        return RiskManager(signalRepository)
    }
    
    @Provides
    @Singleton
    fun provideSignalGenerator(
        technicalIndicators: TechnicalIndicators,
        riskManager: RiskManager,
        mlModel: MLModel
    ): SignalGenerator {
        return SignalGenerator(technicalIndicators, riskManager, mlModel)
    }
}