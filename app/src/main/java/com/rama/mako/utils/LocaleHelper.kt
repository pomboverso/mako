package com.rama.mako.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.rama.mako.managers.PrefsManager
import java.util.Locale

object LocaleHelper {

    fun wrapContext(base: Context): Context {
        val prefs = PrefsManager.getInstance(base)
        val systemLocale = getCurrentLocale(base.resources.configuration)
        val languageCode = resolveLanguageCode(prefs.getAppLanguage(), systemLocale)
        val targetLocale = Locale.forLanguageTag(languageCode)

        val currentLocale = getCurrentLocale(base.resources.configuration)
        if (currentLocale.language == targetLocale.language) {
            return base
        }

        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(targetLocale)
        return base.createConfigurationContext(configuration)
    }

    private fun resolveLanguageCode(selectedLanguage: String, systemLocale: Locale): String {
        return when (selectedLanguage) {
            PrefsManager.AppLanguage.ENGLISH -> PrefsManager.AppLanguage.ENGLISH
            PrefsManager.AppLanguage.GERMAN -> PrefsManager.AppLanguage.GERMAN
            else -> {
                if (systemLocale.language == PrefsManager.AppLanguage.GERMAN) {
                    PrefsManager.AppLanguage.GERMAN
                } else {
                    PrefsManager.AppLanguage.ENGLISH
                }
            }
        }
    }

    private fun getCurrentLocale(configuration: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            configuration.locale
        }
    }
}