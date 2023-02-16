package com.annevonwolffen

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Timer
import java.util.TimerTask

class TimerService : Service() {

    private lateinit var timer: Timer

    private var timeElapsed: Int = 0

    private var timeElapsedListener: TimerTickListener? = null

    private val binder = TimerServiceBinder()

    fun setTimerTickListener(listener: TimerTickListener) {
        timeElapsedListener = listener
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TIMER_SERVICE", "TimerService: onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TIMER_SERVICE", "TimerService: onStartCommand")

        when (intent?.action) {
            START_TIMER_ACTION -> {
                startTimer()
            }
            STOP_TIMER_ACTION -> {
                stopTimer()
            }
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("TIMER_SERVICE", "TimerService: onBind")

        return binder
    }

    private fun createNotification(millis: Int? = null): Notification {
        val notificationChannelId = "service_channel"
        // create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel =
                NotificationChannel(
                    notificationChannelId, "Service Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
            notificationChannel.enableLights(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val intentStartTimer = Intent(this, TimerService::class.java)
        intentStartTimer.action = START_TIMER_ACTION
        val pendingIntentStartTimer = PendingIntent.getService(this, 0, intentStartTimer, PendingIntent.FLAG_IMMUTABLE)

        val intentStopTimer = Intent(this, TimerService::class.java)
        intentStopTimer.action = STOP_TIMER_ACTION
        val pendingIntentStopTimer = PendingIntent.getService(this, 0, intentStopTimer, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Timer Service")
            .setContentText(millis?.toString().orEmpty())
            .setSmallIcon(R.drawable.timer_24)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .addAction(R.drawable.start_24, "Start timer", pendingIntentStartTimer)
            .addAction(R.drawable.stop_24, "Stop timer", pendingIntentStopTimer)
            .build()
    }

    private fun updateNotification(millis: Int) {
        val notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(NOTIFICATION_ID, createNotification(millis))
            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, createNotification(millis))
        }
    }

    private fun startTimer() {
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                timeElapsed++
                updateNotification(timeElapsed)
                timeElapsedListener?.onTick(timeElapsed)
            }
        }, 0, 1000)
    }

    private fun stopTimer() {
        timer.cancel()
    }

    override fun onDestroy() {
        Log.d("TIMER_SERVICE", "TimerService: onDestroy")
        super.onDestroy()
    }

    inner class TimerServiceBinder : Binder() {
        fun setTimerTickListener(listener: TimerTickListener) {
            this@TimerService.setTimerTickListener(listener)
        }
    }

    fun interface TimerTickListener {
        fun onTick(timeElapsed: Int)
    }

    private companion object {
        const val NOTIFICATION_ID = 1
        const val START_TIMER_ACTION = "START_TIMER_ACTION"
        const val STOP_TIMER_ACTION = "STOP_TIMER_ACTION"
    }
}