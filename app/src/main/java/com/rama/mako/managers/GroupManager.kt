package com.rama.mako.managers

import android.content.Context
import com.rama.mako.R

class GroupsManager(context: Context) {

    private val prefs = PrefsManager.getInstance(context)
    private val defaultGroup = context.getString(R.string.favorites_header)
    private val ungroupedLabel = context.getString(R.string.ungrouped_header)

    // --- Groups List ---

    fun getGroups(): MutableList<String> {
        return prefs.getStringSet("groups", mutableSetOf(defaultGroup))
            .toMutableList()
            .sortedBy { it.lowercase() }
            .toMutableList()
    }

    fun saveGroups(groups: List<String>) {
        prefs.setStringSet("groups", groups.toSet())
    }

    // --- App -> Group Mapping ---

    fun getGroup(pkg: String): String? {
        return prefs.getStringSet("app_group_map_$pkg", mutableSetOf())?.firstOrNull()
    }

    fun setGroup(pkg: String, group: String?) {
        if (group != null) {
            prefs.setStringSet("app_group_map_$pkg", setOf(group))
        } else {
            prefs.setStringSet("app_group_map_$pkg", emptySet())
        }
    }

    fun renameGroup(oldName: String, newName: String) {
        val normalizedOld = oldName.trim()
        val normalizedNew = newName.trim()

        // --- Update all apps that belong to the old group ---
        getAllAppGroups().forEach { (pkg, group) ->
            if (group.trim().equals(normalizedOld, ignoreCase = true)) {
                setGroup(pkg, normalizedNew)
            }
        }

        // --- Update the groups list ---
        val groups = getGroups().toMutableList()
        val index = groups.indexOfFirst { it.trim().equals(normalizedOld, ignoreCase = true) }
        if (index != -1) groups[index] = normalizedNew
        else groups.add(normalizedNew) // If somehow the group didn't exist, add it

        saveGroups(groups.sortedBy { it.lowercase() })
    }

    fun deleteGroup(groupName: String) {
        val normalizedGroup = groupName.trim()

        // --- Remove group from the list ---
        val groups = getGroups().toMutableList()
        groups.removeAll { it.trim().equals(normalizedGroup, ignoreCase = true) }
        saveGroups(groups)

        // --- Move apps to ungrouped if they belonged to this group ---
        getAllAppGroups().forEach { (pkg, group) ->
            if (group.trim().equals(normalizedGroup, ignoreCase = true)) {
                setGroup(pkg, ungroupedLabel)
            }
        }
    }

    // --- Group Visibility ---

    fun isGroupVisible(group: String): Boolean {
        return prefs.isGroupVisible(group)
    }

    fun setGroupVisibility(group: String, visible: Boolean) {
        prefs.setGroupVisible(group, visible)
    }

    // --- Helper to get all stored app->group mappings ---
    private fun getAllAppGroups(): Map<String, String> {
        val allKeys = prefs.getStringSet("all_apps", emptySet())
        val map = mutableMapOf<String, String>()
        allKeys.forEach { pkg ->
            getGroup(pkg)?.let { group -> map[pkg] = group }
        }
        return map
    }
}