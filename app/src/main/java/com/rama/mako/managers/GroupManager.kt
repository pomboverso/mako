package com.rama.mako.managers

import android.content.Context
import com.rama.mako.utils.IdUtils

class GroupsManager(
    context: Context,
    private val appsProvider: AppsProvider
) {

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
        val allApps = appsProvider.getAll()
        allApps.forEach { app ->
            if (prefs.getAppGroupId(app.packageName, app.userHandle) == groupId) {
                prefs.setAppGroupId(app.packageName, app.userHandle, newGroupId)
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
}