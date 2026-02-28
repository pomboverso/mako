package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.rama.mako.managers.AppListManager
import com.rama.mako.CsActivity
import com.rama.mako.managers.BatteryManager
import com.rama.mako.managers.ClockManager
import com.rama.mako.R

class MainActivity : CsActivity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var batteryText: TextView
    private lateinit var listView: ListView

    private lateinit var clockManager: ClockManager
    private lateinit var batteryHelper: BatteryManager
    private lateinit var appListManager: AppListManager

    private val prefs by lazy {
        getSharedPreferences("settings", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_home)

        val root = findViewById<View>(R.id.root)
        applyEdgeToEdgePadding(root)

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
        batteryHelper = BatteryManager(
            context = this,
            callback = { status -> batteryText.text = status },
            prefs = prefs
        )
        batteryHelper.register()


        // App List
        appListManager = AppListManager(this, listView)
        appListManager.setup()
    }

    override fun onResume() {
        super.onResume()
        syncSettings()
        appListManager.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryHelper.unregister()
        clockManager.stop()
    }


    // Settings sync (row visibility only)
    private fun syncSettings() {
        val showClock = prefs.getBoolean("show_clock", true)
        val showDate = prefs.getBoolean("show_date", true)
        val showBattery = prefs.getBoolean("show_battery", true)

        timeText.visibility = if (showClock) View.VISIBLE else View.GONE
        findViewById<View>(R.id.date_row).visibility = if (showDate) View.VISIBLE else View.GONE
        findViewById<View>(R.id.battery_row).visibility =
            if (showBattery) View.VISIBLE else View.GONE
    }


    // Open system clock safely
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

        Toast.makeText(this, getString(R.string.no_clock_app_found_label), Toast.LENGTH_SHORT)
            .show()
    }
}
