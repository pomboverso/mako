package com.rama.mako

import android.content.Intent
import android.service.quicksettings.TileService

class Tiles : TileService() {

    override fun onClick() {
        super.onClick()

        val packageName = "org.fossify.clock"

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivityAndCollapse(intent)
    }
}