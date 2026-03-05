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

    fun isSearchVisible(): Boolean = prefs.getBoolean("show_search", true)
    fun isClockVisible(): Boolean = prefs.getBoolean("show_clock", true)
    fun isBatteryVisible(): Boolean = prefs.getBoolean("show_battery", true)
    fun isBatteryTemperatureVisible(): Boolean = prefs.getBoolean("show_battery_temperature", true)
    fun isBatteryChargeStatusVisible(): Boolean =
        prefs.getBoolean("show_battery_charge_status", true)

    fun isDateVisible(): Boolean = prefs.getBoolean("show_date", true)
    fun isYearDayVisible(): Boolean = prefs.getBoolean("show_year_day", true)
    fun isGroupVisible(group: String): Boolean = prefs.getBoolean("group_visibility_$group", true)

    // Fonts
    fun getFontStyle(): String? = prefs.getString("font_style", "system")
    fun setFontSystem() =
        prefs.edit().putString("font_style", "system").apply()

    fun setFontQuicksand() =
        prefs.edit().putString("font_style", "quicksand").apply()

    fun setFontMontserrat() =
        prefs.edit().putString("font_style", "montserrat").apply()

    fun setFontRobotoslab() =
        prefs.edit().putString("font_style", "robotoslab").apply()

    fun setFontJersey() =
        prefs.edit().putString("font_style", "jersey").apply()

    // Clock
    fun getClockFormat(): String? = prefs.getString("clock_format", "system")
    fun setClockNone() = prefs.edit().putBoolean("show_clock", false).remove("clock_format").apply()
    fun setClockSystem() =
        prefs.edit().putBoolean("show_clock", true).putString("clock_format", "system").apply()

    fun setClock24() =
        prefs.edit().putBoolean("show_clock", true).putString("clock_format", "24").apply()

    fun setClock12() =
        prefs.edit().putBoolean("show_clock", true).putString("clock_format", "12").apply()

    // Groups
    fun setGroupVisible(group: String, visible: Boolean) =
        prefs.edit().putBoolean("group_visibility_$group", visible).apply()

    // General
    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    fun setBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun getStringSet(key: String, default: Set<String>): MutableSet<String> {
        return prefs.getStringSet(key, default) ?: default.toMutableSet()
    }

    fun setStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }
}