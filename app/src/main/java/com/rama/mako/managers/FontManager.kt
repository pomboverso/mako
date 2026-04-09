package com.rama.mako.managers

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object FontManager {

    private val cache = mutableMapOf<String, Typeface?>()

    fun applyFont(context: Context, root: View) {
        val prefs = PrefsManager.getInstance(context)
        val fontStyle = prefs.getFontStyle() ?: "system"

        val typeface = getTypeface(context, fontStyle)

        applyRecursively(root, typeface)
    }

    private fun getTypeface(context: Context, style: String): Typeface? {

        if (style == PrefsManager.FontStyle.DEFAULT) return null

        if (cache.containsKey(style)) {
            return cache[style]
        }

        val tf = when (style) {
            PrefsManager.FontStyle.QUICKSAND ->
                Typeface.createFromAsset(context.assets, "fonts/quicksand_bold.ttf")

            PrefsManager.FontStyle.MONTSERRAT ->
                Typeface.createFromAsset(context.assets, "fonts/montserrat_medium.ttf")

            PrefsManager.FontStyle.ROBOTO_SLAB ->
                Typeface.createFromAsset(context.assets, "fonts/robotoslab_semibold.ttf")

            PrefsManager.FontStyle.JERSEY_25 ->
                Typeface.createFromAsset(context.assets, "fonts/jersey25_regular.otf")

            else -> null
        }

        cache[style] = tf
        return tf
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