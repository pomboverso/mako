package com.rama.mako.managers

import android.content.Context

class GroupsManager(private val context: Context) {

    private val prefs = PrefsManager.getInstance(context)

    // Groups

    fun getGroupIds(): List<String> {
        return prefs.getGroupIds().sortedBy { prefs.getGroupLabel(it).lowercase() }
    }

    fun createGroup(label: String): String {
        val id = System.currentTimeMillis().toString()

        val updated = prefs.getGroupIds().toMutableSet()
        updated.add(id)

        prefs.setGroupIds(updated)
        prefs.setGroupLabel(id, label)
        prefs.setGroupVisible(id, true)
        prefs.setGroupExpanded(id, true)

        return id
    }

    fun deleteGroup(groupId: String, newGroupId: String?) {
        val allApps = getAllApps()

        // Move apps
        allApps.forEach { pkg ->
            val currentGroupId = getGroupId(pkg)
            if (currentGroupId == groupId) {
                setGroupId(pkg, newGroupId)
            }
        }

        // Remove group
        val updated = prefs.getGroupIds().toMutableSet()
        updated.remove(groupId)
        prefs.setGroupIds(updated)
    }

    // App -> Group mapping

    fun getGroupId(pkg: String): String? {
        return prefs.getString("app:$pkg:group_id", "")
            .takeIf { it.isNotEmpty() }
    }

    fun setGroupId(pkg: String, groupId: String?) {
        if (groupId != null) {
            prefs.setString("app:$pkg:group_id", groupId)
        } else {
            prefs.setString("app:$pkg:group_id", "")
        }
    }

    // Visibility / Expanded

    fun isGroupVisible(groupId: String): Boolean {
        return prefs.isGroupVisible(groupId)
    }

    fun isGroupExpanded(groupId: String): Boolean {
        return prefs.isGroupExpanded(groupId)
    }

    fun setGroupVisible(groupId: String, visible: Boolean) {
        prefs.setGroupVisible(groupId, visible)
    }

    fun setGroupExpanded(groupId: String, expanded: Boolean) {
        prefs.setGroupExpanded(groupId, expanded)
    }

    // Helpers

    private fun getAllApps(): List<String> {
        val pm = context.packageManager

        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName }
    }
}