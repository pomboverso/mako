package com.rama.mako.managers

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rama.mako.R
import java.io.File

object FontManager {

    private val cache = mutableMapOf<String, Typeface?>()

    fun applyFont(context: Context, root: View) {
        val prefs = PrefsManager.getInstance(context)
        val fontStyle = prefs.getFontStyle() ?: "system"

        root.findViewById<View?>(R.id.custom_font_container)?.visibility =
            if (fontStyle == PrefsManager.FontStyle.CUSTOM) {
                View.VISIBLE
            } else {
                View.GONE
            }

        val typeface = getTypeface(context, fontStyle)

        applyRecursively(root, typeface)
    }

    fun getTypeface(context: Context, style: String): Typeface? {

        if (style == PrefsManager.FontStyle.DEFAULT) return null

        // Custom font: always reload from path (don't cache by style key alone)
        if (style == PrefsManager.FontStyle.CUSTOM) {
            val path = PrefsManager.getInstance(context).getCustomFontPath()
            if (path.isBlank()) return null
            val cacheKey = "custom:$path"
            if (cache.containsKey(cacheKey)) return cache[cacheKey]
            val tf = runCatching { Typeface.createFromFile(File(path)) }.getOrNull()
            cache[cacheKey] = tf
            return tf
        }

        if (cache.containsKey(style)) {
            return cache[style]
        }

        val tf = when (style) {
            PrefsManager.FontStyle.JERSEY_25 ->
                Typeface.createFromAsset(context.assets, "fonts/jersey25_regular.otf")

            else -> null
        }

        cache[style] = tf
        return tf
    }

    /** Call this after saving a new custom font so the old cached entry is evicted. */
    fun clearCustomCache() {
        cache.keys.filter { it.startsWith("custom:") }.forEach { cache.remove(it) }
    }

    private fun applyRecursively(view: View, typeface: Typeface?) {

        if (view is TextView) {
            view.typeface = typeface ?: Typeface.DEFAULT
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyRecursively(view.getChildAt(i), typeface)
            }
        }
    }
}
