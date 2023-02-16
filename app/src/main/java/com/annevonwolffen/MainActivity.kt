package com.annevonwolffen

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startService()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please grant Notification permission from App Settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("TIMER_SERVICE", "MainActivity: onServiceConnected")
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TimerService.TimerServiceBinder
            binder.setTimerTickListener { timeElapsed ->
                Log.d("TIMER_SERVICE", "MainActivity: onTick $timeElapsed")
                runOnUiThread {
                    findViewById<TextView>(R.id.timer_value).text = timeElapsed.toString()
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("TIMER_SERVICE", "MainActivity: onServiceDisconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start_service).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startService()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                startService()
            }
        }
    }

    private fun startService() {
        startService(Intent(this, TimerService::class.java))
        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        unbindService(connection)
        super.onStop()
    }
}