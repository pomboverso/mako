package com.rama.mako.managers

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import java.text.Normalizer
import java.util.Locale
import kotlin.math.abs

class AppListManager(
    private val context: Context,
    private val listView: ListView,
    private val appsProvider: AppsProvider,
    private val onAppLaunched: (() -> Unit)? = null
) {
    private data class ScoredApp(
        val app: AppsProvider.AppEntry,
        val score: Int,
        val normalizedName: String
    )

    private data class GroupMatch(
        val groupId: String,
        val label: String,
        val bestScore: Int,
        val apps: List<ScoredApp>
    )

    private val prefs = PrefsManager.getInstance(context)
    private val iconManager = IconManager(context, appsProvider)
    private val items = mutableListOf<ListItem>()
    private lateinit var adapter: ArrayAdapter<ListItem>
    private var allAppsCache: List<AppsProvider.AppEntry> = emptyList()
    private val searchableNameCache = mutableMapOf<String, String>()
    private val combiningMarkRegex = Regex("\\p{M}+")
    private val tokenSeparatorRegex = Regex("[^a-z0-9]+")

    fun setup() {
        updateAppsCache()
        buildItems()
        setupAdapter()
        setupScrollListener()
    }

    fun refresh() {
        updateAppsCache()
        buildItems()
        adapter.notifyDataSetChanged()
    }

    private fun updateAppsCache() {
        allAppsCache = appsProvider.getAll()
        searchableNameCache.clear()
    }

    private fun buildItems() {
        val allApps = allAppsCache

        // Get all known group IDs
        val groupIds = GroupsManager(context, appsProvider).getGroupIds()

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

            apps.sortedBy { getSearchableName(it) }
                .forEach { items.add(ListItem.App(it)) }
        }
    }

    private fun getAppCacheKey(app: AppsProvider.AppEntry): String {
        return "${app.packageName}:${app.userHandle.hashCode()}"
    }

    private fun getSearchableName(app: AppsProvider.AppEntry): String {
        val key = getAppCacheKey(app)
        return searchableNameCache.getOrPut(key) {
            normalizeForSearch(getDisplayName(app))
        }
    }

    // Explicit ones that might cause trouble when getting normalized
    private fun normalizeForSearch(value: String): String {
        val foldedTurkish = value
            .lowercase(Locale.ROOT)
            .replace('ı', 'i')
            .replace('ş', 's')
            .replace('ç', 'c')
            .replace('ğ', 'g')
            .replace('ö', 'o')
            .replace('ü', 'u')

        val foldedSpanish = foldedTurkish
            .replace('ñ', 'n')
            .replace('¡', ' ')
            .replace('¿', ' ')

        return Normalizer.normalize(foldedSpanish, Normalizer.Form.NFD)
            .replace(combiningMarkRegex, "")
            .trim()
    }

    private fun maxFuzzyDistance(queryLength: Int): Int {
        return when {
            queryLength <= 3 -> 0
            queryLength <= 5 -> 1
            queryLength <= 8 -> 2
            else -> 3
        }
    }

    private fun findWordPrefixIndex(text: String, query: String): Int {
        if (query.isEmpty() || query.length > text.length) return -1

        val lastStart = text.length - query.length
        for (i in 0..lastStart) {
            val isWordStart = i == 0 || !text[i - 1].isLetterOrDigit()
            if (!isWordStart) continue

            if (text.regionMatches(i, query, 0, query.length, ignoreCase = false)) {
                return i
            }
        }

        return -1
    }

    private fun boundedLevenshtein(a: String, b: String, maxDistance: Int): Int? {
        if (abs(a.length - b.length) > maxDistance) return null
        if (a == b) return 0
        if (a.isEmpty()) return if (b.length <= maxDistance) b.length else null
        if (b.isEmpty()) return if (a.length <= maxDistance) a.length else null

        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)

        for (i in 1..a.length) {
            current[0] = i
            var rowMin = current[0]
            val left = a[i - 1]

            for (j in 1..b.length) {
                val cost = if (left == b[j - 1]) 0 else 1
                val deletion = previous[j] + 1
                val insertion = current[j - 1] + 1
                val substitution = previous[j - 1] + cost

                val cell = minOf(deletion, insertion, substitution)
                current[j] = cell
                if (cell < rowMin) rowMin = cell
            }

            if (rowMin > maxDistance) return null

            val swap = previous
            previous = current
            current = swap
        }

        val result = previous[b.length]
        return if (result <= maxDistance) result else null
    }

    private fun getFuzzyDistance(name: String, query: String, maxDistance: Int): Int? {
        var best: Int? = null

        fun tryCandidate(candidate: String) {
            if (candidate.isEmpty()) return
            val distance = boundedLevenshtein(candidate, query, maxDistance) ?: return
            if (best == null || distance < best!!) {
                best = distance
            }
        }

        tryCandidate(name)

        name.split(tokenSeparatorRegex)
            .filter { it.isNotEmpty() }
            .forEach { token ->
                tryCandidate(token)
            }

        return best
    }

    private fun scoreMatch(name: String, query: String): Int? {
        if (name == query) return 0

        if (name.startsWith(query)) {
            return 100 + (name.length - query.length).coerceAtLeast(0)
        }

        val wordPrefixIndex = findWordPrefixIndex(name, query)
        if (wordPrefixIndex >= 0) {
            return 200 + wordPrefixIndex
        }

        val containsIndex = name.indexOf(query)
        if (containsIndex >= 0) {
            val lengthDiff = abs(name.length - query.length)
            return 300 + (containsIndex * 2) + lengthDiff
        }

        val maxDistance = maxFuzzyDistance(query.length)
        if (maxDistance == 0) return null

        val fuzzyDistance = getFuzzyDistance(name, query, maxDistance) ?: return null
        val lengthDiff = abs(name.length - query.length)

        return 1000 + (fuzzyDistance * 100) + lengthDiff
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
        val normalizedQuery = normalizeForSearch(query)
        val isSearchActive = normalizedQuery.isNotEmpty()

        if (!isSearchActive) {
            buildItems()
            adapter.notifyDataSetChanged()
            return
        }

        val filteredItems = mutableListOf<ListItem>()
        val matchedGroups = mutableListOf<GroupMatch>()

        val allApps = allAppsCache

        // All known group IDs
        val groupIds = GroupsManager(context, appsProvider).getGroupIds()

        // Group by ID
        val groupedMap = allApps.groupBy { app ->
            prefs.getAppGroupId(app.packageName, app.userHandle) ?: PrefsManager.SystemIds.UNGROUPED
        }

        // Handle unknown groups (apps pointing to deleted groups)
        val unknownGroupIds = groupedMap.keys.filter { it !in groupIds }
        val allGroupIds = (groupIds + unknownGroupIds)
            .distinct()
            .sortedBy { prefs.getGroupLabel(it).lowercase(Locale.ROOT) }

        allGroupIds.forEach { groupId ->

            val apps = groupedMap[groupId] ?: return@forEach

            val isVisible = prefs.isGroupVisible(groupId)
            if (!isVisible) return@forEach

            val matchedApps = apps.mapNotNull { app ->
                val normalizedName = getSearchableName(app)
                val score = scoreMatch(normalizedName, normalizedQuery) ?: return@mapNotNull null
                ScoredApp(app = app, score = score, normalizedName = normalizedName)
            }.sortedWith(
                compareBy<ScoredApp> { it.score }
                    .thenBy { it.normalizedName }
            )

            if (matchedApps.isEmpty()) return@forEach

            val label = prefs.getGroupLabel(groupId)

            matchedGroups.add(
                GroupMatch(
                    groupId = groupId,
                    label = label,
                    bestScore = matchedApps.first().score,
                    apps = matchedApps
                )
            )
        }

        matchedGroups
            .sortedWith(
                compareBy<GroupMatch> { it.bestScore }
                    .thenBy { it.label.lowercase(Locale.ROOT) }
            )
            .forEach { group ->
                if (prefs.hasGroupHeaders()) {
                    filteredItems.add(
                        ListItem.Header(
                            id = group.groupId,
                            title = group.label
                        )
                    )
                }

                group.apps.forEach { scoredApp ->
                    filteredItems.add(ListItem.App(scoredApp.app))
                }
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
            val groupIds = GroupsManager(
                context,
                appsProvider
            ).getGroupIds()

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
                        val collapsible = prefs.hasCollapsibleGroups()

                        val isExpanded = if (collapsible) prefs.isGroupExpanded(groupId) else false

                        val collapseIndicator = if (collapsible) {
                            context.getString(
                                if (isExpanded)
                                    R.string.settings_section_collapse_indicator
                                else
                                    R.string.settings_section_expand_indicator
                            ) + " "
                        } else ""

                        text.text = collapseIndicator + groupName.uppercase()
                        FontManager.applyFont(context, text)

                        if (collapsible) {
                            view.setOnClickListener {
                                prefs.setGroupExpanded(groupId, !isExpanded)
                                refresh()
                            }
                        } else {
                            view.setOnClickListener(null)
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
                            val drawable = iconManager.getIcon(app)

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
                                } else {
                                    onAppLaunched?.invoke()
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
                            } else {
                                onAppLaunched?.invoke()
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
                            } else {
                                onAppLaunched?.invoke()
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
