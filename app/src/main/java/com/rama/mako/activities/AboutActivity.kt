package com.rama.mako.activities

import android.os.Bundle
import android.view.View
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import com.rama.mako.BaseFullscreenActivity
import com.rama.mako.R

class AboutActivity : BaseFullscreenActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.view_about)

        val root = findViewById<View>(android.R.id.content)
        applyEdgeToEdgePadding(root)

        val repoButton = findViewById<LinearLayout>(R.id.repo_button)
        repoButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/jmiguelrivas/mako")
            )
            startActivity(intent)
        }

        val creatorButton = findViewById<LinearLayout>(R.id.creator_button)
        creatorButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/jmiguelrivas")
            )
            startActivity(intent)
        }

        val closeButton = findViewById<View>(R.id.close_button)
        closeButton.setOnClickListener {
            finish()
        }
    }
}