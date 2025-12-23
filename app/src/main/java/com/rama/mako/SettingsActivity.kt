package com.rama.mako

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.Toast

class SettingsActivity : Activity() {

    private val themes = mapOf(
        "Obsidian" to R.style.Theme_Mako_Obsidian,
        "Clay" to R.style.Theme_Mako_Clay,
        "Emerald" to R.style.Theme_Mako_Emerald,
        "Night" to R.style.Theme_Mako_Night
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply system UI flags
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.view_settings)

        // Adjust for status bar
        findViewById<View>(android.R.id.content).setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(
                v.paddingLeft,
                insets.systemWindowInsetTop,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        // Setup buttons using helper method
        setupButton(R.id.about_button) { startActivity(Intent(this, AboutActivity::class.java)) }
        setupButton(R.id.close_button) { finish() }

        setupButton(R.id.activate_button) {
            openIntent(Intent(Settings.ACTION_HOME_SETTINGS), "Unable to open launcher settings")
        }
        setupButton(R.id.wallpaper_button) {
            openIntent(
                Intent(Intent.ACTION_SET_WALLPAPER),
                "No wallpaper app available"
            )
        }

//        setupThemeBoxes()
    }

    // Helper to bind a click listener
    private fun setupButton(id: Int, action: () -> Unit) {
        findViewById<View>(id).setOnClickListener { action() }
    }

    // Helper to start an intent safely
    private fun openIntent(intent: Intent, errorMsg: String) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

//    private fun setupThemeBoxes() {
//        val themesContainer = findViewById<LinearLayout>(R.id.themes_container)
//
//        val themeColors = mapOf(
//            R.style.Theme_Mako_Obsidian to R.color.bg_theme_obsidian,
//            R.style.Theme_Mako_Clay to R.color.bg_theme_clay,
//            R.style.Theme_Mako_Emerald to R.color.bg_theme_emerald,
//            R.style.Theme_Mako_Night to R.color.bg_theme_night
//        )
//
//        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
//        val currentTheme = prefs.getInt("theme", R.style.Theme_Mako_Obsidian)
////
//        themesContainer.removeAllViews()
//
//        themeColors.forEach { (styleRes, colorRes) ->
//
//            // Outer frame with gray border
//            val frame = FrameLayout(this).apply {
//                layoutParams = LinearLayout.LayoutParams(
//                    resources.getDimensionPixelSize(R.dimen.theme_box_size),
//                    resources.getDimensionPixelSize(R.dimen.theme_box_size)
//                ).apply {
//                    setPadding(8, 8, 8, 8)
//                    marginEnd = resources.getDimensionPixelSize(R.dimen.theme_box_margin)
//                }
//
//                // Use mutate() to avoid shared drawable issues
//                background = resources.getDrawable(R.drawable.bg_theme_box, theme).mutate()
//            }
//
//            // Inner colored view
//            val fill = View(this).apply {
//                layoutParams = FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.MATCH_PARENT
//                )
//                val color = resources.getColor(colorRes, theme)
//                setBackgroundColor(color)
//            }
//
//            frame.addView(fill)
//
//            frame.setOnClickListener {
//                prefs.edit().putInt("theme", styleRes).apply()
//
//                val intent = Intent(this, MainActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//
//                finish()
//            }
//
//            themesContainer.addView(frame)
//        }
//    }
}
