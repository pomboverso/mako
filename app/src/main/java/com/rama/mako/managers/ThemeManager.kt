package com.rama.mako.managers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
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
        val icon: Int,
        val clock: Int,
    )

    // Mako (default)
    private val MAKO_OFF = Palette(
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
        icon = 0xFFCCCCCC.toInt(),
        clock = 0xFFCCCCCC.toInt(),
    )

    // Mako Forest
    private val MAKO = Palette(
        foreground = 0xFFcbdecd.toInt(),
        bg_1 = 0xFF0e190e.toInt(),
        bg_2 = 0xFF1f2920.toInt(),
        bg_3 = 0xFF2d3b24.toInt(),
        accent_1 = 0xFFABD68E.toInt(),
        accent_2 = 0xFFCDC58B.toInt(),
        accent_3 = 0xFFDCD07C.toInt(),
        disabled = 0xFF888888.toInt(),
        input = 0xFF161f16.toInt(),
        button_1 = 0xFF45995a.toInt(),
        button_2 = 0xFFb8e39d.toInt(),
        danger = 0xFFDC6364.toInt(),
        header = 0xff888888.toInt(),
        icon = 0xFFd4efc3.toInt(),
        clock = 0xFFABD68E.toInt(),
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
        icon = 0xFFCDD6F4.toInt(),
        clock = 0xFFCBA6F7.toInt(),
    )


    // Dracula
    private val DRACULA = Palette(
        foreground = 0xFFF8F8F2.toInt(),
        bg_1 = 0xFF282A36.toInt(),
        bg_2 = 0xFF363849.toInt(),
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
        icon = 0xFFF8F8F2.toInt(),
        clock = 0xFFBD93F9.toInt(),
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
        icon = 0xFFECE1D7.toInt(),
        clock = 0xFFEBC06D.toInt(),
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
        icon = 0xFFC0CAF5.toInt(),
        clock = 0xFF7AA2F7.toInt(),
    )

    fun paletteFor(theme: String, context: android.content.Context? = null): Palette =
        when (theme) {
            PrefsManager.Theme.RAMA -> MAKO
            PrefsManager.Theme.CATPPUCCIN_MOCHA -> CATPPUCCIN_MOCHA
            PrefsManager.Theme.DRACULA -> DRACULA
            PrefsManager.Theme.MELANGE -> MELANGE
            PrefsManager.Theme.TOKYO_NIGHT -> TOKYO_NIGHT
            PrefsManager.Theme.CUSTOM -> if (context != null) buildCustomPalette(context) else MAKO_OFF
            else -> MAKO_OFF
        }

    private fun buildCustomPalette(context: android.content.Context): Palette {
        val prefs = PrefsManager.getInstance(context)
        val base = MAKO_OFF
        fun get(key: String, fallback: Int) = prefs.getCustomThemeColor(key, fallback)
        return Palette(
            foreground = get(PrefsManager.PrefKeys.APP_THEME_FOREGROUND, base.foreground),
            bg_1 = get(PrefsManager.PrefKeys.APP_THEME_BG_1, base.bg_1),
            bg_2 = get(PrefsManager.PrefKeys.APP_THEME_BG_2, base.bg_2),
            bg_3 = get(PrefsManager.PrefKeys.APP_THEME_BG_3, base.bg_3),
            accent_1 = get(PrefsManager.PrefKeys.APP_THEME_ACCENT_1, base.accent_1),
            accent_2 = get(PrefsManager.PrefKeys.APP_THEME_ACCENT_2, base.accent_2),
            accent_3 = get(PrefsManager.PrefKeys.APP_THEME_ACCENT_3, base.accent_3),
            disabled = get(PrefsManager.PrefKeys.APP_THEME_DISABLED, base.disabled),
            input = get(PrefsManager.PrefKeys.APP_THEME_INPUT, base.input),
            button_1 = get(PrefsManager.PrefKeys.APP_THEME_BUTTON_1, base.button_1),
            button_2 = get(PrefsManager.PrefKeys.APP_THEME_BUTTON_2, base.button_2),
            danger = get(PrefsManager.PrefKeys.APP_THEME_DANGER, base.danger),
            header = get(PrefsManager.PrefKeys.APP_THEME_HEADER, base.header),
            icon = get(PrefsManager.PrefKeys.APP_THEME_ICON, base.icon),
            clock = get(PrefsManager.PrefKeys.APP_THEME_CLOCK, base.clock),
        )
    }

    fun applyTheme(context: Context, root: View) {
        val prefs = PrefsManager.getInstance(context)
        val palette = paletteFor(prefs.getTheme(), context)
        val typeface = FontManager.getTypeface(context, prefs.getFontStyle())
        applyRecursively(context, root, palette, typeface)
    }

    private fun applyRecursively(
        context: Context,
        view: View,
        palette: Palette,
        typeface: android.graphics.Typeface?
    ) {
        applyToView(context, view, palette, typeface)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyRecursively(context, view.getChildAt(i), palette, typeface)
            }
        }
    }

    private fun applyToView(
        context: Context,
        view: View,
        palette: Palette,
        typeface: android.graphics.Typeface?
    ) {
        // Font + text color
        if (view is TextView) {
            typeface?.let { view.typeface = it }
            when (view) {
                is RadioButton, is CheckBox -> Unit
                else -> {
                    val mapped = mapColor(context, view.currentTextColor, palette)
                    view.setTextColor(mapped ?: palette.foreground)
                }
            }
        }

        // Icon tint on ImageViews
        if (view is ImageView) {
            val tint = view.imageTintList?.defaultColor
            if (tint != null) {
                val mapped = mapColor(context, tint, palette) ?: palette.icon
                view.imageTintList = android.content.res.ColorStateList.valueOf(mapped)
            }
        }

        // Background
        val currentColor = resolveDrawableColor(view.background ?: return) ?: return
        val mapped = mapColor(context, currentColor, palette) ?: return
        view.setBackgroundColor(mapped)
    }

    /**
     * Maps a color from any palette to the equivalent slot in [palette].
     * This works by comparing the incoming color against all known palette
     * slots across both themes.
     */
    private fun mapColor(context: Context, color: Int, palette: Palette): Int? {
        return when (color) {
            // bg_primary
            MAKO_OFF.bg_1, MAKO.bg_1, CATPPUCCIN_MOCHA.bg_1,
            DRACULA.bg_1, MELANGE.bg_1, TOKYO_NIGHT.bg_1,
            context.resources.getColor(R.color.bg_1) -> palette.bg_1

            // bg_secondary
            MAKO_OFF.bg_2, MAKO.bg_2, CATPPUCCIN_MOCHA.bg_2,
            DRACULA.bg_2, MELANGE.bg_2, TOKYO_NIGHT.bg_2,
            context.resources.getColor(R.color.bg_2) -> palette.bg_2

            // bg_tertiary
            MAKO_OFF.bg_3, MAKO.bg_3, CATPPUCCIN_MOCHA.bg_3,
            DRACULA.bg_3, MELANGE.bg_3, TOKYO_NIGHT.bg_3,
            context.resources.getColor(R.color.bg_3) -> palette.bg_3

            // button_primary
            MAKO_OFF.button_1, MAKO.button_1, CATPPUCCIN_MOCHA.button_1,
            DRACULA.button_1, MELANGE.button_1, TOKYO_NIGHT.button_1,
            context.resources.getColor(R.color.button_1) -> palette.button_1

            // button_secondary
            MAKO_OFF.button_2, MAKO.button_2, CATPPUCCIN_MOCHA.button_2,
            DRACULA.button_2, MELANGE.button_2, TOKYO_NIGHT.button_2,
            context.resources.getColor(R.color.button_2) -> palette.button_2

            // button_danger
            MAKO_OFF.danger, MAKO.danger, CATPPUCCIN_MOCHA.danger,
            DRACULA.danger, MELANGE.danger, TOKYO_NIGHT.danger,
            context.resources.getColor(R.color.danger) -> palette.danger

            // input
            MAKO_OFF.input, MAKO.input, CATPPUCCIN_MOCHA.input,
            DRACULA.input, MELANGE.input, TOKYO_NIGHT.input,
            context.resources.getColor(R.color.input) -> palette.input

            // disabled
            MAKO_OFF.disabled, MAKO.disabled, CATPPUCCIN_MOCHA.disabled,
            DRACULA.disabled, MELANGE.disabled, TOKYO_NIGHT.disabled,
            context.resources.getColor(R.color.disabled) -> palette.disabled

            // header
            MAKO_OFF.header, MAKO.header, CATPPUCCIN_MOCHA.header,
            DRACULA.header, MELANGE.header, TOKYO_NIGHT.header,
            context.resources.getColor(R.color.collapse_header) -> palette.header

            // foreground
            MAKO_OFF.foreground, MAKO.foreground, CATPPUCCIN_MOCHA.foreground,
            DRACULA.foreground, MELANGE.foreground, TOKYO_NIGHT.foreground,
            context.resources.getColor(R.color.foreground) -> palette.foreground

            // icon
            MAKO_OFF.icon, MAKO.icon, CATPPUCCIN_MOCHA.icon,
            DRACULA.icon, MELANGE.icon, TOKYO_NIGHT.icon,
            context.resources.getColor(R.color.icon) -> palette.icon

            // clock
            MAKO_OFF.clock, MAKO.clock, CATPPUCCIN_MOCHA.clock,
            DRACULA.clock, MELANGE.clock, TOKYO_NIGHT.clock,
            context.resources.getColor(R.color.clock) -> palette.clock

            else -> null
        }
    }

    private fun resolveDrawableColor(drawable: android.graphics.drawable.Drawable): Int? {
        return if (drawable is android.graphics.drawable.ColorDrawable) drawable.color else null
    }
}
