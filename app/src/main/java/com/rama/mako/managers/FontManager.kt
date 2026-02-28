package com.rama.mako.managers

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object FontManager {
    private var cachedTypeface: Typeface? = null

    fun applyFont(context: Context, root: View) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val useCustom = prefs.getBoolean("use_pixel_font", false)

        val typeface: Typeface? = if (useCustom) {
            if (cachedTypeface == null) {
                cachedTypeface =
                    Typeface.createFromAsset(context.assets, "fonts/jersey25_regular.ttf")
            }
            cachedTypeface
        } else null

        applyRecursively(root, typeface)
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