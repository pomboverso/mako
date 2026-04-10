package com.rama.mako.managers

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.UserHandle
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
        const val APPS_ICON_SOURCE = "apps:icon_source"
        const val APPS_ICON_PACK_PACKAGE = "apps:icon_pack_package"
        const val GROUPS_IDS = "groups:ids"
        const val GROUPS_HEADERS = "groups:headers"
        const val GROUPS_COLLAPSIBLE = "groups:collapsible"
        const val DATE_VISIBLE = "date:visible"
        const val DATE_YEAR_DAY = "date:year_day"
        const val BATTERY_VISIBLE = "battery:visible"
        const val BATTERY_TEMPERATURE = "battery:temperature"
        const val TEMPERATURE_FORMAT = "temperature:format"
        const val BATTERY_CHARGE_STATUS = "battery:charge_status"
        const val CLOCK_FORMAT = "clock:format"
        const val CLOCK_APP = "clock:app"
        const val FONT_STYLE = "font:style"
        const val MIGRATION_ICON_SOURCE_RADIO = "migration:icon_source_radio"

        const val SETTINGS_SECTION_CLOCK = "settings:section:clock"
        const val SETTINGS_SECTION_FONTS = "settings:section:fonts"
        const val SETTINGS_SECTION_BATTERY = "settings:section:battery"
        const val SETTINGS_SECTION_TEMPERATURE = "settings:section:temperature"
        const val SETTINGS_SECTION_DATE = "settings:section:date"
        const val SETTINGS_SECTION_ICONS = "settings:section:icons"
        const val SETTINGS_SECTION_GROUPS = "settings:section:groups"
        const val SETTINGS_SECTION_SEARCH = "settings:section:search"
        const val SETTINGS_SECTION_SYSTEM = "settings:section:system"
        const val SETTINGS_SECTION_DATA = "settings:section:data"

        fun appKey(pkg: String, userHandle: UserHandle): String {
            val userId = userHandle.hashCode()
            return if (userId == 0) "app:$pkg" else "app:$pkg:profile_$userId"
        }

        fun APP_GROUP_ID(pkg: String, userHandle: UserHandle) =
            "${appKey(pkg, userHandle)}:group_id"

        fun APP_CUSTOM_LABEL(pkg: String, userHandle: UserHandle) =
            "${appKey(pkg, userHandle)}:custom_label"

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

    object IconSource {
        const val NONE = "none"
        const val SYSTEM = "system"
        const val MONOCHROME = "monochrome"
        const val ICON_PACK = "icon_pack"
    }

    object TemperatureFormat {
        const val DEFAULT = "default"
        const val CELSIUS = "celsius"
        const val FAHRENHEIT = "fahrenheit"
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
                .putString(PrefKeys.APPS_ICON_SOURCE, IconSource.NONE)
                .putString(PrefKeys.APPS_ICON_PACK_PACKAGE, "")

                .putBoolean(PrefKeys.BATTERY_VISIBLE, true)
                .putBoolean(PrefKeys.BATTERY_TEMPERATURE, true)
                .putString(PrefKeys.TEMPERATURE_FORMAT, TemperatureFormat.DEFAULT)
                .putBoolean(PrefKeys.BATTERY_CHARGE_STATUS, false)

                .putBoolean(PrefKeys.DATE_VISIBLE, true)
                .putBoolean(PrefKeys.DATE_YEAR_DAY, true)

                .putBoolean(PrefKeys.GROUPS_HEADERS, true)
                .putBoolean(PrefKeys.GROUPS_COLLAPSIBLE, true)

                .putBoolean(PrefKeys.SETTINGS_SECTION_CLOCK, true)
                .putBoolean(PrefKeys.SETTINGS_SECTION_TEMPERATURE, true)
                .putBoolean(PrefKeys.SETTINGS_SECTION_DATE, true)
                .putBoolean(PrefKeys.SETTINGS_SECTION_BATTERY, true)
                .putBoolean(PrefKeys.SETTINGS_SECTION_FONTS, true)
                .putBoolean(PrefKeys.SETTINGS_SECTION_ICONS, true)
                .putBoolean(PrefKeys.SETTINGS_SECTION_GROUPS, true)
                .putBoolean(PrefKeys.SETTINGS_SECTION_SEARCH, true)
                .putBoolean(PrefKeys.SETTINGS_SECTION_SYSTEM, true)
                .putBoolean(PrefKeys.SETTINGS_SECTION_DATA, true)

                .apply()
        }

        migrateLegacyPrefs()
    }

    private fun migrateLegacyPrefs() {
        val editor = prefs.edit()
        var hasChanges = false

        if (!prefs.getBoolean(PrefKeys.MIGRATION_ICON_SOURCE_RADIO, false)) {
            val iconsEnabled = prefs.getBoolean(PrefKeys.APPS_ICONS, false)
            val currentSource = prefs.getString(PrefKeys.APPS_ICON_SOURCE, IconSource.SYSTEM)

            val normalizedSource = when (currentSource) {
                IconSource.NONE -> IconSource.NONE
                IconSource.MONOCHROME -> IconSource.MONOCHROME
                IconSource.ICON_PACK -> IconSource.ICON_PACK
                else -> IconSource.SYSTEM
            }

            val migratedSource = if (iconsEnabled) normalizedSource else IconSource.NONE

            editor.putString(PrefKeys.APPS_ICON_SOURCE, migratedSource)
            editor.putBoolean(PrefKeys.MIGRATION_ICON_SOURCE_RADIO, true)
            hasChanges = true
        }

        if (hasChanges) {
            editor.apply()
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

    fun getAppGroupId(pkg: String, userHandle: UserHandle): String {
        return prefs.getString(PrefKeys.APP_GROUP_ID(pkg, userHandle), null)
            ?: SystemIds.UNGROUPED.also {
                prefs.edit().putString(PrefKeys.APP_GROUP_ID(pkg, userHandle), it).apply()
            }
    }

    fun setAppGroupId(pkg: String, userHandle: UserHandle, groupId: String?) {
        val key = PrefKeys.APP_GROUP_ID(pkg, userHandle)
        if (groupId != null) {
            prefs.edit().putString(key, groupId).apply()
        } else {
            prefs.edit().remove(key).apply()
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
        getIconSource() != IconSource.NONE

    fun getIconSource(): String {
        return when (prefs.getString(PrefKeys.APPS_ICON_SOURCE, IconSource.NONE)) {
            IconSource.NONE -> IconSource.NONE
            IconSource.MONOCHROME -> IconSource.MONOCHROME
            IconSource.ICON_PACK -> IconSource.ICON_PACK
            IconSource.SYSTEM -> IconSource.SYSTEM
            else -> IconSource.NONE
        }
    }

    fun setIconSource(source: String) {
        val normalized = when (source) {
            IconSource.NONE -> IconSource.NONE
            IconSource.MONOCHROME -> IconSource.MONOCHROME
            IconSource.ICON_PACK -> IconSource.ICON_PACK
            else -> IconSource.SYSTEM
        }
        prefs.edit().putString(PrefKeys.APPS_ICON_SOURCE, normalized).apply()
    }

    fun getIconPackPackage(): String =
        prefs.getString(PrefKeys.APPS_ICON_PACK_PACKAGE, "") ?: ""

    fun setIconPackPackage(packageName: String) =
        prefs.edit().putString(PrefKeys.APPS_ICON_PACK_PACKAGE, packageName).apply()

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

    fun getTemperatureFormat(): String {
        return when (prefs.getString(PrefKeys.TEMPERATURE_FORMAT, TemperatureFormat.DEFAULT)) {
            TemperatureFormat.CELSIUS -> TemperatureFormat.CELSIUS
            TemperatureFormat.FAHRENHEIT -> TemperatureFormat.FAHRENHEIT
            else -> TemperatureFormat.DEFAULT
        }
    }

    fun setTemperatureFormat(format: String) {
        val normalized = when (format) {
            TemperatureFormat.CELSIUS -> TemperatureFormat.CELSIUS
            TemperatureFormat.FAHRENHEIT -> TemperatureFormat.FAHRENHEIT
            else -> TemperatureFormat.DEFAULT
        }
        prefs.edit().putString(PrefKeys.TEMPERATURE_FORMAT, normalized).apply()
    }

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

    fun getCustomName(pkg: String, userHandle: UserHandle): String? =
        prefs.getString(PrefKeys.APP_CUSTOM_LABEL(pkg, userHandle), null)
            ?.takeIf { it.isNotEmpty() }

    fun setCustomName(pkg: String, userHandle: UserHandle, name: String) =
        prefs.edit().putString(PrefKeys.APP_CUSTOM_LABEL(pkg, userHandle), name).apply()

    fun clearCustomName(pkg: String, userHandle: UserHandle) =
        prefs.edit().remove(PrefKeys.APP_CUSTOM_LABEL(pkg, userHandle)).apply()

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