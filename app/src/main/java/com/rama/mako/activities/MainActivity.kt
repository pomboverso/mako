package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import android.widget.TextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.rama.mako.CsActivity
import com.rama.mako.R
import com.rama.mako.managers.AppListManager
import com.rama.mako.managers.AppsProvider
import com.rama.mako.managers.BatteryManager
import com.rama.mako.managers.ClockManager
import com.rama.mako.managers.FontManager
import com.rama.mako.managers.PrefsManager
import com.rama.mako.widgets.WdButton

class MainActivity : CsActivity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var batteryText: TextView
    private lateinit var listView: ListView

    private lateinit var clockManager: ClockManager
    private lateinit var batteryManager: BatteryManager
    private lateinit var appListManager: AppListManager
    private lateinit var appsProvider: AppsProvider

    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PrefsManager.getInstance(this).initPrefs()
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
        appsProvider = AppsProvider(this)
        appListManager = AppListManager(this, listView, AppsProvider(this))
        appListManager.setup()

        val appLayout = findViewById<LinearLayout>(R.id.apps_layout)
        appLayout.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        initSearchbar()
    }

    private var currentSearchQuery: String = ""

    private fun initSearchbar() {
        val searchField = findViewById<EditText>(R.id.search_field)
        val clearBtn = findViewById<FrameLayout>(R.id.clear_field)

        // Load previous query
        searchField.setText(currentSearchQuery)
        searchField.setSelection(currentSearchQuery.length)
        appListManager.filter(currentSearchQuery)

        // Update the query as user types
        searchField.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                appListManager.filter(currentSearchQuery)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Clear button clears field + query
        clearBtn.setOnClickListener {
            currentSearchQuery = ""
            searchField.text.clear()
            appListManager.filter("")
        }
    }

    override fun onResume() {
        super.onResume()
        syncSettings()
        appListManager.refresh()
        batteryManager.forceUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryManager.unregister()
        clockManager.stop()
    }

    // --- Settings sync (row visibility only) ---
    private fun syncSettings() {
        timeText.visibility =
            if (prefs.getClockFormat() != PrefsManager.ClockFormat.NONE) View.VISIBLE else View.GONE
        findViewById<View>(R.id.date_row).visibility =
            if (prefs.isDateVisible()) View.VISIBLE else View.GONE
        findViewById<View>(R.id.battery_row).visibility =
            if (prefs.isBatteryVisible()) View.VISIBLE else View.GONE
        findViewById<View>(R.id.searchbar).visibility =
            if (prefs.isSearchVisible()) View.VISIBLE else View.GONE
    }

    // --- Open system clock safely ---
    private fun openSystemClock() {
        val packageName = prefs.getClockApp()
        if (packageName.isNotEmpty()) {
            val app = appsProvider.getAll().firstOrNull { it.packageName == packageName }
            if (app != null) {
                if (!appsProvider.launch(app)) {
                    Toast.makeText(
                        this,
                        getString(R.string.unable_launch_app_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}