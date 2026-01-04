package com.cryptosignal.assistant.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cryptosignal.assistant.R
import com.cryptosignal.assistant.domain.models.Direction
import com.cryptosignal.assistant.domain.models.RiskStatus
import com.cryptosignal.assistant.domain.models.Signal
import com.cryptosignal.assistant.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val CHANNEL_ID = "crypto_signals_channel"
        private const val CHANNEL_NAME = "Crypto Signals"
        private const val CHANNEL_DESCRIPTION = "Notifications for crypto trading signals"
        private const val NOTIFICATION_ID_SIGNAL = 1001
        private const val NOTIFICATION_ID_NO_SIGNAL = 1002
        private const val NOTIFICATION_ID_RISK_LIMIT = 1003
        private const val NOTIFICATION_ID_TP_SL = 1004
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showSignalNotification(signal: Signal) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val directionIcon = when (signal.direction) {
                Direction.LONG -> "📈"
                Direction.SHORT -> "📉"
            }
            
            val directionText = when (signal.direction) {
                Direction.LONG -> "LONG"
                Direction.SHORT -> "SHORT"
            }
            
            val color = when (signal.direction) {
                Direction.LONG -> android.graphics.Color.GREEN
                Direction.SHORT -> android.graphics.Color.RED
            }
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // You need to create this icon
                .setContentTitle("$directionIcon $directionText ${signal.pair}")
                .setContentText("Вход: ${String.format("%.2f", signal.entryPrice)} | SL: ${String.format("%.2f", signal.stopLoss)} | TP: ${String.format("%.2f", signal.takeProfit)}")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Вход: ${String.format("%.2f", signal.entryPrice)}\nSL: ${String.format("%.2f", signal.stopLoss)}\nTP: ${String.format("%.2f", signal.takeProfit)}\nR/R: ${String.format("%.1f", signal.riskReward)}\nПлечо: ${String.format("%.1f", signal.leverage)}x\n\n${signal.comment}"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(color)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .build()
            
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID_SIGNAL, notification)
            }
            
            Timber.d("Signal notification shown for ${signal.pair}")
            
        } catch (e: Exception) {
            Timber.e(e, "Error showing signal notification")
        }
    }
    
    fun showNoSignalNotification() {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("📊 Сигнал от агента")
                .setContentText("На рынке сейчас нет адекватной точки входа")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("На рынке сейчас нет адекватной точки входа. Агент продолжает анализ и пришлет сигнал при появлении хороших условий."))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(android.graphics.Color.BLUE)
                .build()
            
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID_NO_SIGNAL, notification)
            }
            
            Timber.d("No signal notification shown")
            
        } catch (e: Exception) {
            Timber.e(e, "Error showing no signal notification")
        }
    }
    
    fun showRiskLimitNotification(riskStatus: RiskStatus) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "settings")
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⚠️ Превышен лимит риска")
                .setContentText("Текущая просадка: ${String.format("%.1f", riskStatus.currentDrawdown * 100)}%")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Текущая просадка: ${String.format("%.1f", riskStatus.currentDrawdown * 100)}%\nМаксимально допустимая: 20%\n\nПриложение временно приостановило генерацию сигналов. Проверьте настройки риска."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setColor(android.graphics.Color.YELLOW)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .build()
            
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID_RISK_LIMIT, notification)
            }
            
            Timber.d("Risk limit notification shown")
            
        } catch (e: Exception) {
            Timber.e(e, "Error showing risk limit notification")
        }
    }
    
    fun showTPSLReachedNotification(signal: Signal, isTP: Boolean) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val (title, content, color) = if (isTP) {
                Triple(
                    "✅ Take Profit достигнут",
                    "${signal.pair} ${if (signal.isLong()) "LONG" else "SHORT"}: +${String.format("%.1f", signal.outcome?.pnlR ?: 0.0)}R",
                    android.graphics.Color.GREEN
                )
            } else {
                Triple(
                    "❌ Stop Loss достигнут",
                    "${signal.pair} ${if (signal.isLong()) "LONG" else "SHORT"}: ${String.format("%.1f", signal.outcome?.pnlR ?: 0.0)}R",
                    android.graphics.Color.RED
                )
            }
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(color)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .build()
            
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID_TP_SL, notification)
            }
            
            Timber.d("TP/SL notification shown for ${signal.pair}")
            
        } catch (e: Exception) {
            Timber.e(e, "Error showing TP/SL notification")
        }
    }
    
    fun cancelAllNotifications() {
        try {
            with(NotificationManagerCompat.from(context)) {
                cancel(NOTIFICATION_ID_SIGNAL)
                cancel(NOTIFICATION_ID_NO_SIGNAL)
                cancel(NOTIFICATION_ID_RISK_LIMIT)
                cancel(NOTIFICATION_ID_TP_SL)
            }
            Timber.d("All notifications cancelled")
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling notifications")
        }
    }
}