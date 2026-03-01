package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.rama.mako.CsActivity
import com.rama.mako.R
import com.rama.mako.managers.AppListManager
import com.rama.mako.managers.BatteryManager
import com.rama.mako.managers.ClockManager
import com.rama.mako.managers.PrefsManager

class MainActivity : CsActivity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var batteryText: TextView
    private lateinit var listView: ListView

    private lateinit var clockManager: ClockManager
    private lateinit var batteryManager: BatteryManager
    private lateinit var appListManager: AppListManager

    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_home)

        val root = findViewById<View>(R.id.root)
        applyEdgeToEdgePadding(root)

        // --- Prefs ---
        prefs = PrefsManager.getInstance(this)

        // --- Views ---
        timeText = findViewById(R.id.time)
        dateText = findViewById(R.id.date)
        batteryText = findViewById(R.id.battery)
        listView = findViewById(R.id.app_list)

        // --- Clock ---
        clockManager = ClockManager(timeText, dateText, this) // now uses PrefsManager internally
        clockManager.start()
        timeText.setOnClickListener { openSystemClock() }

        // --- Battery ---
        batteryManager = BatteryManager(
            context = this,
            callback = { status -> batteryText.text = status },
        )
        batteryManager.register()

        // --- App List ---
        appListManager = AppListManager(this, listView)
        appListManager.setup()

        val emptySpaceDrawer = findViewById<View>(R.id.empty_space_drawer)
        emptySpaceDrawer.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
    }

    override fun onResume() {
        super.onResume()
        syncSettings()
        appListManager.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryManager.unregister()
        clockManager.stop()
    }

    // --- Settings sync (row visibility only) ---
    private fun syncSettings() {
        timeText.visibility = if (prefs.isClockVisible()) View.VISIBLE else View.GONE
        findViewById<View>(R.id.date_row).visibility =
            if (prefs.getBoolean("show_date", true)) View.VISIBLE else View.GONE
        findViewById<View>(R.id.battery_row).visibility =
            if (prefs.getBoolean("show_battery", true)) View.VISIBLE else View.GONE
    }

    // --- Open system clock safely ---
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