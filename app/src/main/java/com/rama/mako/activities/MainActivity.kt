package com.rama.mako.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.ListView
import android.widget.TextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import android.window.OnBackInvokedCallback
import com.rama.mako.CsActivity
import com.rama.mako.R
import com.rama.mako.managers.AppListManager
import com.rama.mako.managers.AppsProvider
import com.rama.mako.managers.BatteryManager
import com.rama.mako.managers.ClockManager
import com.rama.mako.managers.HomeBackgroundManager
import com.rama.mako.managers.PrefsManager

class MainActivity : CsActivity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var batteryText: TextView
    private lateinit var listView: ListView

    private lateinit var clockManager: ClockManager
    private lateinit var batteryManager: BatteryManager
    private lateinit var appListManager: AppListManager
    private lateinit var appsProvider: AppsProvider

    private lateinit var homeBackgroundManager: HomeBackgroundManager
    private lateinit var rootView: View

    private lateinit var searchField: EditText
    private lateinit var searchIcon: FrameLayout
    private lateinit var clearBtn: FrameLayout
    private var isSearchBarAlwaysVisible = false

    private var backCallback: OnBackInvokedCallback? = null
    private var isSearchExpanded = false
    private var isProgrammaticSearchUpdate = false
    private val searchDebounceHandler = Handler(Looper.getMainLooper())
    private var searchDebounceRunnable: Runnable? = null
    private var resumeRefreshRunnable: Runnable? = null
    private var currentSearchQuery: String = ""
    private var wallpaperReceiverRegistered = false
    private var lastAppliedBackgroundMode: String? = null
    private var lastAppliedWallpaperSignature: Int? = null

    companion object {
        private const val WALLPAPER_CHANGED_ACTION = "android.intent.action.WALLPAPER_CHANGED"
    }

    private val wallpaperChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            if (intent?.action == WALLPAPER_CHANGED_ACTION) {
                applyHomeBackground()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PrefsManager.getInstance(this).initPrefs()
        setContentView(R.layout.view_home)

        rootView = findViewById(R.id.root)
        applyEdgeToEdgePadding(rootView)

        // --- Prefs ---
        homeBackgroundManager = HomeBackgroundManager(this)
        applyHomeBackground(force = true)

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
        appListManager = AppListManager(
            this,
            listView,
            appsProvider
        ) {
            if (isSearchExpanded) {
                collapseSearch()
            }
        }
        appListManager.setup()

        val appLayout = findViewById<LinearLayout>(R.id.apps_layout)
        appLayout.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        initSearchbar()
        setupBackHandling()
    }

    // --- OnBackInvokedCallback registor for Android 13+ ---

    private fun setupBackHandling() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backCallback = OnBackInvokedCallback {
                // If search is always visible, loose focus and collapse keyboard
                if (isSearchBarAlwaysVisible) {
                    searchField.clearFocus()
                    val imm =
                        getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(searchField.windowToken, 0)
                }
                // If search is expanded, collapse it; otherwise consume back to prevent launcher restart
                else if (isSearchExpanded) {
                    collapseSearch()
                }
                // Else, do nothing (consume back to keep launcher open)
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_OVERLAY,
                backCallback!!
            )
        }
    }

    private fun initSearchbar() {
        searchField = findViewById(R.id.search_field)
        searchIcon = findViewById(R.id.search_icon)
        clearBtn = findViewById(R.id.clear_field)

        // Initially collapsed
        searchField.visibility = View.GONE
        clearBtn.visibility = View.GONE

        // Search icon
        searchIcon.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

            if (isSearchExpanded) {
                collapseSearch()
            } else {
                expandSearch()
            }
        }

        // Text change with debounce
        searchField.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isProgrammaticSearchUpdate) return

                val query = s.toString()

                // Cancel previous debounce
                searchDebounceRunnable?.let { searchDebounceHandler.removeCallbacks(it) }

                // Schedule new after 300ms
                searchDebounceRunnable = Runnable {
                    currentSearchQuery = query
                    appListManager.filter(currentSearchQuery)
                }
                searchDebounceHandler.postDelayed(searchDebounceRunnable!!, 300)

                // Clear button
                clearBtn.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Clear button (resets the list too)
        clearBtn.setOnClickListener {
            currentSearchQuery = ""
            searchDebounceRunnable?.let { searchDebounceHandler.removeCallbacks(it) }
            isProgrammaticSearchUpdate = true
            searchField.text.clear()
            isProgrammaticSearchUpdate = false
            appListManager.filter("")
            clearBtn.visibility = View.GONE
        }
    }

    private fun expandSearch() {
        isSearchExpanded = true

        // Show field
        searchField.visibility = View.VISIBLE
        if (!isSearchBarAlwaysVisible)
            searchField.requestFocus()

        val scaleX = ObjectAnimator.ofFloat(searchField, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(searchField, "scaleY", 0.8f, 1f)
        val alpha = ObjectAnimator.ofFloat(searchField, "alpha", 0f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 300
            interpolator = OvershootInterpolator(1.5f)
            start()
        }

        // Show keyboard
        val imm =
            getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(searchField, 0)
    }

    private fun collapseSearch(clearQuery: Boolean = true, hideKeyboard: Boolean = true) {
        isSearchExpanded = false

        // Hide field
        searchField.visibility = View.GONE
        clearBtn.visibility = View.GONE
        searchField.clearFocus()

        if (clearQuery) {
            currentSearchQuery = ""
            searchDebounceRunnable?.let { searchDebounceHandler.removeCallbacks(it) }
            isProgrammaticSearchUpdate = true
            searchField.text.clear()
            isProgrammaticSearchUpdate = false
            appListManager.filter("")
        }

        if (hideKeyboard) {
            val imm =
                getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(searchField.windowToken, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        applyHomeBackground()
        if (shouldListenWallpaperChanges()) {
            registerWallpaperReceiverIfNeeded()
        } else {
            unregisterWallpaperReceiverIfNeeded()
        }
        syncSettings()
        schedulePostResumeRefresh()

        if (isSearchBarAlwaysVisible)
            expandSearch()
    }

    override fun onPause() {
        super.onPause()
        unregisterWallpaperReceiverIfNeeded()
        clearPendingResumeRefresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterWallpaperReceiverIfNeeded()
        clearPendingResumeRefresh()

        // Clean up debounce handler
        searchDebounceRunnable?.let { searchDebounceHandler.removeCallbacks(it) }

        // Unregister back callback for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && backCallback != null) {
            onBackInvokedDispatcher.unregisterOnBackInvokedCallback(backCallback!!)
        }

        batteryManager.unregister()
        clockManager.stop()
    }

    override fun onBackPressed() {
        // Handle back for below Android 12
        // If search is always visible, loose focus and collapse keyboard
        if (isSearchBarAlwaysVisible) {
            searchField.clearFocus()
            val imm =
                getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(searchField.windowToken, 0)
        }
        // If search is expanded, collapse it; otherwise consume back to prevent launcher restart
        else if (isSearchExpanded) {
            collapseSearch()
        }
        // Else, do nothing (consume back to keep launcher open)
    }

    // --- Settings sync (row visibility only) ---
    private fun syncSettings() {
        val searchVisible = prefs.isSearchVisible()

        isSearchBarAlwaysVisible = prefs.isSearchBarAlwaysVisible()
        timeText.visibility =
            if (prefs.getClockFormat() != PrefsManager.ClockFormat.NONE) View.VISIBLE else View.GONE
        findViewById<View>(R.id.date_row).visibility =
            if (prefs.isDateVisible()) View.VISIBLE else View.GONE
        findViewById<View>(R.id.battery_row).visibility =
            if (prefs.isBatteryVisible()) View.VISIBLE else View.GONE
        findViewById<View>(R.id.searchbar).visibility =
            if (searchVisible) View.VISIBLE else View.GONE
        searchIcon.visibility =
            if (searchVisible && !isSearchBarAlwaysVisible) View.VISIBLE else View.GONE
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

    private fun applyHomeBackground(force: Boolean = false) {
        val mode = prefs.getHomeBackgroundMode()
        val wallpaperSignature =
            if (homeBackgroundManager.shouldTrackWallpaperChangesForMode(mode)) {
                homeBackgroundManager.getWallpaperSignature()
            } else {
                null
            }

        if (!force && mode == lastAppliedBackgroundMode && wallpaperSignature == lastAppliedWallpaperSignature) {
            return
        }

        if (mode == PrefsManager.BackgroundMode.WALLPAPER) {
            applyWallpaperModeBackground()
        } else {
            disableWindowWallpaper(mode)
            homeBackgroundManager.applyTo(rootView, mode)
        }

        lastAppliedBackgroundMode = mode
        lastAppliedWallpaperSignature = wallpaperSignature
    }

    private fun applyWallpaperModeBackground() {
        enableWindowWallpaper()
        rootView.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun enableWindowWallpaper() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(homeBackgroundManager.createWallpaperOverlayDrawable())
    }

    private fun disableWindowWallpaper(mode: String) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(homeBackgroundManager.createBackgroundDrawable(mode))
    }

    private fun schedulePostResumeRefresh() {
        clearPendingResumeRefresh()

        resumeRefreshRunnable = Runnable {
            if (isFinishing || isDestroyed) return@Runnable
            appListManager.refresh()
            batteryManager.forceUpdate()
        }

        rootView.post(resumeRefreshRunnable)
    }

    private fun clearPendingResumeRefresh() {
        resumeRefreshRunnable?.let {
            rootView.removeCallbacks(it)
        }
        resumeRefreshRunnable = null
    }

    private fun shouldListenWallpaperChanges(): Boolean {
        val mode = prefs.getHomeBackgroundMode()
        return homeBackgroundManager.shouldTrackWallpaperChangesForMode(mode)
    }

    private fun registerWallpaperReceiverIfNeeded() {
        if (wallpaperReceiverRegistered) return

        val filter = IntentFilter(WALLPAPER_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                wallpaperChangedReceiver,
                filter,
                RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(wallpaperChangedReceiver, filter)
        }

        wallpaperReceiverRegistered = true
    }

    private fun unregisterWallpaperReceiverIfNeeded() {
        if (!wallpaperReceiverRegistered) return

        runCatching { unregisterReceiver(wallpaperChangedReceiver) }
        wallpaperReceiverRegistered = false
    }
}