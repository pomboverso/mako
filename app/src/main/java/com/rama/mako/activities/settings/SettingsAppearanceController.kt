package com.rama.mako.activities.settings

import android.view.View
import android.widget.RadioGroup
import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.PrefsManager

class SettingsAppearanceController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs

    fun setup() {
        setupFontStyle()
        setupTemperatureFormat()
        setupBackgroundMode()
    }

    private fun setupFontStyle() {
        val group = activity.findViewById<RadioGroup>(R.id.font_style_group)

        when (prefs.getFontStyle()) {
            PrefsManager.FontStyle.JERSEY_25 -> group.check(R.id.font_jersey)
            PrefsManager.FontStyle.MONTSERRAT -> group.check(R.id.font_montserrat)
            PrefsManager.FontStyle.ROBOTO_SLAB -> group.check(R.id.font_robotoslab)
            PrefsManager.FontStyle.QUICKSAND -> group.check(R.id.font_quicksand)
            else -> group.check(R.id.font_default)
        }

        group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.font_jersey -> prefs.setFontStyle(PrefsManager.FontStyle.JERSEY_25)
                R.id.font_quicksand -> prefs.setFontStyle(PrefsManager.FontStyle.QUICKSAND)
                R.id.font_robotoslab -> prefs.setFontStyle(PrefsManager.FontStyle.ROBOTO_SLAB)
                R.id.font_montserrat -> prefs.setFontStyle(PrefsManager.FontStyle.MONTSERRAT)
                R.id.font_default -> prefs.setFontStyle(PrefsManager.FontStyle.DEFAULT)
            }
            activity.refreshFont()
        }
    }

    private fun setupTemperatureFormat() {
        val group = activity.findViewById<RadioGroup>(R.id.temperature_format_group)

        when (prefs.getTemperatureFormat()) {
            PrefsManager.TemperatureFormat.CELSIUS -> group.check(R.id.temperature_celsius)
            PrefsManager.TemperatureFormat.FAHRENHEIT -> group.check(R.id.temperature_fahrenheit)
            else -> group.check(R.id.temperature_system)
        }

        group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.temperature_celsius -> prefs.setTemperatureFormat(PrefsManager.TemperatureFormat.CELSIUS)
                R.id.temperature_fahrenheit -> prefs.setTemperatureFormat(PrefsManager.TemperatureFormat.FAHRENHEIT)
                else -> prefs.setTemperatureFormat(PrefsManager.TemperatureFormat.DEFAULT)
            }
        }
    }

    private fun setupBackgroundMode() {
        val group = activity.findViewById<RadioGroup>(R.id.home_background_mode_group)
        val wallpaperButton = activity.findViewById<View>(R.id.wallpaper_button)

        val initialMode = prefs.getHomeBackgroundMode()

        when (initialMode) {
            PrefsManager.BackgroundMode.WALLPAPER -> group.check(R.id.home_background_wallpaper)
            PrefsManager.BackgroundMode.DYNAMIC -> group.check(R.id.home_background_dynamic)
            PrefsManager.BackgroundMode.AMOLED -> group.check(R.id.home_background_amoled)
            else -> group.check(R.id.home_background_default)
        }

        updateWallpaperButtonVisibility(wallpaperButton, initialMode)

        group.setOnCheckedChangeListener { _, id ->
            val mode = when (id) {
                R.id.home_background_wallpaper -> PrefsManager.BackgroundMode.WALLPAPER
                R.id.home_background_dynamic -> PrefsManager.BackgroundMode.DYNAMIC
                R.id.home_background_amoled -> PrefsManager.BackgroundMode.AMOLED
                else -> PrefsManager.BackgroundMode.DEFAULT
            }

            prefs.setHomeBackgroundMode(mode)
            updateWallpaperButtonVisibility(wallpaperButton, mode)
            activity.applySettingsBackground()
        }
    }

    private fun updateWallpaperButtonVisibility(button: View, mode: String) {
        button.visibility = if (
            mode == PrefsManager.BackgroundMode.WALLPAPER ||
            mode == PrefsManager.BackgroundMode.DYNAMIC
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}