package com.cryptosignal.assistant.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class BackgroundSyncService : Service() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    @Inject
    lateinit var syncWorkerFactory: SyncWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("BackgroundSyncService onCreate")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("BackgroundSyncService onStartCommand")
        
        serviceScope.launch {
            try {
                // Schedule periodic sync work
                schedulePeriodicSync()
                
                // Perform immediate sync
                performImmediateSync()
                
            } catch (e: Exception) {
                Timber.e(e, "Error in BackgroundSyncService")
            } finally {
                stopSelf(startId)
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("BackgroundSyncService onDestroy")
    }
    
    private suspend fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES // Sync every 15 minutes
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "crypto_sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        
        Timber.d("Periodic sync scheduled")
    }
    
    private suspend fun performImmediateSync() {
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .build()
        
        WorkManager.getInstance(applicationContext)
            .enqueue(workRequest)
        
        Timber.d("Immediate sync enqueued")
    }
}