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
import android.widget.ImageView
import android.widget.Toast

class AppListHelper(
    private val context: Context,
    private val listView: ListView
) {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val namePrefs = context.getSharedPreferences("app_names", Context.MODE_PRIVATE)
    private val pm = context.packageManager
    private val apps = mutableListOf<ResolveInfo>()
    private lateinit var adapter: ArrayAdapter<ResolveInfo>

    // ------------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------------
    fun setup() {
        loadApps()
        sortApps()
        setupAdapter()
        setupScrollListener()
    }

    fun refresh() {
        loadApps()
        sortApps()
        adapter.notifyDataSetChanged()
    }

    // ------------------------------------------------------------------------
    // Data
    // ------------------------------------------------------------------------
    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        apps.clear()
        apps.addAll(pm.queryIntentActivities(intent, 0))
    }

    private fun sortApps() {
        apps.sortWith(
            compareByDescending<ResolveInfo> { isFavorite(it.activityInfo.packageName) }
                .thenBy {
                    val pkg = it.activityInfo.packageName
                    (getCustomName(pkg) ?: it.loadLabel(pm).toString()).lowercase()
                }
        )
    }

    private fun isFavorite(pkg: String) = prefs.getBoolean(pkg, false)
    private fun setFavorite(pkg: String, value: Boolean) {
        prefs.edit().putBoolean(pkg, value).apply()
    }

    private fun getCustomName(pkg: String): String? = namePrefs.getString(pkg, null)
    private fun setCustomName(pkg: String, name: String) =
        namePrefs.edit().putString(pkg, name).apply()

    private fun clearCustomName(pkg: String) = namePrefs.edit().remove(pkg).apply()

    // ------------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------------
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
        val currentName = getCustomName(pkg) ?: app.loadLabel(pm).toString()

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
                    refresh()
                }
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .setNeutralButton(context.getString(R.string.reset)) { _, _ ->
                clearCustomName(pkg)
                refresh()
            }
            .show()
    }

    // ------------------------------------------------------------------------
    // Context menu
    // ------------------------------------------------------------------------
    private fun showContextMenu(anchor: View, app: ResolveInfo) {
        val pkg = app.activityInfo.packageName
        val isFav = isFavorite(pkg)

        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.app_context_menu, popup.menu)

        // Update favorite menu title based on current state
        popup.menu.findItem(R.id.action_favorite)?.title =
            context.getString(if (isFav) R.string.remove_from_favorites else R.string.add_to_favorites)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_rename -> {
                    showRenameDialog(app)
                    true
                }

                R.id.action_favorite -> {
                    val newState = !isFavorite(pkg)
                    setFavorite(pkg, newState)
                    refresh()
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

    // ------------------------------------------------------------------------
    // Adapter
    // ------------------------------------------------------------------------
    private fun setupAdapter() {
        adapter = object : ArrayAdapter<ResolveInfo>(
            context,
            R.layout.app_list_item,
            R.id.open_app_button,
            apps
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val app = getItem(position) ?: return view
                val pkg = app.activityInfo.packageName

                val label = view.findViewById<TextView>(R.id.open_app_button)
                val emptySpace = view.findViewById<View>(R.id.empty_space)
                val bottomBorder = view.findViewById<View>(R.id.favorite_bottom_border)

                emptySpace.setOnLongClickListener {
                    context.startActivity(
                        Intent(context, SettingsActivity::class.java)
                    )
                    true
                }

                // Set label
                label.text = getCustomName(pkg) ?: app.loadLabel(pm).toString()

                bottomBorder.visibility =
                    if (isLastFavorite(position)) View.VISIBLE else View.GONE

                // Clicks
                label.setOnClickListener { launchApp(pkg) }
                emptySpace.setOnClickListener { launchApp(pkg) }

                label.setOnLongClickListener {
                    showContextMenu(it, app)
                    true
                }

                return view
            }

            private fun isLastFavorite(position: Int): Boolean {
                val current = getItem(position) ?: return false
                val pkg = current.activityInfo.packageName
                if (!isFavorite(pkg)) return false

                val next = apps.getOrNull(position + 1)
                return next == null || !isFavorite(next.activityInfo.packageName)
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
