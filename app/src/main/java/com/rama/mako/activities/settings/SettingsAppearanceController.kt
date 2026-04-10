package com.rama.mako.activities.settings

import android.widget.RadioGroup
import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.PrefsManager

class SettingsAppearanceController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs

    fun setup() {
        setupFontStyle()
        setupTemperatureFormat()
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
}