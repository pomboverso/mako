package com.rama.mako.activities.settings

import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.PrefsManager
import com.rama.mako.managers.ThemeManager
import com.rama.mako.widgets.WdColorPicker

class SettingsAppearanceController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs

    fun setup() {
        setupFontStyle()
        setupTemperatureFormat()
        setupTheme()
        setupCustomTheme()
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

    private fun setupTheme() {
        val group = activity.findViewById<RadioGroup>(R.id.themes_group)
        val form = activity.findViewById<View>(R.id.themes_form)

        // Show form only if custom is already selected
        form.visibility =
            if (prefs.getTheme() == PrefsManager.Theme.CUSTOM) View.VISIBLE else View.GONE

        when (prefs.getTheme()) {
            PrefsManager.Theme.RAMA -> group.check(R.id.theme_rama)
            PrefsManager.Theme.MAKO -> group.check(R.id.theme_mako)
            PrefsManager.Theme.CATPPUCCIN_MOCHA -> group.check(R.id.theme_catppuccin_mocha)
            PrefsManager.Theme.DRACULA -> group.check(R.id.theme_dracula)
            PrefsManager.Theme.MELANGE -> group.check(R.id.theme_melange)
            PrefsManager.Theme.TOKYO_NIGHT -> group.check(R.id.theme_tokyo_night)
            PrefsManager.Theme.CUSTOM -> group.check(R.id.theme_custom)
            else -> group.check(R.id.theme_mako)
        }

        group.setOnCheckedChangeListener { _, id ->
            val theme = when (id) {
                R.id.theme_rama -> PrefsManager.Theme.RAMA
                R.id.theme_mako -> PrefsManager.Theme.MAKO
                R.id.theme_catppuccin_mocha -> PrefsManager.Theme.CATPPUCCIN_MOCHA
                R.id.theme_dracula -> PrefsManager.Theme.DRACULA
                R.id.theme_melange -> PrefsManager.Theme.MELANGE
                R.id.theme_tokyo_night -> PrefsManager.Theme.TOKYO_NIGHT
                R.id.theme_custom -> PrefsManager.Theme.CUSTOM
                else -> PrefsManager.Theme.MAKO
            }

            // Show/hide the custom form
            form.visibility = if (theme == PrefsManager.Theme.CUSTOM) View.VISIBLE else View.GONE

            if (theme != PrefsManager.Theme.CUSTOM) {
                // For built-in themes: save and apply immediately
                prefs.setTheme(theme)
                activity.recreate()
            } else {
                // For custom: populate fields with current palette but don't apply yet
                populateCustomFields(ThemeManager.paletteFor(prefs.getTheme(), activity))
            }
        }
    }

    private fun colorToHex(color: Int): String =
        String.format("#%06X", 0xFFFFFF and color)

    private fun populateCustomFields(palette: ThemeManager.Palette) {
        activity.findViewById<WdColorPicker>(R.id.foreground).setColor(palette.foreground)
        activity.findViewById<WdColorPicker>(R.id.collapsible_header)
            .setColor(palette.collapsible_header)
        activity.findViewById<WdColorPicker>(R.id.clock).setColor(palette.clock)
        activity.findViewById<WdColorPicker>(R.id.icons).setColor(palette.icon)
        activity.findViewById<WdColorPicker>(R.id.accent).setColor(palette.accent_1)
        activity.findViewById<WdColorPicker>(R.id.bg_2).setColor(palette.bg_2)
        activity.findViewById<WdColorPicker>(R.id.bg_3).setColor(palette.bg_3)
        activity.findViewById<WdColorPicker>(R.id.bg_1).setColor(palette.bg_1)
        activity.findViewById<WdColorPicker>(R.id.input).setColor(palette.input)
        activity.findViewById<WdColorPicker>(R.id.btn_1).setColor(palette.button_1)
        activity.findViewById<WdColorPicker>(R.id.btn_2).setColor(palette.button_2)
        activity.findViewById<WdColorPicker>(R.id.danger).setColor(palette.danger)
    }

    private fun parseHex(text: String): Int? {
        val hex = text.trim()
        if (!hex.matches(Regex("^#[0-9A-Fa-f]{6}$"))) return null
        return runCatching { Color.parseColor(hex) }.getOrNull()
    }

    private fun setupCustomTheme() {
        // Populate fields with current theme palette on open
        val currentPalette = ThemeManager.paletteFor(prefs.getTheme(), activity)
        populateCustomFields(currentPalette)

        val saveButton = activity.findViewById<android.view.View>(R.id.save_custom_theme)
        saveButton.setOnClickListener {
            val fields = mapOf(
                PrefsManager.PrefKeys.APP_THEME_FOREGROUND to activity.findViewById<EditText>(R.id.foreground),
                PrefsManager.PrefKeys.APP_THEME_COLLAPSIBLE_HEADER to activity.findViewById<EditText>(
                    R.id.collapsible_header
                ),
                PrefsManager.PrefKeys.APP_THEME_CLOCK to activity.findViewById<EditText>(R.id.clock),
                PrefsManager.PrefKeys.APP_THEME_ICON to activity.findViewById<EditText>(R.id.icons),
                PrefsManager.PrefKeys.APP_THEME_ACCENT_1 to activity.findViewById<EditText>(R.id.accent),
                PrefsManager.PrefKeys.APP_THEME_BG_1 to activity.findViewById<EditText>(R.id.bg_1),
                PrefsManager.PrefKeys.APP_THEME_BG_2 to activity.findViewById<EditText>(R.id.bg_2),
                PrefsManager.PrefKeys.APP_THEME_BG_3 to activity.findViewById<EditText>(R.id.bg_3),
                PrefsManager.PrefKeys.APP_THEME_INPUT to activity.findViewById<EditText>(R.id.input),
                PrefsManager.PrefKeys.APP_THEME_BUTTON_1 to activity.findViewById<EditText>(R.id.btn_1),
                PrefsManager.PrefKeys.APP_THEME_BUTTON_2 to activity.findViewById<EditText>(R.id.btn_2),
                PrefsManager.PrefKeys.APP_THEME_DANGER to activity.findViewById<EditText>(R.id.danger),
            )

            var allValid = true
            fields.forEach { (key, editText) ->
                val color = parseHex(editText.text.toString())
                if (color != null) {
                    prefs.setCustomThemeColor(key, color)
                } else {
                    editText.error = "Invalid hex"
                    allValid = false
                }
            }

            if (allValid) {
                prefs.setTheme(PrefsManager.Theme.CUSTOM)
                activity.recreate()
            }
        }
    }

    private fun setupBackgroundMode() {
        val group = activity.findViewById<RadioGroup>(R.id.home_background_mode_group)

        val initialMode = prefs.getHomeBackgroundMode()

        when (initialMode) {
            PrefsManager.BackgroundMode.WALLPAPER -> group.check(R.id.home_background_wallpaper)
            else -> group.check(R.id.home_background_default)
        }

        group.setOnCheckedChangeListener { _, id ->
            val mode = when (id) {
                R.id.home_background_wallpaper -> PrefsManager.BackgroundMode.WALLPAPER
                else -> PrefsManager.BackgroundMode.DEFAULT
            }

            prefs.setHomeBackgroundMode(mode)
            activity.applySettingsBackground()
        }
    }
}