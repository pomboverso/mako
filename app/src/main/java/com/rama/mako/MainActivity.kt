package com.rama.mako

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.content.Intent

class MainActivity : Activity() {
    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var batteryText: TextView
    private lateinit var listView: ListView

    private lateinit var clockManager: ClockManager
    private lateinit var batteryHelper: BatteryManagerHelper
    private lateinit var appListHelper: AppListHelper

    private val settingsPrefs by lazy {
        getSharedPreferences("settings", MODE_PRIVATE)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        setContentView(R.layout.view_home)

        setContentView(R.layout.view_home)

        val root = findViewById<View>(R.id.root)
        root.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemWindowInsetLeft + dp(16),
                insets.systemWindowInsetTop + dp(16),
                insets.systemWindowInsetRight + dp(16),
                insets.systemWindowInsetBottom + dp(16)
            )
            insets
        }

        // Views
        timeText = findViewById(R.id.time)
        dateText = findViewById(R.id.date)
        batteryText = findViewById(R.id.battery)
        listView = findViewById(R.id.appList)

        // Clock
        clockManager = ClockManager(timeText, dateText, prefs)
        clockManager.start()
        timeText.setOnClickListener { openSystemClock() }

        // Battery
        batteryHelper = BatteryManagerHelper(this) { status ->
            batteryText.text = status
        }
        batteryHelper.register()

        // App List
        appListHelper = AppListHelper(this, listView)
        appListHelper.setup()

    }

    override fun onResume() {
        super.onResume()
        syncSettings()
        appListHelper.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryHelper.unregister()
        clockManager.stop()
    }

    private fun syncSettings() {
        val showClock = settingsPrefs.getBoolean("show_clock", true)
        val showDate = settingsPrefs.getBoolean("show_date", true)
        val showBattery = settingsPrefs.getBoolean("show_battery", true)

        timeText.visibility = if (showClock) View.VISIBLE else View.GONE
        findViewById<View>(R.id.date_row).visibility = if (showDate) View.VISIBLE else View.GONE
        findViewById<View>(R.id.battery_row).visibility =
            if (showBattery) View.VISIBLE else View.GONE
    }

    private fun openSystemClock() {
        val pm = packageManager
        val intents = listOf(
            Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CLOCK"),
            Intent("android.intent.action.SHOW_ALARMS"),
            Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_ALARM")
        )

        for (intent in intents) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent)
                return
            }
        }

        Toast.makeText(this, "No clock app found", Toast.LENGTH_SHORT).show()
    }
}
