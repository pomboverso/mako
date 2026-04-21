package com.rama.mako.activities.settings

import android.view.View
import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.PrefsManager.PrefKeys
import com.rama.mako.widgets.WdCheckbox

class SettingsExtController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs

    fun setup() {
        val checkbox = activity.findViewById<WdCheckbox>(R.id.show_hidden_apps)
        checkbox.visibility = View.VISIBLE

        val isChecked = prefs.getBoolean(PrefKeys.APPS_SHOW_HIDDEN, false)
        checkbox.setChecked(isChecked)

        checkbox.setOnCheckedChangeListener { checked ->
            prefs.setBoolean(PrefKeys.APPS_SHOW_HIDDEN, checked)
        }
    }
}
