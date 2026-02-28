package com.rama.mako.managers

import android.content.Context
import android.content.SharedPreferences

class PrefsManager private constructor(context: Context) {

    val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrefsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Clock
    fun isClockVisible(): Boolean = prefs.getBoolean("show_clock", true)
    fun getClockFormat(): String? = prefs.getString("clock_format", "system")
    fun setClockNone() = prefs.edit().putBoolean("show_clock", false).remove("clock_format").apply()
    fun setClockSystem() =
        prefs.edit().putBoolean("show_clock", true).putString("clock_format", "system").apply()

    fun setClock24() =
        prefs.edit().putBoolean("show_clock", true).putString("clock_format", "24").apply()

    fun setClock12() =
        prefs.edit().putBoolean("show_clock", true).putString("clock_format", "12").apply()

    // Checkboxes
    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    fun setBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()

    // Group visibility
    fun isGroupVisible(group: String): Boolean = prefs.getBoolean("group_visibility_$group", true)
    fun setGroupVisible(group: String, visible: Boolean) =
        prefs.edit().putBoolean("group_visibility_$group", visible).apply()

    fun getStringSet(key: String, default: Set<String>): MutableSet<String> {
        return prefs.getStringSet(key, default) ?: default.toMutableSet()
    }

    fun setStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }
}