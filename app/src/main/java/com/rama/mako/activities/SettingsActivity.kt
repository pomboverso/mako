package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import com.rama.mako.BaseFullscreenActivity
import com.rama.mako.R
import com.rama.mako.widgets.WdCheckbox

class SettingsActivity : BaseFullscreenActivity() {

    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_settings)

        val root = findViewById<View>(android.R.id.content)
        applyEdgeToEdgePadding(root)

        // Setup buttons
        setupButton(R.id.about_button) { startActivity(Intent(this, AboutActivity::class.java)) }
        setupButton(R.id.close_button) { finish() }

        setupButton(R.id.activate_button) {
            openIntent(Intent(Settings.ACTION_HOME_SETTINGS), "Unable to open launcher settings")
        }
        setupButton(R.id.wallpaper_button) {
            openIntent(Intent(Intent.ACTION_SET_WALLPAPER), "No wallpaper app available")
        }

        findViewById<View>(R.id.reset_button).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.change_apps_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }

        // Clock radio buttons
        val clockFormatGroup = findViewById<RadioGroup>(R.id.clock_format_group)

        // Restore saved state
        val showClock = prefs.getBoolean("show_clock", true)
        val clockFormat = prefs.getString("clock_format", "system")

        when {
            !showClock -> clockFormatGroup.check(R.id.clock_none)
            clockFormat == "24" -> clockFormatGroup.check(R.id.clock_24)
            clockFormat == "12" -> clockFormatGroup.check(R.id.clock_12)
            else -> clockFormatGroup.check(R.id.clock_system)
        }

        // Update prefs when user selects a radio button
        clockFormatGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.clock_none -> prefs.edit().putBoolean("show_clock", false)
                    .remove("clock_format").apply()

                R.id.clock_system -> prefs.edit().putBoolean("show_clock", true)
                    .putString("clock_format", "system").apply()

                R.id.clock_24 -> prefs.edit().putBoolean("show_clock", true)
                    .putString("clock_format", "24").apply()

                R.id.clock_12 -> prefs.edit().putBoolean("show_clock", true)
                    .putString("clock_format", "12").apply()
            }
        }

        // Checkboxes (without charge status)
        bindWdCheckbox(R.id.show_date, "show_date", false, dependentViewId = R.id.show_year_day)
        bindWdCheckbox(R.id.show_year_day, "show_year_day", false)
        bindWdCheckbox(R.id.show_battery, "show_battery", false)
    }

    // Helper to bind a checkbox to SharedPreferences
    private fun bindWdCheckbox(
        wdCheckboxId: Int,
        prefKey: String,
        defaultValue: Boolean,
        dependentViewId: Int? = null
    ) {
        val wdCheckbox = findViewById<WdCheckbox>(wdCheckboxId)
        val dependentView = dependentViewId?.let { findViewById<View>(it) }

        // Initialize state
        val isChecked = prefs.getBoolean(prefKey, defaultValue)
        wdCheckbox.setChecked(isChecked)
        dependentView?.visibility = if (isChecked) View.VISIBLE else View.GONE

        wdCheckbox.setOnCheckedChangeListener { checked ->
            prefs.edit().putBoolean(prefKey, checked).apply()
            dependentView?.visibility = if (checked) View.VISIBLE else View.GONE
        }
    }

    // Helper to bind a click listener
    private fun setupButton(id: Int, action: () -> Unit) {
        findViewById<View>(id).setOnClickListener { action() }
    }

    // Safely open an intent
    private fun openIntent(intent: Intent, errorMsg: String) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
