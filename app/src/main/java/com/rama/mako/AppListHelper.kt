package com.rama.mako

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.rama.mako.activities.SettingsActivity

class AppListHelper(
    private val context: Context,
    private val listView: ListView
) {

    private val groupPrefs = context.getSharedPreferences("groups", Context.MODE_PRIVATE)
    private val groupsListPrefs = context.getSharedPreferences("groups_list", Context.MODE_PRIVATE)

    // pkg -> group name
    private fun getGroup(pkg: String): String? = groupPrefs.getString(pkg, null)

    private fun setGroup(pkg: String, group: String?) {
        groupPrefs.edit().putString(pkg, group).apply()
    }

    private val namePrefs = context.getSharedPreferences("app_names", Context.MODE_PRIVATE)
    private val pm = context.packageManager

    private val items = mutableListOf<ListItem>()
    private lateinit var adapter: ArrayAdapter<ListItem>

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
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val allApps = pm.queryIntentActivities(intent, 0)

        // group apps
        val grouped = allApps.groupBy { app ->
            getGroup(app.activityInfo.packageName) ?: context.getString(R.string.ungrouped_label)
        }.toSortedMap()

        items.clear()

        grouped.forEach { (groupName, groupApps) ->
            // add header
            items.add(ListItem.Header(groupName))

            // sort apps inside group
            val sortedApps = groupApps.sortedBy { getDisplayName(it).lowercase() }

            sortedApps.forEach {
                items.add(ListItem.App(it))
            }
        }
    }

    private fun sanitizeSystemLabel(raw: String): String {
        return raw
            // Remove emojis & decorative symbols only
            .replace(Regex("[\\p{So}\\p{Cn}]"), "")
            // Remove notification-style punctuation spam
            .replace(Regex("[!?.]{2,}"), "")
            // Normalize whitespace
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun getDisplayName(app: ResolveInfo): String {
        val pkg = app.activityInfo.packageName

        getCustomName(pkg)?.let { return it }

        // Only sanitize system-provided labels
        val systemLabel = app.loadLabel(pm).toString()
        return sanitizeSystemLabel(systemLabel)
    }

    private fun getCustomName(pkg: String): String? = namePrefs.getString(pkg, null)
    private fun setCustomName(pkg: String, name: String) =
        namePrefs.edit().putString(pkg, name).apply()

    private fun clearCustomName(pkg: String) = namePrefs.edit().remove(pkg).apply()

    private fun getGroups(): MutableList<String> {
        return groupsListPrefs.getStringSet("groups", mutableSetOf("Default"))!!
            .toMutableList()
    }

    private fun saveGroups(groups: List<String>) {
        groupsListPrefs.edit().putStringSet("groups", groups.toSet()).apply()
    }

    private fun launchApp(pkg: String) {
        val intent = pm.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Unable to launch app", Toast.LENGTH_SHORT).show()
            refresh()
        }
    }

    private fun openAppSettings(pkg: String) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", pkg, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun showRenameDialog(app: ResolveInfo) {
        val pkg = app.activityInfo.packageName
        val currentName = getCustomName(pkg) ?: getDisplayName(app)

        val input = EditText(context).apply {
            setText(currentName)
            setSelection(text.length)
            maxLines = 1
            setSingleLine(true)
        }

        val container = FrameLayout(context).apply {
            val padding = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
            addView(input)
        }

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.rename_app))
            .setView(container)
            .setPositiveButton(context.getString(R.string.save)) { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    setCustomName(pkg, newName)
                }
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .setNeutralButton(context.getString(R.string.reset)) { _, _ ->
                clearCustomName(pkg)
                refresh()
            }
            .show()
    }

    private sealed class ListItem {
        data class Header(val title: String) : ListItem()
        data class App(val info: ResolveInfo) : ListItem()
    }

    private fun showGroupsDialog(app: ResolveInfo) {
        val pkg = app.activityInfo.packageName

        val view = View.inflate(context, R.layout.dialog_groups, null)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)
            .create()

        val closeBtn = view.findViewById<View>(R.id.close_button)
        val addGroupBtn = view.findViewById<View>(R.id.activate_button)
        val container = view.findViewById<LinearLayout>(R.id.groups)

        var groups = getGroups()
        val currentGroup = getGroup(pkg)

        fun renderGroups() {
            container.removeAllViews()

            val radioGroup = RadioGroup(context)

            groups.forEach { group ->
                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val radio = RadioButton(context).apply {
                    text = group
                    isChecked = group == currentGroup
                }

                row.addView(radio)

                radio.setOnClickListener {
                    setGroup(pkg, group)
                    refresh()
                    dialog.dismiss()
                }

                radioGroup.addView(row)
            }

            container.addView(radioGroup)
        }

        renderGroups()

        addGroupBtn.setOnClickListener {
            val input = EditText(context)

            AlertDialog.Builder(context)
                .setTitle("New group")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val newGroup = input.text.toString().trim()
                    if (newGroup.isNotEmpty()) {
                        groups.add(newGroup)
                        saveGroups(groups)
                        renderGroups()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showContextMenu(anchor: View, app: ResolveInfo) {
        val pkg = app.activityInfo.packageName

        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.app_context_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_rename -> {
                    showRenameDialog(app)
                    true
                }

                R.id.action_favorite -> {
                    showGroupsDialog(app)
                    true
                }

                R.id.action_settings -> {
                    openAppSettings(pkg)
                    true
                }

                else -> false
            }
        }

        forceShowIcons(popup)
        popup.show()
    }

    private fun setupAdapter() {
        adapter = object : ArrayAdapter<ListItem>(
            context,
            0,
            items
        ) {

            override fun getViewTypeCount() = 2

            override fun getItemViewType(position: Int): Int {
                return when (getItem(position)) {
                    is ListItem.Header -> 0
                    is ListItem.App -> 1
                    else -> 1
                }
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val item = getItem(position)!!

                return when (item) {
                    is ListItem.Header -> {
                        val view =
                            convertView ?: View.inflate(context, R.layout.app_list_header, null)
                        val text = view.findViewById<TextView>(R.id.header_text)
                        text.text = "------- " + item.title.uppercase()
                        view
                    }

                    is ListItem.App -> {
                        val view =
                            convertView ?: View.inflate(context, R.layout.app_list_item, null)
                        val app = item.info
                        val pkg = app.activityInfo.packageName

                        val label = view.findViewById<TextView>(R.id.open_app_button)
                        val emptySpace = view.findViewById<View>(R.id.empty_space)

                        label.text = getDisplayName(app)

                        label.setOnClickListener { launchApp(pkg) }
                        emptySpace.setOnClickListener { launchApp(pkg) }

                        label.setOnLongClickListener {
                            showContextMenu(it, app)
                            true
                        }

                        emptySpace.setOnLongClickListener {
                            context.startActivity(
                                Intent(context, SettingsActivity::class.java)
                            )
                            true
                        }

                        view
                    }
                }
            }
        }

        listView.adapter = adapter
    }

    // ------------------------------------------------------------------------
    // Scroll behavior
    // ------------------------------------------------------------------------
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

    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------
    private fun forceShowIcons(popup: PopupMenu) {
        try {
            val field = PopupMenu::class.java.getDeclaredField("mPopup")
            field.isAccessible = true
            val menuPopupHelper = field.get(popup)
            menuPopupHelper.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menuPopupHelper, true)
        } catch (_: Exception) {
        }
    }
}
