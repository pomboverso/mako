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

    companion object {
        @Volatile
        private var INSTANCE: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrefsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    object UI {
        const val SEPARATOR = "------"
        const val UNGROUPED_LABEL = "$SEPARATOR Default"
        const val FAVORITES_LABEL = "$SEPARATOR Favorites"
    }

    object SystemIds {
        val UNGROUPED = IdUtils.toBase36Fixed(0)
        val FAVORITES = IdUtils.toBase36Fixed(1)
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
        const val ROBOTO_SLAB = "roboto-slab"
        const val JERSEY_25 = "jersey-25"
    }

    object ClockFormat {
        const val NONE = "none"
        const val DEFAULT = "default"
        const val HOUR_12 = "12-hour"
        const val HOUR_24 = "24-hour"
    }

    fun initPrefs() {
        val ids = prefs.getStringSet(PrefKeys.GROUPS_IDS, null)

        if (ids.isNullOrEmpty()) {
            val defaultIds = setOf(
                SystemIds.UNGROUPED,
                SystemIds.FAVORITES
            )

            prefs.edit()
                .putStringSet(PrefKeys.GROUPS_IDS, defaultIds)

                .putString(PrefKeys.GROUP_LABEL(SystemIds.UNGROUPED), UI.UNGROUPED_LABEL)
                .putBoolean(PrefKeys.GROUP_VISIBLE(SystemIds.UNGROUPED), true)
                .putBoolean(PrefKeys.GROUP_EXPANDED(SystemIds.UNGROUPED), true)

                .putString(PrefKeys.GROUP_LABEL(SystemIds.FAVORITES), UI.FAVORITES_LABEL)
                .putBoolean(PrefKeys.GROUP_VISIBLE(SystemIds.FAVORITES), true)
                .putBoolean(PrefKeys.GROUP_EXPANDED(SystemIds.FAVORITES), true)

                .putString(PrefKeys.FONT_STYLE, FontStyle.JERSEY_25)

                .putString(PrefKeys.CLOCK_FORMAT, ClockFormat.HOUR_24)
                .putString(PrefKeys.CLOCK_APP, "")

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

    //    fun getAppGroupId(pkg: String): String? =
//        getString(PrefKeys.APP_GROUP_ID(pkg), "").takeIf { it.isNotEmpty() }
    fun getAppGroupId(pkg: String): String {
        return prefs.getString(PrefKeys.APP_GROUP_ID(pkg), null)
            ?: SystemIds.UNGROUPED.also {
                prefs.edit().putString(PrefKeys.APP_GROUP_ID(pkg), it).apply()
            }
    }

    fun setAppGroupId(pkg: String, groupId: String?) {
        if (groupId != null) {
            setString(PrefKeys.APP_GROUP_ID(pkg), groupId)
        } else {
            setString(PrefKeys.APP_GROUP_ID(pkg), "")
        }
    }

    // GROUPS

    fun getGroupIds(): Set<String> =
        prefs.getStringSet(PrefKeys.GROUPS_IDS, emptySet()) ?: emptySet()

    fun setGroupIds(ids: Set<String>) =
        prefs.edit().putStringSet(PrefKeys.GROUPS_IDS, ids).apply()

    fun getGroupLabel(id: String): String =
        prefs.getString(PrefKeys.GROUP_LABEL(id), "") ?: ""

    fun setGroupLabel(id: String, value: String) =
        prefs.edit().putString(PrefKeys.GROUP_LABEL(id), value).apply()

    fun isGroupVisible(id: String): Boolean =
        prefs.getBoolean(PrefKeys.GROUP_VISIBLE(id), false)

    fun setGroupVisible(id: String, value: Boolean) =
        prefs.edit().putBoolean(PrefKeys.GROUP_VISIBLE(id), value).apply()

    fun isGroupExpanded(id: String): Boolean =
        prefs.getBoolean(PrefKeys.GROUP_EXPANDED(id), false)

    fun setGroupExpanded(id: String, value: Boolean) =
        prefs.edit().putBoolean(PrefKeys.GROUP_EXPANDED(id), value).apply()

    // SETTINGS - APPS

    fun isSearchVisible(): Boolean =
        prefs.getBoolean(PrefKeys.APPS_SEARCH, false)

    fun hasIconsVisible(): Boolean =
        prefs.getBoolean(PrefKeys.APPS_ICONS, false)

    // SETTINGS - GROUPS

    fun hasGroupHeaders(): Boolean =
        prefs.getBoolean(PrefKeys.GROUPS_HEADERS, false)

    fun hasCollapsibleGroups(): Boolean =
        prefs.getBoolean(PrefKeys.GROUPS_COLLAPSIBLE, false)

    // SETTINGS - CLOCK

    fun getClockFormat(): String =
        prefs.getString(PrefKeys.CLOCK_FORMAT, "") ?: ""

    fun setClockFormat(format: String) =
        prefs.edit().putString(PrefKeys.CLOCK_FORMAT, format).apply()

    fun getClockApp(): String =
        prefs.getString(PrefKeys.CLOCK_APP, "") ?: ""

    fun setClockApp(appId: String) =
        prefs.edit().putString(PrefKeys.CLOCK_APP, appId).apply()

    // SETTINGS - DATE

    fun isDateVisible(): Boolean =
        prefs.getBoolean(PrefKeys.DATE_VISIBLE, false)

    fun isYearDayVisible(): Boolean =
        prefs.getBoolean(PrefKeys.DATE_YEAR_DAY, false)

    // SETTINGS - BATTERY

    fun isBatteryVisible(): Boolean =
        prefs.getBoolean(PrefKeys.BATTERY_VISIBLE, false)

    fun isBatteryTemperatureVisible(): Boolean =
        prefs.getBoolean(PrefKeys.BATTERY_TEMPERATURE, false)

    fun isBatteryChargeStatusVisible(): Boolean =
        prefs.getBoolean(PrefKeys.BATTERY_CHARGE_STATUS, false)

    // SETTINGS - FONT

    fun getFontStyle(): String =
        prefs.getString(PrefKeys.FONT_STYLE, "") ?: ""

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

                is Set<*> -> {
                    val array = org.json.JSONArray()
                    value.forEach { item ->
                        array.put(item)
                    }
                    json.put(key, array)
                }

                else -> json.put(key, value.toString())
            }
        }

        return json
    }

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

    fun clearAllPrefs(): Result<Unit> {
        return try {
            prefs.edit().clear().commit()
            initPrefs()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}