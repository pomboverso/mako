package com.rama.mako.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.rama.mako.CsActivity
import com.rama.mako.R

class AboutActivity : CsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.view_about)

        val root = findViewById<View>(android.R.id.content)
        applyEdgeToEdgePadding(root)
        applyCurrentTheme(root)

        val closeButton = findViewById<View>(R.id.close_button)
        closeButton.setOnClickListener {
            finish()
        }

        val discordButton = findViewById<View>(R.id.discord_button)
        discordButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/zFFupY8PFE"))
            startActivity(intent)
        }

        val version = packageManager.getPackageInfo(packageName, 0).versionCode
        val nameView = findViewById<TextView>(R.id.name_version)
        nameView.text = getString(R.string.app_name) + ' ' + version
    }
}