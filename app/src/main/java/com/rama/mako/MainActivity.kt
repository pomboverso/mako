package com.rama.mako

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var batteryText: TextView
    private lateinit var listView: ListView
    private lateinit var clockManager: ClockManager
    private lateinit var batteryHelper: BatteryManagerHelper

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Load saved theme BEFORE super.onCreate
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        super.onCreate(savedInstanceState)

        // Allow layout behind system bars (no AndroidX)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Inflate layout FIRST
        setContentView(R.layout.view_home)

        // Root view for safe-area padding
        val root = findViewById<View>(R.id.root)

        root.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemWindowInsetLeft + dp(32),
                insets.systemWindowInsetTop + dp(32),
                insets.systemWindowInsetRight + dp(32),
                insets.systemWindowInsetBottom + dp(32)
            )
            insets
        }

        // Find views
        timeText = findViewById(R.id.time)
        dateText = findViewById(R.id.date)
        batteryText = findViewById(R.id.battery)
        listView = findViewById(R.id.appList)

        // Clock
        clockManager = ClockManager(timeText, dateText)
        clockManager.start()

        // Battery
        batteryHelper = BatteryManagerHelper(this) { status ->
            batteryText.text = status
        }
        batteryHelper.register()

        // App list
        AppListHelper(this, listView).setup()

        // Open system clock
        timeText.setOnClickListener {

            val pm = packageManager

            val intents = listOf(
                // Standard (rarely works, but try first)
                Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_CLOCK"),

                // AOSP / many OEM clocks
                Intent("android.intent.action.SHOW_ALARMS"),

                // Last resort: let user choose
                Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.APP_ALARM")
            )

            for (intent in intents) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent)
                    return@setOnClickListener
                }
            }

            Toast.makeText(this, "No clock app found", Toast.LENGTH_SHORT).show()
        }

        // Settings button
        findViewById<View>(R.id.settings_button).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryHelper.unregister()
    }
}
