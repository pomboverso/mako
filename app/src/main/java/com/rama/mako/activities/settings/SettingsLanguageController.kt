package com.rama.mako.activities.settings

import android.widget.RadioGroup
import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.PrefsManager

class SettingsLanguageController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs

    fun setup() {
        val group = activity.findViewById<RadioGroup>(R.id.language_group)

        when (prefs.getAppLanguage()) {
            PrefsManager.AppLanguage.ENGLISH -> group.check(R.id.language_english)
            PrefsManager.AppLanguage.GERMAN -> group.check(R.id.language_german)
            else -> group.check(R.id.language_system)
        }

        group.setOnCheckedChangeListener { _, id ->
            val language = when (id) {
                R.id.language_english -> PrefsManager.AppLanguage.ENGLISH
                R.id.language_german -> PrefsManager.AppLanguage.GERMAN
                else -> PrefsManager.AppLanguage.SYSTEM
            }

            if (language == prefs.getAppLanguage()) {
                return@setOnCheckedChangeListener
            }

            prefs.setAppLanguage(language)
            activity.recreate()
        }
    }
}