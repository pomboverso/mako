package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.rama.mako.CsActivity
import com.rama.mako.R
import com.rama.mako.activities.settings.SettingsAppearanceController
import com.rama.mako.activities.settings.SettingsBasicController
import com.rama.mako.activities.settings.SettingsCheckboxController
import com.rama.mako.activities.settings.SettingsClockController
import com.rama.mako.activities.settings.SettingsPinController
import com.rama.mako.activities.settings.SettingsGroupsController
import com.rama.mako.activities.settings.SettingsIconsController
import com.rama.mako.activities.settings.SettingsLanguageController
import com.rama.mako.activities.settings.SettingsExtController
import com.rama.mako.managers.AppsProvider
import com.rama.mako.managers.GroupsManager
import com.rama.mako.managers.HomeBackgroundManager
import com.rama.mako.managers.IconManager
import com.rama.mako.managers.PrefsManager

class SettingsActivity : CsActivity() {

    lateinit var appsProvider: AppsProvider
    lateinit var iconManager: IconManager
    lateinit var groupsManager: GroupsManager

    private lateinit var clockController: SettingsClockController
    private lateinit var appearanceController: SettingsAppearanceController
    private lateinit var homeBackgroundManager: HomeBackgroundManager
    private lateinit var settingsRootView: View

    private var lastAppliedBackgroundMode: String? = null
    private var lastAppliedWallpaperSignature: Int? = null
    
    private var isUnlocked = false
    private var isLockScreenShowing = false
    private val LOCK_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_settings)

        settingsRootView = findViewById(R.id.settings_root)
        applyEdgeToEdgePadding(settingsRootView)

        homeBackgroundManager = HomeBackgroundManager(this)
        applySettingsBackground(force = true)

        appsProvider = AppsProvider(this)
        iconManager = IconManager(this, appsProvider)
        groupsManager = GroupsManager(this, appsProvider)

        clockController = SettingsClockController(this).also { it.setup() }

        SettingsBasicController(this).setup()
        appearanceController = SettingsAppearanceController(this).also { it.setup() }
        SettingsLanguageController(this).setup()
        SettingsIconsController(this).setup()
        SettingsCheckboxController(this).setup()
        SettingsGroupsController(this).setup()
        SettingsPinController(this).setup()

        // ext flavor
        SettingsExtController(this).setup()
    }

    override fun onResume() {
        super.onResume()
        applySettingsBackground()

        // Prevent re-lock if already unlocked or lock screen is active
        if (isUnlocked || isLockScreenShowing) return

        val lockEnabled = prefs.getBoolean(
            PrefsManager.PrefKeys.SECURITY_KEYPAD_VISIBLE,
            false
        )
        val hasPin = prefs.getPin().isNotEmpty()

        if (lockEnabled && hasPin) {
            isLockScreenShowing = true
            startActivityForResult(
                Intent(this, LockActivity::class.java),
                LOCK_REQUEST
            )
        }
    }

    fun applySettingsBackground(force: Boolean = false) {
        val mode = prefs.getHomeBackgroundMode()

        val wallpaperSignature =
            if (homeBackgroundManager.shouldTrackWallpaperChangesForMode(mode)) {
                homeBackgroundManager.getWallpaperSignature()
            } else {
                null
            }

        if (!force &&
            mode == lastAppliedBackgroundMode &&
            wallpaperSignature == lastAppliedWallpaperSignature
        ) {
            return
        }

        if (mode == PrefsManager.BackgroundMode.WALLPAPER) {
            enableWindowWallpaper()
        } else {
            disableWindowWallpaper(mode)
            homeBackgroundManager.applyToSettings(settingsRootView, mode)
        }

        lastAppliedBackgroundMode = mode
        lastAppliedWallpaperSignature = wallpaperSignature
    }

    private fun enableWindowWallpaper() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(
            homeBackgroundManager.createWallpaperOverlayDrawable()
        )
    }

    private fun disableWindowWallpaper(mode: String) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(
            homeBackgroundManager.createBackgroundDrawable(mode)
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOCK_REQUEST) {
            isLockScreenShowing = false

            if (resultCode == RESULT_OK) {
                isUnlocked = true
            }
        }

        clockController.onActivityResult(requestCode, resultCode, data)
    }
}