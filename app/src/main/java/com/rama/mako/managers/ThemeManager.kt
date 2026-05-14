package com.rama.mako.managers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import com.rama.mako.R

object ThemeManager {

    data class Palette(
        val foreground: Int,
        val bg_1: Int,
        val bg_2: Int,
        val bg_3: Int,
        val accent_1: Int,
        val accent_2: Int,
        val accent_3: Int,
        val disabled: Int,
        val input: Int,
        val button_1: Int,
        val button_2: Int,
        val danger: Int,
        val header: Int,
    )

    // Mako (default)
    private val MAKO = Palette(
        foreground = 0xFFCCCCCC.toInt(),
        bg_1 = 0xFF141417.toInt(),
        bg_2 = 0xFF1F1F29.toInt(),
        bg_3 = 0xFF24313b.toInt(),
        accent_1 = 0xFFABD68E.toInt(),
        accent_2 = 0xFFCDC58B.toInt(),
        accent_3 = 0xFFDCD07C.toInt(),
        disabled = 0xFF888888.toInt(),
        input = 0xFF16161F.toInt(),
        button_1 = 0xFF459984.toInt(),
        button_2 = 0xFF6194AF.toInt(),
        danger = 0xFFDC6364.toInt(),
        header = 0xff888888.toInt(),
    )

    // Catppuccin Mocha
    private val CATPPUCCIN_MOCHA = Palette(
        foreground = 0xFFCDD6F4.toInt(),
        bg_1 = 0xFF1E1E2E.toInt(),
        bg_2 = 0xFF313244.toInt(),
        bg_3 = 0xFF45475A.toInt(),
        accent_1 = 0xFFA6E3A1.toInt(),
        accent_2 = 0xFFF9E2AF.toInt(),
        accent_3 = 0xFFFFD700.toInt(),
        disabled = 0xFF6C7086.toInt(),
        input = 0xFF181825.toInt(),
        button_1 = 0xFF89B4FA.toInt(),
        button_2 = 0xFF74C7EC.toInt(),
        danger = 0xFFF38BA8.toInt(),
        header = 0xFFB4BEFE.toInt(),
    )


    // Dracula
    private val DRACULA = Palette(
        foreground = 0xFFF8F8F2.toInt(),
        bg_1 = 0xFF282A36.toInt(),
        bg_2 = 0xFF343746.toInt(),
        bg_3 = 0xFF424450.toInt(),
        accent_1 = 0xFF50FA7B.toInt(),
        accent_2 = 0xFFF1FA8C.toInt(),
        accent_3 = 0xFFFFB86C.toInt(),
        disabled = 0xFF6272A4.toInt(),
        input = 0xFF21222C.toInt(),
        button_1 = 0xFFBD93F9.toInt(),
        button_2 = 0xFF8BE9FD.toInt(),
        danger = 0xFFFF79C6.toInt(),
        header = 0xFFBD93F9.toInt(),
    )

    // Melange Dark
    private val MELANGE = Palette(
        foreground = 0xFFECE1D7.toInt(),
        bg_1 = 0xFF292522.toInt(),
        bg_2 = 0xFF352F2A.toInt(),
        bg_3 = 0xFF403A34.toInt(),
        accent_1 = 0xFF78997A.toInt(),
        accent_2 = 0xFFEBC06D.toInt(),
        accent_3 = 0xFFE49B5D.toInt(),
        disabled = 0xFF867462.toInt(),
        input = 0xFF211E1B.toInt(),
        button_1 = 0xFF7F91B2.toInt(),
        button_2 = 0xFF85B695.toInt(),
        danger = 0xFFB65C60.toInt(),
        header = 0xFFEBC06D.toInt(),
    )

    // Tokyo Night
    private val TOKYO_NIGHT = Palette(
        foreground = 0xFFC0CAF5.toInt(),
        bg_1 = 0xFF1A1B26.toInt(),
        bg_2 = 0xFF24283B.toInt(),
        bg_3 = 0xFF292E42.toInt(),
        accent_1 = 0xFF9ECE6A.toInt(),
        accent_2 = 0xFFE0AF68.toInt(),
        accent_3 = 0xFFFF9E64.toInt(),
        disabled = 0xFF565F89.toInt(),
        input = 0xFF16161E.toInt(),
        button_1 = 0xFF7AA2F7.toInt(),
        button_2 = 0xFF2AC3DE.toInt(),
        danger = 0xFFF7768E.toInt(),
        header = 0xFF7AA2F7.toInt(),
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
            MAKO.bg_1, CATPPUCCIN_MOCHA.bg_1,
            DRACULA.bg_1, MELANGE.bg_1, TOKYO_NIGHT.bg_1,
            context.resources.getColor(R.color.bg_1) -> palette.bg_1

            // bg_secondary
            MAKO.bg_2, CATPPUCCIN_MOCHA.bg_2,
            DRACULA.bg_2, MELANGE.bg_2, TOKYO_NIGHT.bg_2,
            context.resources.getColor(R.color.bg_2) -> palette.bg_2

            // bg_tertiary
            MAKO.bg_3, CATPPUCCIN_MOCHA.bg_3,
            DRACULA.bg_3, MELANGE.bg_3, TOKYO_NIGHT.bg_3,
            context.resources.getColor(R.color.bg_3) -> palette.bg_3

            // button_primary
            MAKO.button_1, CATPPUCCIN_MOCHA.button_1,
            DRACULA.button_1, MELANGE.button_1, TOKYO_NIGHT.button_1,
            context.resources.getColor(R.color.button_1) -> palette.button_1

            // button_secondary
            MAKO.button_2, CATPPUCCIN_MOCHA.button_2,
            DRACULA.button_2, MELANGE.button_2, TOKYO_NIGHT.button_2,
            context.resources.getColor(R.color.button_2) -> palette.button_2

            // button_danger
            MAKO.danger, CATPPUCCIN_MOCHA.danger,
            DRACULA.danger, MELANGE.danger, TOKYO_NIGHT.danger,
            context.resources.getColor(R.color.danger) -> palette.danger

            // input
            MAKO.input, CATPPUCCIN_MOCHA.input,
            DRACULA.input, MELANGE.input, TOKYO_NIGHT.input,
            context.resources.getColor(R.color.input) -> palette.input

            // disabled
            MAKO.disabled, CATPPUCCIN_MOCHA.disabled,
            DRACULA.disabled, MELANGE.disabled, TOKYO_NIGHT.disabled,
            context.resources.getColor(R.color.header) -> palette.disabled

            else -> null
        }
    }

    private fun resolveDrawableColor(drawable: android.graphics.drawable.Drawable): Int? {
        return if (drawable is android.graphics.drawable.ColorDrawable) drawable.color else null
    }
}
