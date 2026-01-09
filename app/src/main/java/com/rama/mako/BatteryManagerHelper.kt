package com.rama.mako

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlin.math.abs
import kotlin.math.roundToInt

class BatteryManagerHelper(
    private val context: Context,
    private val callback: (String) -> Unit
) {
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent == null) return

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            // Temperature in Fahrenheit
            val tempF = (((intent.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE,
                -1
            ) / 10f) * 9f / 5f) + 32f).toInt()

            // Temperature label
            val tempLabel = when {
                tempF <= 113 -> ""            // normal
                tempF in 114..140 -> "Warm"
                tempF in 141..158 -> "Hot"
                else -> "Critical"
            }

            // Charging status
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val statusText = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
                else -> "Unknown"
            }

            // Charging type
            val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val chargeType = if (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            ) {
                when (chargePlug) {
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> ""
                }
            } else {
                ""
            }

            // Current in mA (may be negative if discharging)
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val currentMa =
                bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000f // µA → mA

            // Compute instantaneous power in Watts
            val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            val voltageV = voltageMv / 1000f
            val powerW = abs(voltageV * (currentMa / 1000f)) // Watts

            // Round to integer x value
            val powerX = powerW.roundToInt()

            // Build status + x
            val statusFinal = when {
                statusText == "Full" -> "Full"
                statusText == "Charging" && chargeType.isNotEmpty() -> "$statusText ($chargeType) ${powerX}x"
                statusText == "Charging" -> "$statusText ${powerX}x"
                statusText == "Discharging" -> "$statusText ${powerX}x"
                else -> statusText
            }

            if (level >= 0 && scale > 0) {
                val levelPct = (level * 100 / scale.toFloat()).toInt()
                val tempDisplay =
                    if (tempLabel.isNotEmpty()) "$tempF°F ($tempLabel)" else "$tempF°F"

                // Build info list
                val infoParts = listOf(
                    "$levelPct%",
                    tempDisplay,
                    statusFinal
                )

                val info = infoParts.joinToString(" :: ")
                callback(info)
            }
        }
    }

    fun register() =
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    fun unregister() = context.unregisterReceiver(batteryReceiver)
}
