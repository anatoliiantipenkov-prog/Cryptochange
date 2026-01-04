package com.cryptosignal.assistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CryptoSignalApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        
        Timber.d("CryptoSignalApp initialized")
    }
    
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // In release builds, we might want to send logs to a crash reporting service
            // For now, just log errors
            if (priority >= Timber.ERROR) {
                // Could send to Crashlytics or similar service
            }
        }
    }
}