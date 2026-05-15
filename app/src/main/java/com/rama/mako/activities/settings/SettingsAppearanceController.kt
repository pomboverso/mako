package com.rama.mako.activities.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.FontManager
import com.rama.mako.managers.PrefsManager
import com.rama.mako.managers.ThemeManager
import java.io.File
import java.io.FileOutputStream

class SettingsAppearanceController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs

    fun setup() {
        setupFontStyle()
        setupTemperatureFormat()
        setupTheme()
        setupCustomTheme()
        setupBackgroundMode()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == activity.FONT_PICK_REQUEST && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data ?: return
            val savedPath = copyFontToInternalStorage(uri)
            if (savedPath != null) {
                FontManager.clearCustomCache()
                prefs.setCustomFontPath(savedPath)
                prefs.setFontStyle(PrefsManager.FontStyle.CUSTOM)
                updateCustomFontLabel()
                activity.refreshFont()
            }
        }
    }

    private fun setupFontStyle() {
        val group = activity.findViewById<RadioGroup>(R.id.font_style_group)

        when (prefs.getFontStyle()) {
            PrefsManager.FontStyle.JERSEY_25 -> group.check(R.id.font_jersey)
            PrefsManager.FontStyle.CUSTOM -> group.check(R.id.font_custom)
            else -> group.check(R.id.font_default)
        }

        group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.font_jersey -> {
                    prefs.setFontStyle(PrefsManager.FontStyle.JERSEY_25)
                    activity.refreshFont()
                }
                R.id.font_default -> {
                    prefs.setFontStyle(PrefsManager.FontStyle.DEFAULT)
                    activity.refreshFont()
                }
                R.id.font_custom -> {
                    // If a font is already saved, apply it immediately;
                    // otherwise open the picker right away.
                    if (prefs.getCustomFontPath().isNotBlank()) {
                        prefs.setFontStyle(PrefsManager.FontStyle.CUSTOM)
                        activity.refreshFont()
                    } else {
                        openFontPicker()
                    }
                }
            }
        }

        // Button to (re-)pick a font file
        activity.findViewById<View>(R.id.font_custom_pick_btn).setOnClickListener {
            openFontPicker()
        }

        updateCustomFontLabel()
    }

    private fun openFontPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "font/ttf", "font/otf", "application/x-font-ttf",
                "application/x-font-otf", "application/octet-stream"
            ))
        }
        activity.startActivityForResult(intent, activity.FONT_PICK_REQUEST)
    }

    private fun copyFontToInternalStorage(uri: Uri): String? {
        return runCatching {
            val inputStream = activity.contentResolver.openInputStream(uri) ?: return null
            val dir = File(activity.filesDir, "fonts").also { it.mkdirs() }
            // Preserve extension (.ttf / .otf) for Typeface.createFromFile
            val ext = activity.contentResolver.getType(uri)
                ?.let { if (it.contains("otf")) "otf" else "ttf" } ?: "ttf"
            val dest = File(dir, "custom_font.$ext")
            FileOutputStream(dest).use { out -> inputStream.copyTo(out) }
            dest.absolutePath
        }.getOrNull()
    }

    private fun updateCustomFontLabel() {
        val label = activity.findViewById<TextView>(R.id.font_custom_name_label)
        val path = prefs.getCustomFontPath()
        label.text = if (path.isNotBlank()) File(path).name else activity.getString(R.string.font_custom_none_label)
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
        activity.findViewById<EditText>(R.id.fg).setText(colorToHex(palette.foreground))
        activity.findViewById<EditText>(R.id.collapsible_header)
            .setText(colorToHex(palette.collapsible_header))
        activity.findViewById<EditText>(R.id.clock).setText(colorToHex(palette.clock))
        activity.findViewById<EditText>(R.id.icons).setText(colorToHex(palette.icon))
        activity.findViewById<EditText>(R.id.accent).setText(colorToHex(palette.accent_1))
        activity.findViewById<EditText>(R.id.bg_1).setText(colorToHex(palette.bg_1))
        activity.findViewById<EditText>(R.id.bg_2).setText(colorToHex(palette.bg_2))
        activity.findViewById<EditText>(R.id.bg_3).setText(colorToHex(palette.bg_3))
        activity.findViewById<EditText>(R.id.input).setText(colorToHex(palette.input))
        activity.findViewById<EditText>(R.id.btn_1).setText(colorToHex(palette.button_1))
        activity.findViewById<EditText>(R.id.btn_2).setText(colorToHex(palette.button_2))
        activity.findViewById<EditText>(R.id.danger).setText(colorToHex(palette.danger))
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
                PrefsManager.PrefKeys.APP_THEME_FOREGROUND to activity.findViewById<EditText>(R.id.fg),
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