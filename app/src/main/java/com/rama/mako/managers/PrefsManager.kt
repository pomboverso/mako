package com.rama.mako.managers

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.rama.mako.utils.IdUtils
import org.json.JSONObject

class PrefsManager private constructor(context: Context) {

    val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val ungroupedId = IdUtils.toBase36Fixed(0)
    val favoritesId = IdUtils.toBase36Fixed(1)

    companion object {
        @Volatile
        private var INSTANCE: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrefsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    object PrefKeys {
        const val APPS_SEARCH = "apps:search"
        const val APPS_ICONS = "apps:icons"
        const val GROUPS_IDS = "groups:ids"
        const val GROUPS_HEADERS = "groups:headers"
        const val GROUPS_COLLAPSIBLE = "groups:collapsible"
        const val DATE_VISIBLE = "date:visible"
        const val DATE_YEAR_DAY = "date:year_day"
        const val BATTERY_VISIBLE = "battery:visible"
        const val BATTERY_TEMPERATURE = "battery:temperature"
        const val BATTERY_CHARGE_STATUS = "battery:charge_status"
        const val CLOCK_FORMAT = "clock:format"
        const val CLOCK_APP = "clock:app"
        const val FONT_STYLE = "font:style"

        fun APP_GROUP_ID(pkg: String) = "app:$pkg:group_id"
        fun GROUP_LABEL(id: String) = "group:$id:label"
        fun GROUP_VISIBLE(id: String) = "group:$id:visible"
        fun GROUP_EXPANDED(id: String) = "group:$id:expanded"
    }

    object FontStyle {
        const val DEFAULT = "default"
        const val MONTSERRAT = "montserrat"
        const val QUICKSAND = "quicksand"
        const val ROBOTO_SLAB = "robotoslab"
        const val JERSEY_25 = "jersey"
    }

    object ClockFormat {
        const val NONE = "none"
        const val DEFAULT = "default"
        const val HOUR_12 = "12-hour"
        const val HOUR_24 = "24-hour"
    }

    fun ensureDefaultGroups() {
        val ids = prefs.getStringSet(PrefKeys.GROUPS_IDS, null)

        if (ids.isNullOrEmpty()) {
            val defaultIds = setOf(
                ungroupedId,
                favoritesId
            )
            val separator = "------"

            prefs.edit()
                .putStringSet(PrefKeys.GROUPS_IDS, defaultIds)

                .putString(PrefKeys.GROUP_LABEL(ungroupedId), "$separator Default")
                .putBoolean(PrefKeys.GROUP_VISIBLE(ungroupedId), true)
                .putBoolean(PrefKeys.GROUP_EXPANDED(ungroupedId), true)

                .putString(PrefKeys.GROUP_LABEL(favoritesId), "$separator Favorites")
                .putBoolean(PrefKeys.GROUP_VISIBLE(favoritesId), true)
                .putBoolean(PrefKeys.GROUP_EXPANDED(favoritesId), true)

                .putString(PrefKeys.FONT_STYLE, FontStyle.JERSEY_25)
                .putString(PrefKeys.CLOCK_FORMAT, ClockFormat.HOUR_24)

                .putBoolean(PrefKeys.APPS_ICONS, false)
                .putBoolean(PrefKeys.APPS_SEARCH, false)

                .putBoolean(PrefKeys.BATTERY_VISIBLE, true)
                .putBoolean(PrefKeys.BATTERY_TEMPERATURE, true)
                .putBoolean(PrefKeys.BATTERY_CHARGE_STATUS, false)

                .putBoolean(PrefKeys.DATE_VISIBLE, true)
                .putBoolean(PrefKeys.DATE_YEAR_DAY, true)

                .putBoolean(PrefKeys.GROUPS_HEADERS, true)
                .putBoolean(PrefKeys.GROUPS_COLLAPSIBLE, true)
                .apply()
        }
    }

    // --- GROUP HELPERS ---

    fun addGroupId(id: String) {
        val updated = getGroupIds().toMutableSet()
        updated.add(id)
        setGroupIds(updated)
    }

    fun removeGroupId(id: String) {
        val updated = getGroupIds().toMutableSet()
        updated.remove(id)
        setGroupIds(updated)
    }

    // --- APP GROUP MAPPING ---

    fun getAppGroupId(pkg: String): String? =
        getString(PrefKeys.APP_GROUP_ID(pkg), "").takeIf { it.isNotEmpty() }

    fun setAppGroupId(pkg: String, groupId: String?) {
        if (groupId != null) {
            setString(PrefKeys.APP_GROUP_ID(pkg), groupId)
        } else {
            setString(PrefKeys.APP_GROUP_ID(pkg), "")
        }
    }

    // GROUPS

    fun getGroupIds(): Set<String> =
        prefs.getStringSet(PrefKeys.GROUPS_IDS, setOf("0")) ?: setOf("0")

    fun setGroupIds(ids: Set<String>) =
        prefs.edit().putStringSet(PrefKeys.GROUPS_IDS, ids).apply()

    fun getGroupLabel(id: String): String =
        prefs.getString(PrefKeys.GROUP_LABEL(id), "") ?: ""

    fun setGroupLabel(id: String, value: String) =
        prefs.edit().putString(PrefKeys.GROUP_LABEL(id), value).apply()

    fun isGroupVisible(id: String): Boolean =
        prefs.getBoolean(PrefKeys.GROUP_VISIBLE(id), true)

    fun setGroupVisible(id: String, value: Boolean) =
        prefs.edit().putBoolean(PrefKeys.GROUP_VISIBLE(id), value).apply()

    fun isGroupExpanded(id: String): Boolean =
        prefs.getBoolean(PrefKeys.GROUP_EXPANDED(id), true)

    fun setGroupExpanded(id: String, value: Boolean) =
        prefs.edit().putBoolean(PrefKeys.GROUP_EXPANDED(id), value).apply()

    // SETTINGS - APPS

    fun isSearchVisible(): Boolean =
        prefs.getBoolean(PrefKeys.APPS_SEARCH, true)

    fun hasIconsVisible(): Boolean =
        prefs.getBoolean(PrefKeys.APPS_ICONS, true)

    // SETTINGS - GROUPS

    fun hasGroupHeaders(): Boolean =
        prefs.getBoolean(PrefKeys.GROUPS_HEADERS, true)

    fun hasCollapsibleGroups(): Boolean =
        prefs.getBoolean(PrefKeys.GROUPS_COLLAPSIBLE, true)

    // SETTINGS - CLOCK

    fun getClockFormat(): String =
        prefs.getString(PrefKeys.CLOCK_FORMAT, ClockFormat.DEFAULT) ?: ClockFormat.DEFAULT

    fun setClockFormat(format: String) =
        prefs.edit().putString(PrefKeys.CLOCK_FORMAT, format).apply()

    fun getClockApp(): String =
        prefs.getString(PrefKeys.CLOCK_APP, "org.fossify.clock") ?: "org.fossify.clock"

    fun setClockApp(appId: String) =
        prefs.edit().putString(PrefKeys.CLOCK_APP, appId).apply()

    // SETTINGS - DATE

    fun isDateVisible(): Boolean =
        prefs.getBoolean(PrefKeys.DATE_VISIBLE, true)

    fun isYearDayVisible(): Boolean =
        prefs.getBoolean(PrefKeys.DATE_YEAR_DAY, true)

    // SETTINGS - BATTERY

    fun isBatteryVisible(): Boolean =
        prefs.getBoolean(PrefKeys.BATTERY_VISIBLE, true)

    fun isBatteryTemperatureVisible(): Boolean =
        prefs.getBoolean(PrefKeys.BATTERY_TEMPERATURE, true)

    fun isBatteryChargeStatusVisible(): Boolean =
        prefs.getBoolean(PrefKeys.BATTERY_CHARGE_STATUS, true)

    // SETTINGS - FONT

    fun getFontStyle(): String =
        prefs.getString(PrefKeys.FONT_STYLE, FontStyle.DEFAULT) ?: FontStyle.DEFAULT

    fun setFontStyle(style: String) =
        prefs.edit().putString(PrefKeys.FONT_STYLE, style).apply()

    // GENERIC HELPERS

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    fun setBoolean(key: String, value: Boolean) =
        prefs.edit().putBoolean(key, value).apply()

    fun getString(key: String, defaultValue: String = ""): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    fun setString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()

    // Export to SAF (user picked location)

    fun exportToUri(context: Context, uri: Uri): Boolean {
        return try {
            val json = buildExportJson()

            context.contentResolver.openOutputStream(uri)?.use {
                it.write(json.toString(2).toByteArray())
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Core builder

    private fun buildExportJson(): JSONObject {
        val json = JSONObject()

        val sortedEntries = prefs.all.entries
            .sortedBy { it.key }

        sortedEntries.forEach { (key, value) ->
            Log.d("mako-export", "$key = $value")

            when (value) {
                is Boolean -> json.put(key, value)
                is Int -> json.put(key, value)
                is Long -> json.put(key, value)
                is Float -> json.put(key, value)
                is String -> json.put(key, value)
//                is Set<*> -> json.put(key, value.toList().sorted()) // optional: sort sets too
                else -> json.put(key, value.toString())
            }
        }

        return json
    }

    fun clearAllPrefs() {
        prefs.edit().clear().apply()
    }
}