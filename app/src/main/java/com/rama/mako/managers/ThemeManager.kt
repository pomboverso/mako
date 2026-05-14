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
    private val CATPPUCCIN_MOCHA = Palette(
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


    // Dracula
    private val DRACULA = Palette(
        foreground = 0xFFF8F8F2.toInt(), // foreground
        bgPrimary = 0xFF282A36.toInt(), // background
        bgSecondary = 0xFF343746.toInt(), // current line
        bgTertiary = 0xFF424450.toInt(), // comment
        accentPrimary = 0xFF50FA7B.toInt(), // green
        accentSecondary = 0xFFF1FA8C.toInt(), // yellow
        accentTertiary = 0xFFFFB86C.toInt(), // orange
        disabled = 0xFF6272A4.toInt(), // comment
        input = 0xFF21222C.toInt(), // darker bg
        buttonPrimary = 0xFFBD93F9.toInt(), // purple
        buttonSecondary = 0xFF8BE9FD.toInt(), // cyan
        buttonDanger = 0xFFFF79C6.toInt(), // pink
        buttonPin = 0xFF282A36.toInt(), // background
    )

    // Melange Dark
    private val MELANGE = Palette(
        foreground = 0xFFECE1D7.toInt(), // text
        bgPrimary = 0xFF292522.toInt(), // background
        bgSecondary = 0xFF352F2A.toInt(), // surface
        bgTertiary = 0xFF403A34.toInt(), // overlay
        accentPrimary = 0xFF78997A.toInt(), // green
        accentSecondary = 0xFFEBC06D.toInt(), // yellow
        accentTertiary = 0xFFE49B5D.toInt(), // orange
        disabled = 0xFF867462.toInt(), // muted
        input = 0xFF211E1B.toInt(), // darker bg
        buttonPrimary = 0xFF7F91B2.toInt(), // blue
        buttonSecondary = 0xFF85B695.toInt(), // teal
        buttonDanger = 0xFFB65C60.toInt(), // red
        buttonPin = 0xFF292522.toInt(), // background
    )

    // Tokyo Night
    private val TOKYO_NIGHT = Palette(
        foreground = 0xFFC0CAF5.toInt(), // fg
        bgPrimary = 0xFF1A1B26.toInt(), // bg
        bgSecondary = 0xFF24283B.toInt(), // bg_highlight
        bgTertiary = 0xFF292E42.toInt(), // bg_visual
        accentPrimary = 0xFF9ECE6A.toInt(), // green
        accentSecondary = 0xFFE0AF68.toInt(), // yellow
        accentTertiary = 0xFFFF9E64.toInt(), // orange
        disabled = 0xFF565F89.toInt(), // comment
        input = 0xFF16161E.toInt(), // bg_dark
        buttonPrimary = 0xFF7AA2F7.toInt(), // blue
        buttonSecondary = 0xFF2AC3DE.toInt(), // cyan
        buttonDanger = 0xFFF7768E.toInt(), // red
        buttonPin = 0xFF1A1B26.toInt(), // bg
    )

    fun paletteFor(theme: String): Palette = when (theme) {
        PrefsManager.Theme.CATPPUCCIN_MOCHA -> CATPPUCCIN_MOCHA
        PrefsManager.Theme.DRACULA -> DRACULA
        PrefsManager.Theme.MELANGE -> MELANGE
        PrefsManager.Theme.TOKYO_NIGHT -> TOKYO_NIGHT
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
            MAKO.bgPrimary, CATPPUCCIN_MOCHA.bgPrimary,
            DRACULA.bgPrimary, MELANGE.bgPrimary, TOKYO_NIGHT.bgPrimary,
            context.getColor(R.color.bg_primary) -> palette.bgPrimary

            // bg_secondary
            MAKO.bgSecondary, CATPPUCCIN_MOCHA.bgSecondary,
            DRACULA.bgSecondary, MELANGE.bgSecondary, TOKYO_NIGHT.bgSecondary,
            context.getColor(R.color.bg_secondary) -> palette.bgSecondary

            // bg_tertiary
            MAKO.bgTertiary, CATPPUCCIN_MOCHA.bgTertiary,
            DRACULA.bgTertiary, MELANGE.bgTertiary, TOKYO_NIGHT.bgTertiary,
            context.getColor(R.color.bg_tertiary) -> palette.bgTertiary

            // button_primary
            MAKO.buttonPrimary, CATPPUCCIN_MOCHA.buttonPrimary,
            DRACULA.buttonPrimary, MELANGE.buttonPrimary, TOKYO_NIGHT.buttonPrimary,
            context.getColor(R.color.button_primary) -> palette.buttonPrimary

            // button_secondary
            MAKO.buttonSecondary, CATPPUCCIN_MOCHA.buttonSecondary,
            DRACULA.buttonSecondary, MELANGE.buttonSecondary, TOKYO_NIGHT.buttonSecondary,
            context.getColor(R.color.button_secondary) -> palette.buttonSecondary

            // button_danger
            MAKO.buttonDanger, CATPPUCCIN_MOCHA.buttonDanger,
            DRACULA.buttonDanger, MELANGE.buttonDanger, TOKYO_NIGHT.buttonDanger,
            context.getColor(R.color.button_danger) -> palette.buttonDanger

            // button_pin
            MAKO.buttonPin, CATPPUCCIN_MOCHA.buttonPin,
            DRACULA.buttonPin, MELANGE.buttonPin, TOKYO_NIGHT.buttonPin,
            context.getColor(R.color.button_pin) -> palette.buttonPin

            // input
            MAKO.input, CATPPUCCIN_MOCHA.input,
            DRACULA.input, MELANGE.input, TOKYO_NIGHT.input,
            context.getColor(R.color.input) -> palette.input

            // disabled
            MAKO.disabled, CATPPUCCIN_MOCHA.disabled,
            DRACULA.disabled, MELANGE.disabled, TOKYO_NIGHT.disabled,
            context.getColor(R.color.disabled) -> palette.disabled

            else -> null
        }
    }

    private fun resolveDrawableColor(drawable: android.graphics.drawable.Drawable): Int? {
        return if (drawable is android.graphics.drawable.ColorDrawable) drawable.color else null
    }
}
