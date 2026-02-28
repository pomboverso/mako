package com.rama.mako.managers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import com.rama.mako.R
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class BatteryManager(
    private val context: Context,
    private val callback: (String) -> Unit,
    private val prefs: SharedPreferences
) {
    private val FAHRENHEIT_COUNTRIES = setOf("US", "BS", "BZ", "KY", "PW")

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent == null) return

            // Battery level
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level < 0 || scale <= 0) return
            val levelPct = (level * 100 / scale.toFloat()).toInt()

            // Temperature (Celsius/Fahrenheit)
            val tempC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10
            val tempLabel = when {
                tempC <= 45 -> ""
                tempC in 46..60 -> context.getString(R.string.temp_warm)
                tempC in 61..70 -> context.getString(R.string.temp_hot)
                else -> context.getString(R.string.temp_critical)
            }

            val useFahrenheit = Locale.getDefault().country in FAHRENHEIT_COUNTRIES
            val (temperatureValue, unitRes) = if (useFahrenheit) {
                (tempC * 9 / 5 + 32) to R.string.unit_fahrenheit
            } else {
                tempC to R.string.unit_celsius
            }
            val unit = context.getString(unitRes)

            val tempDisplay = if (tempLabel.isNotEmpty()) {
                context.getString(R.string.battery_temp_labeled, temperatureValue, unit, tempLabel)
            } else {
                context.getString(R.string.battery_temp, temperatureValue, unit)
            }

            // Charging status
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val statusText = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.status_charging)
                BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.status_discharging)
                BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.status_full)
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.status_not_charging)
                else -> context.getString(R.string.status_unknown)
            }

            val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val chargeType = if (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            ) {
                when (chargePlug) {
                    BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.charge_usb)
                    BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.charge_ac)
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.charge_wireless)
                    else -> ""
                }
            } else ""

            // Power calculation
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val currentMa = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000f
            val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            val voltageV = voltageMv / 1000f
            val powerW = abs(voltageV * (currentMa / 1000f))
            val powerX = powerW.roundToInt()

            // Format final status
            val statusFinal = when {
                statusText == context.getString(R.string.status_full) -> statusText
                statusText == context.getString(R.string.status_charging) && chargeType.isNotEmpty() ->
                    context.getString(
                        R.string.battery_status_power_type,
                        statusText,
                        chargeType,
                        powerX
                    )

                statusText == context.getString(R.string.status_charging) ||
                        statusText == context.getString(R.string.status_discharging) ->
                    context.getString(R.string.battery_status_power, statusText, powerX)

                else -> statusText
            }

            // Final output
            val infoParts = listOf(levelPct.toString() + "%", tempDisplay, statusFinal)
            callback(infoParts.joinToString(" :: "))
        }
    }

    fun register() =
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    fun unregister() =
        context.unregisterReceiver(batteryReceiver)
}
