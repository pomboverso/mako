package com.rama.mako.managers

import android.content.Context
import com.rama.mako.utils.IdUtils

class GroupsManager(private val context: Context) {

    private val prefs = PrefsManager.getInstance(context)

    // ------------------- Groups -------------------

    fun getGroupIds(): List<String> =
        prefs.getGroupIds()
            .sortedBy { prefs.getGroupLabel(it).lowercase() }


    fun createGroup(baseLabel: String): String {
        val id = IdUtils.toBase36Fixed(System.currentTimeMillis())

        val label = generateUniqueLabel(baseLabel)

        prefs.addGroupId(id)
        prefs.setGroupLabel(id, label)
        prefs.setGroupVisible(id, true)
        prefs.setGroupExpanded(id, true)

        return id
    }

    fun deleteGroup(groupId: String, newGroupId: String?) {
        getAllApps().forEach { pkg ->
            if (prefs.getAppGroupId(pkg) == groupId) {
                prefs.setAppGroupId(pkg, newGroupId)
            }
        }

        prefs.removeGroupId(groupId)
    }

    // ------------------- Label logic -------------------

    private fun generateUniqueLabel(base: String): String {
        val existing = prefs.getGroupIds()
            .map { prefs.getGroupLabel(it).trim().lowercase() }

        var label = base
        var counter = 1

        while (existing.contains(label.trim().lowercase())) {
            counter++
            label = "$base $counter"
        }

        return label
    }

    // ------------------- Visibility -------------------

    fun isGroupVisible(groupId: String) =
        prefs.isGroupVisible(groupId)

    fun isGroupExpanded(groupId: String) =
        prefs.isGroupExpanded(groupId)

    fun setGroupVisible(groupId: String, visible: Boolean) =
        prefs.setGroupVisible(groupId, visible)

    fun setGroupExpanded(groupId: String, expanded: Boolean) =
        prefs.setGroupExpanded(groupId, expanded)

    // ------------------- Helpers -------------------

    private fun getAllApps(): List<String> {
        val pm = context.packageManager

        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName }
    }
}