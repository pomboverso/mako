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

    // APPS

    fun getAppIds(): Set<String> =
        prefs.getStringSet("apps:ids", emptySet()) ?: emptySet()

    fun setAppIds(ids: Set<String>) =
        prefs.edit().putStringSet("apps:ids", ids).apply()

    fun getAppLabel(id: String): String =
        prefs.getString("app:$id:label", "") ?: ""

    fun setAppLabel(id: String, value: String) =
        prefs.edit().putString("app:$id:label", value).apply()

    fun getAppGroupId(id: String): Int =
        prefs.getInt("app:$id:group_id", 0)

    fun setAppGroupId(id: String, groupId: Int) =
        prefs.edit().putInt("app:$id:group_id", groupId).apply()

    // GROUPS

    fun getGroupIds(): Set<String> =
        prefs.getStringSet("groups:ids", setOf("0")) ?: setOf("0")

    fun setGroupIds(ids: Set<String>) =
        prefs.edit().putStringSet("groups:ids", ids).apply()

    fun getGroupLabel(id: String): String =
        prefs.getString("group:$id:label", "") ?: ""

    fun setGroupLabel(id: String, value: String) =
        prefs.edit().putString("group:$id:label", value).apply()

    fun isGroupVisible(id: String): Boolean =
        prefs.getBoolean("group:$id:visible", true)

    fun setGroupVisible(id: String, value: Boolean) =
        prefs.edit().putBoolean("group:$id:visible", value).apply()

    fun isGroupExpanded(id: String): Boolean =
        prefs.getBoolean("group:$id:expanded", true)

    fun setGroupExpanded(id: String, value: Boolean) =
        prefs.edit().putBoolean("group:$id:expanded", value).apply()

    // SETTINGS - APPS

    fun isSearchVisible(): Boolean =
        prefs.getBoolean("settings:apps:search", true)

    fun setSearchVisible(value: Boolean) =
        prefs.edit().putBoolean("settings:apps:search", value).apply()

    fun hasIconsVisible(): Boolean =
        prefs.getBoolean("settings:apps:icons", true)

    fun setIconsVisible(value: Boolean) =
        prefs.edit().putBoolean("settings:apps:icons", value).apply()

    // SETTINGS - GROUPS

    fun hasGroupHeaders(): Boolean =
        prefs.getBoolean("settings:groups:headers", true)

    fun hasCollapsibleGroups(): Boolean =
        prefs.getBoolean("settings:groups:collapsible", true)

    // SETTINGS - CLOCK

    fun getClockFormat(): String =
        prefs.getString("settings:clock:format", "24-hours") ?: "24-hours"

    fun setClockFormat(format: String) =
        prefs.edit().putString("settings:clock:format", format).apply()

    // SETTINGS - DATE

    fun isDateVisible(): Boolean =
        prefs.getBoolean("settings:date:visible", true)

    fun setDateVisible(value: Boolean) =
        prefs.edit().putBoolean("settings:date:visible", value).apply()

    fun isYearDayVisible(): Boolean =
        prefs.getBoolean("settings:date:year_day", true)

    // SETTINGS - BATTERY

    fun isBatteryVisible(): Boolean =
        prefs.getBoolean("settings:battery:visible", true)

    fun isBatteryTemperatureVisible(): Boolean =
        prefs.getBoolean("settings:battery:temperature", true)

    fun isBatteryChargeStatusVisible(): Boolean =
        prefs.getBoolean("settings:battery:charge_status", true)

    // SETTINGS - FONT

    fun getFontStyle(): String =
        prefs.getString("settings:font:style", "system") ?: "system"

    fun setFontStyle(style: String) =
        prefs.edit().putString("settings:font:style", style).apply()

    fun setFontSystem() = setFontStyle("system")
    fun setFontQuicksand() = setFontStyle("quicksand")
    fun setFontMontserrat() = setFontStyle("montserrat")
    fun setFontRobotoslab() = setFontStyle("robotoslab")
    fun setFontJersey() = setFontStyle("jersey")

    // GENERIC HELPERS

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    fun setBoolean(key: String, value: Boolean) =
        prefs.edit().putBoolean(key, value).apply()

    fun getString(key: String, defaultValue: String = ""): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    fun setString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()

    fun getStringSet(key: String, default: Set<String>): MutableSet<String> =
        prefs.getStringSet(key, default) ?: default.toMutableSet()

    fun setStringSet(key: String, value: Set<String>) =
        prefs.edit().putStringSet(key, value).apply()
}