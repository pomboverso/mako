package com.rama.mako.managers

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.rama.mako.R

class HomeBackgroundManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = PrefsManager.getInstance(appContext)
    private val wallpaperManager by lazy { WallpaperManager.getInstance(appContext) }

    fun applyTo(view: View, modeOverride: String? = null) {
        val mode = modeOverride ?: prefs.getHomeBackgroundMode()
        view.background = createBackgroundDrawable(mode)
    }

    fun applyToSettings(view: View, modeOverride: String? = null) {
        applyTo(view, modeOverride)
    }

    fun createWallpaperOverlayDrawable(): Drawable {
        return ColorDrawable(resolveWallpaperScrimColor())
    }

    fun shouldTrackWallpaperChangesForMode(mode: String): Boolean {
        return mode == PrefsManager.BackgroundMode.DYNAMIC && supportsWallpaperReactiveBackground()
    }

    fun getWallpaperSignature(): Int? {
        if (!supportsWallpaperReactiveBackground()) return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null

        return runCatching {
            wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)
        }.getOrNull()
    }

    fun createBackgroundDrawable(mode: String): Drawable {
        return when (mode) {
            PrefsManager.BackgroundMode.WALLPAPER -> ColorDrawable(
                ContextCompat.getColor(
                    appContext,
                    R.color.bg_primary
                )
            )
            PrefsManager.BackgroundMode.DYNAMIC -> ColorDrawable(resolveDynamicSolidColor())
            PrefsManager.BackgroundMode.AMOLED -> ColorDrawable(
                ContextCompat.getColor(
                    appContext,
                    R.color.bg_amoled
                )
            )

            else -> ColorDrawable(ContextCompat.getColor(appContext, R.color.bg_primary))
        }
    }

    private fun resolveDynamicSolidColor(): Int {
        val fallback = ContextCompat.getColor(appContext, R.color.bg_dynamic_fallback)

        resolveSystemDynamicColor()?.let { return darkenForReadability(it) }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return fallback
        }

        val wallpaperColors = runCatching {
            wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        }.getOrNull() ?: return fallback

        val sourceColor = wallpaperColors.primaryColor?.toArgb()
            ?: wallpaperColors.secondaryColor?.toArgb()
            ?: wallpaperColors.tertiaryColor?.toArgb()
            ?: return fallback

        return darkenForReadability(sourceColor)
    }

    private fun resolveWallpaperScrimColor(): Int {
        val fallback = ContextCompat.getColor(appContext, R.color.bg_wallpaper_scrim)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return fallback

        val wallpaperColors = runCatching {
            wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        }.getOrNull() ?: return fallback

        val hints = wallpaperColors.colorHints
        val alpha = when {
            hints and WallpaperColors.HINT_SUPPORTS_DARK_TEXT != 0 -> 0xB8
            hints and WallpaperColors.HINT_SUPPORTS_DARK_THEME != 0 -> 0x7A
            else -> 0x96
        }

        return ColorUtils.setAlphaComponent(Color.BLACK, alpha)
    }

    private fun supportsWallpaperReactiveBackground(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    }

    private fun resolveSystemDynamicColor(): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null

        return runCatching {
            ContextCompat.getColor(appContext, android.R.color.system_accent1_900)
        }.getOrNull()
    }

    private fun darkenForReadability(color: Int): Int {
        var tuned = ColorUtils.blendARGB(color, Color.BLACK, 0.62f)
        var iterations = 0

        while (ColorUtils.calculateLuminance(tuned) > 0.15 && iterations < 5) {
            tuned = ColorUtils.blendARGB(tuned, Color.BLACK, 0.25f)
            iterations++
        }

        return tuned
    }
}