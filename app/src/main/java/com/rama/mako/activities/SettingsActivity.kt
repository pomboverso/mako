package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import com.rama.mako.CsActivity
import com.rama.mako.R
import com.rama.mako.managers.AppsProvider
import com.rama.mako.managers.GroupsManager
import com.rama.mako.managers.IconManager
import com.rama.mako.managers.PrefsManager
import com.rama.mako.activities.settings.SettingsAppearanceController
import com.rama.mako.activities.settings.SettingsBasicController
import com.rama.mako.activities.settings.SettingsCheckboxController
import com.rama.mako.activities.settings.SettingsClockController
import com.rama.mako.activities.settings.SettingsGroupsController
import com.rama.mako.activities.settings.SettingsIconsController

class SettingsActivity : CsActivity() {

    val prefs by lazy { PrefsManager.getInstance(this) }
    lateinit var appsProvider: AppsProvider
    lateinit var iconManager: IconManager
    lateinit var groupsManager: GroupsManager
    private lateinit var clockController: SettingsClockController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_settings)

        applyEdgeToEdgePadding(findViewById(android.R.id.content))

        appsProvider = AppsProvider(this)
        iconManager = IconManager(this, appsProvider)
        groupsManager = GroupsManager(this, appsProvider)

        // each module handles itself
        clockController = SettingsClockController(this).also { it.setup() }

        SettingsBasicController(this).setup()
        SettingsAppearanceController(this).setup()
        SettingsIconsController(this).setup()
        SettingsCheckboxController(this).setup()
        SettingsGroupsController(this).setup()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        clockController.onActivityResult(requestCode, resultCode, data)
    }

}