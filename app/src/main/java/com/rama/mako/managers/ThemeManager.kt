package com.rama.mako.managers

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import com.rama.mako.R

object ThemeManager {

    data class Palette(
        val foreground: Int,
        val bgPrimary: Int,
        val bgSecondary: Int,
        val bgTertiary: Int,
        val accentPrimary: Int,
        val accentSecondary: Int,
        val accentTertiary: Int,
        val disabled: Int,
        val input: Int,
        val buttonPrimary: Int,
        val buttonSecondary: Int,
        val buttonDanger: Int,
        val buttonPin: Int,
    )

    // Mako (default)
    private val MAKO = Palette(
        foreground = 0xFFCCCCCC.toInt(),
        bgPrimary = 0xFF141417.toInt(),
        bgSecondary = 0xFF1F1F29.toInt(),
        bgTertiary = 0xFF322F1B.toInt(),
        accentPrimary = 0xFFABD68E.toInt(),
        accentSecondary = 0xFFCDC58B.toInt(),
        accentTertiary = 0xFFDCD07C.toInt(),
        disabled = 0xFF888888.toInt(),
        input = 0xFF16161F.toInt(),
        buttonPrimary = 0xFF459984.toInt(),
        buttonSecondary = 0xFF6194AF.toInt(),
        buttonDanger = 0xFFDC6364.toInt(),
        buttonPin = 0xFF333355.toInt(),
    )

    // Catppuccin Mocha
    private val CATPPUCCIN = Palette(
        foreground = 0xFFCDD6F4.toInt(), // text
        bgPrimary = 0xFF1E1E2E.toInt(), // base
        bgSecondary = 0xFF313244.toInt(), // surface0
        bgTertiary = 0xFF45475A.toInt(), // surface1
        accentPrimary = 0xFFA6E3A1.toInt(), // green
        accentSecondary = 0xFFF9E2AF.toInt(), // yellow
        accentTertiary = 0xFFFFD700.toInt(), // gold (placeholder, tweak as needed)
        disabled = 0xFF6C7086.toInt(), // overlay0
        input = 0xFF181825.toInt(), // mantle
        buttonPrimary = 0xFF89B4FA.toInt(), // blue
        buttonSecondary = 0xFF74C7EC.toInt(), // sapphire
        buttonDanger = 0xFFF38BA8.toInt(), // red
        buttonPin = 0xFF1E1E2E.toInt(), // base (same as bg for subtlety)
    )

    fun paletteFor(theme: String): Palette = when (theme) {
        PrefsManager.Theme.CATPPUCCIN -> CATPPUCCIN
        else -> MAKO
    }

    fun applyTheme(context: Context, root: View) {
        val prefs = PrefsManager.getInstance(context)
        val palette = paletteFor(prefs.getTheme())
        applyRecursively(context, root, palette)
    }

    private fun applyRecursively(context: Context, view: View, palette: Palette) {
        applyToView(context, view, palette)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyRecursively(context, view.getChildAt(i), palette)
            }
        }
    }

    private fun applyToView(context: Context, view: View, palette: Palette) {
        val bg = view.background ?: return

        // Map the view's current background color to the matching palette slot
        val currentColor = resolveDrawableColor(bg) ?: return

        val mapped = mapColor(context, currentColor, palette) ?: return
        view.setBackgroundColor(mapped)

        // Also tint text on views where we set a background
        if (view is TextView && view !is CheckBox && view !is RadioButton) {
            view.setTextColor(palette.foreground)
        }
    }

    /**
     * Maps a color from any palette to the equivalent slot in [palette].
     * This works by comparing the incoming color against all known palette
     * slots across both themes.
     */
    private fun mapColor(context: Context, color: Int, palette: Palette): Int? {
        return when (color) {
            // bg_primary
            MAKO.bgPrimary, CATPPUCCIN.bgPrimary,
            context.getColor(R.color.bg_primary) -> palette.bgPrimary

            // bg_secondary
            MAKO.bgSecondary, CATPPUCCIN.bgSecondary,
            context.getColor(R.color.bg_secondary) -> palette.bgSecondary

            // bg_tertiary
            MAKO.bgTertiary, CATPPUCCIN.bgTertiary,
            context.getColor(R.color.bg_tertiary) -> palette.bgTertiary

            // button_primary
            MAKO.buttonPrimary, CATPPUCCIN.buttonPrimary,
            context.getColor(R.color.button_primary) -> palette.buttonPrimary

            // button_secondary
            MAKO.buttonSecondary, CATPPUCCIN.buttonSecondary,
            context.getColor(R.color.button_secondary) -> palette.buttonSecondary

            // button_danger
            MAKO.buttonDanger, CATPPUCCIN.buttonDanger,
            context.getColor(R.color.button_danger) -> palette.buttonDanger

            // button_pin
            MAKO.buttonPin, CATPPUCCIN.buttonPin,
            context.getColor(R.color.button_pin) -> palette.buttonPin

            // input
            MAKO.input, CATPPUCCIN.input,
            context.getColor(R.color.input) -> palette.input

            // disabled
            MAKO.disabled, CATPPUCCIN.disabled,
            context.getColor(R.color.disabled) -> palette.disabled

            else -> null
        }
    }

    private fun resolveDrawableColor(drawable: android.graphics.drawable.Drawable): Int? {
        return if (drawable is android.graphics.drawable.ColorDrawable) drawable.color else null
    }
}
