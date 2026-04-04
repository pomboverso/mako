package com.rama.mako.managers

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.generateViewId
import android.view.ViewGroup
import android.widget.*
import com.rama.mako.R
import com.rama.mako.utils.sp
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.widgets.WdButton

class AppListManager(
    private val context: Context,
    private val listView: ListView,
    private val appsProvider: AppsProvider
) {
    private val prefs = PrefsManager.getInstance(context)
    private val items = mutableListOf<ListItem>()
    private lateinit var adapter: ArrayAdapter<ListItem>
    private val iconCache = mutableMapOf<String, Drawable>()

    fun setup() {
        buildItems()
        setupAdapter()
        setupScrollListener()
    }

    fun refresh() {
        buildItems()
        adapter.notifyDataSetChanged()
    }

    private fun buildItems() {
        val allApps = appsProvider.getAll()

        // Get all known group IDs
        val groupIds = prefs.getGroupIds().toMutableSet()

        // Map apps by groupId (NOT label)
        val groupedMap = allApps.groupBy { app ->
            prefs.getAppGroupId(app.packageName, app.userHandle) ?: PrefsManager.SystemIds.UNGROUPED
        }

        items.clear()

        // Include unknown groupIds (apps pointing to deleted groups)
        val unknownGroupIds = groupedMap.keys.filter { it !in groupIds }
        val allGroupIds = (groupIds + unknownGroupIds).distinct()

        allGroupIds.forEach { groupId ->

            val apps = groupedMap[groupId] ?: return@forEach

            val isVisible = prefs.isGroupVisible(groupId)
            if (!isVisible) return@forEach

            val label = prefs.getGroupLabel(groupId)

            // Header uses label only for display
            if (prefs.hasGroupHeaders()) {
                items.add(
                    ListItem.Header(
                        id = groupId,
                        title = label
                    )
                )
            }

            val isExpanded = prefs.isGroupExpanded(groupId)
            if (!isExpanded) return@forEach

            apps.sortedBy { getDisplayName(it).lowercase() }
                .forEach { items.add(ListItem.App(it)) }
        }
    }

    private fun getDisplayName(app: AppsProvider.AppEntry): String {
        val custom = prefs.getCustomName(app.packageName, app.userHandle)
        return if (custom != null) {
            if (app.isWorkProfile) "[${app.profileInitial}] $custom" else custom
        } else {
            app.displayLabel
        }
    }

    fun filter(query: String) {
        val lowerQuery = query.lowercase()

        val filteredItems = mutableListOf<ListItem>()

        val allApps = appsProvider.getAll()

        // All known group IDs
        val groupIds = prefs.getGroupIds().toMutableSet()

        // Group by ID
        val groupedMap = allApps.groupBy { app ->
            prefs.getAppGroupId(app.packageName, app.userHandle) ?: PrefsManager.SystemIds.UNGROUPED
        }

        // Handle unknown groups (apps pointing to deleted groups)
        val unknownGroupIds = groupedMap.keys.filter { it !in groupIds }
        val allGroupIds = (groupIds + unknownGroupIds).distinct()

        allGroupIds.forEach { groupId ->

            val apps = groupedMap[groupId] ?: return@forEach

            val isVisible = prefs.isGroupVisible(groupId)
            if (!isVisible) return@forEach

            // Filter apps
            val matchedApps = apps.filter {
                getDisplayName(it).lowercase().contains(lowerQuery)
            }

            if (matchedApps.isEmpty()) return@forEach

            val label = prefs.getGroupLabel(groupId)

            if (prefs.hasGroupHeaders()) {
                filteredItems.add(
                    ListItem.Header(
                        id = groupId,
                        title = label
                    )
                )

            }

            val isExpanded = prefs.isGroupExpanded(groupId)
            if (!isExpanded) return@forEach

            matchedApps
                .sortedBy { getDisplayName(it).lowercase() }
                .forEach { filteredItems.add(ListItem.App(it)) }
        }

        items.clear()
        items.addAll(filteredItems)
        adapter.notifyDataSetChanged()
    }

    private fun openAppSettings(pkg: String) {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", pkg, null)
            )
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
    }

    private fun showRenameDialog(app: AppsProvider.AppEntry) {
        val pkg = app.packageName
        val currentName = prefs.getCustomName(app.packageName, app.userHandle)
            ?: app.label

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_rename_app, null)
        FontManager.applyFont(context, view)
        val input = view.findViewById<EditText>(R.id.edit_text)
        val yesButton = view.findViewById<WdButton>(R.id.yes_button)
        val resetButton = view.findViewById<WdButton>(R.id.reset_button)
        val noButton = view.findViewById<WdButton>(R.id.no_button)

        input.setText(currentName)
        input.setSelection(input.text.length)

        val dialog = AlertDialog.Builder(context).setView(view).create()

        yesButton.setOnClickListener {
            input.text.toString().trim().takeIf { it.isNotEmpty() }
                ?.let { prefs.setCustomName(pkg, app.userHandle, it) }
            refresh()
            dialog.dismiss()
        }

        resetButton.setOnClickListener {
            prefs.clearCustomName(pkg, app.userHandle)
            refresh()
            dialog.dismiss()
        }

        noButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showGroupsDialog(app: AppsProvider.AppEntry) {
        val pkg = app.packageName

        val view = View.inflate(context, R.layout.dialog_groups_add, null)
        FontManager.applyFont(context, view)

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)
            .create()

        val closeBtn = view.findViewById<View>(R.id.close_button)
        val container = view.findViewById<RadioGroup>(R.id.groups)

        fun renderGroups() {
            container.removeAllViews()

            val radioGroup = RadioGroup(context)

            val currentGroupId =
                prefs.getAppGroupId(pkg, app.userHandle) ?: PrefsManager.SystemIds.UNGROUPED

            // All group IDs (include ungrouped)
            val groupIds = prefs.getGroupIds().toMutableList()

            groupIds.forEachIndexed { index, groupId ->
                val isLast = index == groupIds.lastIndex
                val label = prefs.getGroupLabel(groupId)

                val radio = RadioButton(context).apply {
                    id = generateViewId()
                    text = label
                    isChecked = groupId == currentGroupId
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = if (isLast) 0 else context.sp(8f)
                    }
                }

                FontManager.applyFont(context, radio)

                radio.setOnClickListener {
                    prefs.setAppGroupId(pkg, app.userHandle, groupId)
                    refresh()
                    dialog.dismiss()
                }

                radioGroup.addView(radio)
            }

            container.addView(radioGroup)
        }

        renderGroups()

        closeBtn.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showContextMenu(anchor: View, app: AppsProvider.AppEntry) {
        val pkg = app.packageName
        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.app_context_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_rename -> {
                    showRenameDialog(app); true
                }

                R.id.action_favorite -> {
                    showGroupsDialog(app); true
                }

                R.id.action_settings -> {
                    openAppSettings(pkg); true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun setupAdapter() {
        adapter = object : ArrayAdapter<ListItem>(context, 0, items) {
            override fun getViewTypeCount() = 2
            override fun getItemViewType(position: Int) = when (getItem(position)) {
                is ListItem.Header -> 0
                is ListItem.App -> 1
                else -> 1
            }

            override fun isEnabled(position: Int) = getItem(position) is ListItem.App
            override fun areAllItemsEnabled() = false

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val item = getItem(position)!!

                return when (item) {
                    is ListItem.Header -> {
                        val view =
                            convertView ?: View.inflate(context, R.layout.app_list_header, null)

                        val text = view.findViewById<TextView>(R.id.header_text)

                        val groupId = item.id
                        val groupName = item.title

                        val isExpanded = prefs.isGroupExpanded(groupId)

                        text.text =
                            (if (isExpanded) "[-] " else "[+] ") + groupName.uppercase()

                        FontManager.applyFont(context, text)

                        if (prefs.hasCollapsibleGroups()) {

                            view.setOnClickListener {
                                val currently = prefs.isGroupExpanded(groupId)
                                prefs.setGroupExpanded(groupId, !currently)
                                refresh()
                            }
                        }

                        view
                    }

                    is ListItem.App -> {
                        val view =
                            convertView ?: View.inflate(context, R.layout.list_item_app, null)
                        val app = item.info
                        val pkg = app.packageName
                        val label = view.findViewById<TextView>(R.id.open_app_button)
                        val emptySpace = view.findViewById<View>(R.id.empty_space)

                        val icon = view.findViewById<ImageView>(R.id.app_icon)
                        val showIcons = prefs.hasIconsVisible()

                        if (showIcons) {
                            val drawable = appsProvider.getIcon(app)

                            icon.setImageDrawable(drawable)
                            icon.visibility = View.VISIBLE
                            icon.setOnClickListener {
                                if (!appsProvider.launch(app)) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.unable_launch_app_toast),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    refresh()
                                }
                            }
                            icon.setOnLongClickListener { showContextMenu(it, app); true }
                        } else {
                            icon.visibility = View.GONE
                            icon.setImageDrawable(null)
                            icon.setOnClickListener(null)
                        }

                        label.text = getDisplayName(app)

                        label.setOnClickListener {
                            if (!appsProvider.launch(app)) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.unable_launch_app_toast),
                                    Toast.LENGTH_SHORT
                                ).show()
                                refresh()
                            }
                        }
                        emptySpace.setOnClickListener {
                            if (!appsProvider.launch(app)) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.unable_launch_app_toast),
                                    Toast.LENGTH_SHORT
                                ).show()
                                refresh()
                            }
                        }

                        label.setOnLongClickListener { showContextMenu(it, app); true }
                        emptySpace.setOnLongClickListener {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                            true
                        }

                        FontManager.applyFont(context, label)
                        view
                    }
                }
            }
        }

        listView.adapter = adapter
    }

    private fun setupScrollListener() {
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) = Unit
            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) = Unit
        })
    }

    private sealed class ListItem {
        data class Header(val id: String, val title: String) : ListItem()
        data class App(val info: AppsProvider.AppEntry) : ListItem()
    }
}