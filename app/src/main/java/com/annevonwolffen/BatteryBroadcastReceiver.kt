package com.annevonwolffen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import java.lang.ref.WeakReference

class BatteryBroadcastReceiver(batteryListener: BatteryListener) : BroadcastReceiver() {

    private val batteryListener: WeakReference<BatteryListener> = WeakReference(batteryListener)

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TIMER_SERVICE", "BatteryBroadcastReceiver: onReceive")
        intent?.let {
            batteryListener.get()?.onBatteryLevelChanged(getBatteryLevel(it))
        }
    }

    private fun getBatteryLevel(intent: Intent): Float {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        return level / scale.toFloat()
    }

    fun interface BatteryListener {
        fun onBatteryLevelChanged(value: Float)
    }
}