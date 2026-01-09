package com.rama.mako

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import android.widget.Switch

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.view_settings)

        // Adjust for status bar
        findViewById<View>(android.R.id.content).setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(
                v.paddingLeft,
                insets.systemWindowInsetTop,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        // Setup buttons using helper method
        setupButton(R.id.about_button) { startActivity(Intent(this, AboutActivity::class.java)) }
        setupButton(R.id.close_button) { finish() }

        setupButton(R.id.activate_button) {
            openIntent(Intent(Settings.ACTION_HOME_SETTINGS), "Unable to open launcher settings")
        }
        setupButton(R.id.wallpaper_button) {
            openIntent(
                Intent(Intent.ACTION_SET_WALLPAPER),
                "No wallpaper app available"
            )
        }

        // Reset App
        findViewById<View>(R.id.reset_button).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            }
            startActivity(intent)
        }

        // Edit Apps
        findViewById<View>(R.id.change_apps_button).setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)
            startActivity(intent)
        }

        // Toggle Clock
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val toggleClock = findViewById<Switch>(R.id.toggle_clock)

        toggleClock.isChecked = prefs.getBoolean("show_clock", true)

        toggleClock.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit()
                .putBoolean("show_clock", isChecked)
                .apply()
        }

        // Toggle Date
        val toggleDate = findViewById<Switch>(R.id.toggle_date)
        toggleDate.isChecked = prefs.getBoolean("show_date", true)

        toggleDate.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit()
                .putBoolean("show_date", isChecked)
                .apply()
        }

        // Toggle Battery
        val toggleBattery = findViewById<Switch>(R.id.toggle_battery)
        toggleBattery.isChecked = prefs.getBoolean("show_battery", true)

        toggleBattery.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit()
                .putBoolean("show_battery", isChecked)
                .apply()
        }
    }

    // Helper to bind a click listener
    private fun setupButton(id: Int, action: () -> Unit) {
        findViewById<View>(id).setOnClickListener { action() }
    }

    // Helper to start an intent safely
    private fun openIntent(intent: Intent, errorMsg: String) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
