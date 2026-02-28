package com.rama.mako.managers

import android.content.Context
import com.rama.mako.R

class GroupsManager(private val context: Context) {

    private val groupPrefs = context.getSharedPreferences("groups", Context.MODE_PRIVATE)
    private val groupsListPrefs = context.getSharedPreferences("groups_list", Context.MODE_PRIVATE)
    private val settingsPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val defaultGroup = context.getString(R.string.favorites_header)
    private val ungroupedLabel = context.getString(R.string.ungrouped_header)

    fun getGroups(): MutableList<String> {
        return groupsListPrefs
            .getStringSet("groups", mutableSetOf(defaultGroup))!!
            .toMutableList()
            .sortedBy { it.lowercase() }
            .toMutableList()
    }

    fun saveGroups(groups: List<String>) {
        groupsListPrefs.edit().putStringSet("groups", groups.toSet()).apply()
    }

    fun getGroup(pkg: String): String? {
        return groupPrefs.getString(pkg, null)
    }

    fun setGroup(pkg: String, group: String?) {
        groupPrefs.edit().putString(pkg, group).apply()
    }

    fun renameGroup(oldName: String, newName: String) {
        val editor = groupPrefs.edit()
        groupPrefs.all.forEach { (pkg, group) ->
            if (group == oldName) editor.putString(pkg, newName)
        }
        editor.apply()

        val groups = getGroups()
        val index = groups.indexOf(oldName)
        if (index != -1) groups[index] = newName
        saveGroups(groups.sortedBy { it.lowercase() })
    }

    fun deleteGroup(groupName: String) {
        // Remove from list
        val groups = getGroups()
        if (!groups.contains(groupName)) return
        groups.remove(groupName)
        saveGroups(groups)

        // Move apps to ungrouped
        val editor = groupPrefs.edit()
        groupPrefs.all.forEach { (pkg, group) ->
            if (group == groupName) editor.putString(pkg, ungroupedLabel)
        }
        editor.apply()
    }

    fun isGroupVisible(group: String): Boolean {
        return settingsPrefs.getBoolean("group_visibility_$group", true)
    }

    fun setGroupVisibility(group: String, visible: Boolean) {
        settingsPrefs.edit().putBoolean("group_visibility_$group", visible).apply()
    }
}