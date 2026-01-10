package com.rama.mako

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class BatteryManagerHelper(
    private val context: Context,
    private val callback: (String) -> Unit
) {

    private val FAHRENHEIT_COUNTRIES =
        setOf("US", "BS", "BZ", "KY", "PW")
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent == null) return

            // ─────────────────────────────────────
            // Battery level
            // ─────────────────────────────────────
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            // ─────────────────────────────────────
            // Temperature (base unit: Celsius)
            // ─────────────────────────────────────
            val tempC =
                intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10

            val tempLabel = when {
                tempC <= 45 -> "" // normal
                tempC in 46..60 -> context.getString(R.string.temp_warm)
                tempC in 61..70 -> context.getString(R.string.temp_hot)
                else -> context.getString(R.string.temp_critical)
            }

            val useFahrenheit =
                Locale.getDefault().country in FAHRENHEIT_COUNTRIES

            val temperatureValue: Int
            val temperatureUnitRes: Int

            if (useFahrenheit) {
                temperatureValue = (tempC * 9 / 5) + 32
                temperatureUnitRes = R.string.unit_fahrenheit
            } else {
                temperatureValue = tempC
                temperatureUnitRes = R.string.unit_celsius
            }

            val unit = context.getString(temperatureUnitRes)

            val tempDisplay =
                if (tempLabel.isNotEmpty()) {
                    context.getString(
                        R.string.battery_temp_labeled,
                        temperatureValue,
                        unit,
                        tempLabel
                    )
                } else {
                    context.getString(
                        R.string.battery_temp,
                        temperatureValue,
                        unit
                    )
                }

            // ─────────────────────────────────────
            // Charging status
            // ─────────────────────────────────────
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val statusText = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING ->
                    context.getString(R.string.status_charging)

                BatteryManager.BATTERY_STATUS_DISCHARGING ->
                    context.getString(R.string.status_discharging)

                BatteryManager.BATTERY_STATUS_FULL ->
                    context.getString(R.string.status_full)

                BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                    context.getString(R.string.status_not_charging)

                else ->
                    context.getString(R.string.status_unknown)
            }

            // ─────────────────────────────────────
            // Charging type
            // ─────────────────────────────────────
            val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val chargeType =
                if (
                    status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
                ) {
                    when (chargePlug) {
                        BatteryManager.BATTERY_PLUGGED_USB ->
                            context.getString(R.string.charge_usb)

                        BatteryManager.BATTERY_PLUGGED_AC ->
                            context.getString(R.string.charge_ac)

                        BatteryManager.BATTERY_PLUGGED_WIRELESS ->
                            context.getString(R.string.charge_wireless)

                        else -> ""
                    }
                } else ""

            // ─────────────────────────────────────
            // Power calculation
            // ─────────────────────────────────────
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val currentMa =
                bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000f // µA → mA

            val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            val voltageV = voltageMv / 1000f

            val powerW = abs(voltageV * (currentMa / 1000f))
            val powerX = powerW.roundToInt()

            // ─────────────────────────────────────
            // Status + power formatting
            // ─────────────────────────────────────
            val statusFinal = when {
                statusText == context.getString(R.string.status_full) ->
                    statusText

                statusText == context.getString(R.string.status_charging) &&
                        chargeType.isNotEmpty() ->
                    context.getString(
                        R.string.battery_status_power_type,
                        statusText,
                        chargeType,
                        powerX
                    )

                statusText == context.getString(R.string.status_charging) ||
                        statusText == context.getString(R.string.status_discharging) ->
                    context.getString(
                        R.string.battery_status_power,
                        statusText,
                        powerX
                    )

                else -> statusText
            }

            // ─────────────────────────────────────
            // Final output
            // ─────────────────────────────────────
            if (level >= 0 && scale > 0) {
                val levelPct = (level * 100 / scale.toFloat()).toInt()

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
        context.registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

    fun unregister() =
        context.unregisterReceiver(batteryReceiver)
}
