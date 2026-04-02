package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.rama.mako.CsActivity
import com.rama.mako.R
import com.rama.mako.managers.FontManager
import com.rama.mako.managers.GroupsManager
import com.rama.mako.managers.PrefsManager
import com.rama.mako.widgets.WdButton
import com.rama.mako.widgets.WdCheckbox

class SettingsActivity : CsActivity() {

    private val prefs by lazy { PrefsManager.getInstance(this) }
    private val groupsManager by lazy { GroupsManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_settings)

        applyEdgeToEdgePadding(findViewById(android.R.id.content))

        setupBasicButtons()
        setupClockFormat()
        setupFontStyle()
        setupCheckboxes()
        setupGroups()
    }

    // ------------------- Basic buttons -------------------
    private fun setupBasicButtons() {
        setupButton(R.id.about_button) {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        setupButton(R.id.close_button) { finish() }

        setupButton(R.id.activate_button) {
            openIntent(
                Intent(Settings.ACTION_HOME_SETTINGS),
                getString(R.string.unable_open_settings_toast)
            )
        }

        setupButton(R.id.wallpaper_button) {
            openIntent(
                Intent(Intent.ACTION_SET_WALLPAPER),
                getString(R.string.unable_open_wallpaper_app_toast)
            )
        }

        findViewById<View>(R.id.reset_button).setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
        }

        findViewById<View>(R.id.change_apps_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }

        findViewById<WdButton>(R.id.set_clock_app_button).setOnClickListener {
            showAppPickerDialog()
        }
    }

    data class AppInfo(val label: String, val packageName: String)

    private fun getLaunchableApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return packageManager.queryIntentActivities(intent, 0)
            .map {
                AppInfo(
                    label = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun showAppPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pick_clock_app, null)
        FontManager.applyFont(this, dialogView)

        val dialog = android.app.Dialog(this).apply {
            setContentView(dialogView)
            setCancelable(true)
        }

        val listView = dialogView.findViewById<ListView>(R.id.app_list)
        val closeBtn = dialogView.findViewById<WdButton>(R.id.close_button)

        val apps = getLaunchableApps()

        val adapter = object : BaseAdapter() {
            override fun getCount() = apps.size
            override fun getItem(position: Int) = apps[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    R.layout.list_item_app,
                    parent,
                    false
                )

                val app = apps[position]

                view.findViewById<TextView>(R.id.open_app_button).text = app.label
                view.findViewById<ImageView>(R.id.app_icon).setImageDrawable(
                    packageManager.getApplicationIcon(app.packageName)
                )

                FontManager.applyFont(parent.context, view)
                return view
            }
        }

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedApp = apps[position]
            prefs.setClockApp(selectedApp.packageName)

            Toast.makeText(this, "Selected: ${selectedApp.label}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        closeBtn.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    // ------------------- Font style -------------------
    private fun setupFontStyle() {
        val group = findViewById<RadioGroup>(R.id.font_style_group)

        when (prefs.getFontStyle()) {
            "jersey" -> group.check(R.id.font_jersey)
            "montserrat" -> group.check(R.id.font_montserrat)
            "robotoslab" -> group.check(R.id.font_robotoslab)
            "quicksand" -> group.check(R.id.font_quicksand)
            else -> group.check(R.id.font_default)
        }

        group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.font_jersey -> prefs.setFontStyle("jersey")
                R.id.font_quicksand -> prefs.setFontStyle("quicksand")
                R.id.font_robotoslab -> prefs.setFontStyle("roboto")
                R.id.font_montserrat -> prefs.setFontStyle("montserrat")
                R.id.font_default -> prefs.setFontStyle("default")
            }
            refreshFont()
        }
    }

    // ------------------- Clock format -------------------
    private fun setupClockFormat() {
        val group = findViewById<RadioGroup>(R.id.clock_format_group)

        when {
            prefs.getClockFormat() == "none" -> group.check(R.id.clock_none)
            prefs.getClockFormat() == "24-hours" -> group.check(R.id.clock_24)
            prefs.getClockFormat() == "12-hours" -> group.check(R.id.clock_12)
            else -> group.check(R.id.clock_system)
        }

        group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.clock_none -> prefs.setClockFormat("none")
                R.id.clock_system -> prefs.setClockFormat("default")
                R.id.clock_24 -> prefs.setClockFormat("24-hours")
                R.id.clock_12 -> prefs.setClockFormat("12-hours")
            }
        }
    }

    // ------------------- Checkboxes -------------------
    private fun setupCheckboxes() {
        bindWdCheckbox(R.id.show_date, "settings:date:visible", true, listOf(R.id.show_year_day))
        bindWdCheckbox(R.id.show_search, "settings:apps:search", true)
        bindWdCheckbox(R.id.show_icons, "settings:apps:icons", true)

        bindWdCheckbox(
            R.id.show_group_header,
            "settings:groups:headers",
            true,
            listOf(R.id.has_collapsible_groups)
        )

        bindWdCheckbox(R.id.has_collapsible_groups, "settings:groups:collapsible", true)

        bindWdCheckbox(R.id.show_year_day, "settings:date:year_day", true)

        bindWdCheckbox(
            R.id.show_battery,
            "settings:battery:visible",
            true,
            listOf(
                R.id.show_battery_temperature,
                R.id.show_battery_charge_status
            )
        )

        bindWdCheckbox(R.id.show_battery_temperature, "settings:battery:temperature", true)
        bindWdCheckbox(R.id.show_battery_charge_status, "settings:battery:charge_status", true)
    }

    private fun bindWdCheckbox(
        wdCheckboxId: Int,
        key: String,
        defaultValue: Boolean,
        dependentViewIds: List<Int>? = null,
        onChange: ((Boolean) -> Unit)? = null
    ) {
        val checkbox = findViewById<WdCheckbox>(wdCheckboxId)
        val dependents = dependentViewIds?.map { findViewById<View>(it) }

        val isChecked = prefs.getBoolean(key, defaultValue)
        checkbox.setChecked(isChecked)

        dependents?.forEach {
            it.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        checkbox.setOnCheckedChangeListener { checked ->
            prefs.setBoolean(key, checked)

            dependents?.forEach {
                it.visibility = if (checked) View.VISIBLE else View.GONE
            }

            onChange?.invoke(checked)
        }
    }

    // ------------------- Groups -------------------
    private fun setupGroups() {
        val container = findViewById<RadioGroup>(R.id.groups)

        fun render() {
            container.removeAllViews()

            prefs.getGroupIds().forEach { id ->
                val label = prefs.getGroupLabel(id)
                addGroupRow(id, label, container, mutableListOf())
            }
        }

        render()

        findViewById<WdButton>(R.id.add_group).setOnClickListener {

            // Generate NEW ID (simple incremental or timestamp)
            val newId = System.currentTimeMillis().toString()

            // Default label
            var newLabel = getString(R.string.new_group_header)
            var counter = 1

            val existingLabels = prefs.getGroupIds()
                .map { prefs.getGroupLabel(it) }

            while (existingLabels.contains(newLabel)) {
                counter++
                newLabel = getString(R.string.new_group_header_count, counter)
            }

            // Save new group
            val updatedIds = prefs.getGroupIds().toMutableSet()
            updatedIds.add(newId)

            prefs.setGroupIds(updatedIds)
            prefs.setGroupLabel(newId, newLabel)
            prefs.setGroupVisible(newId, true)
            prefs.setGroupExpanded(newId, true)

            render()
        }
    }

    private fun addGroupRow(
        groupId: String,
        groupLabel: String,
        container: LinearLayout,
        groups: MutableList<String>
    ) {
        val row = layoutInflater.inflate(R.layout.list_item_group, container, false)
        FontManager.applyFont(this, row)

        val name = row.findViewById<EditText>(R.id.group_name)
        val delete = row.findViewById<FrameLayout>(R.id.delete_group)
        val toggle = row.findViewById<FrameLayout>(R.id.toggle_visibility)
        val toggleIcon = row.findViewById<ImageView>(R.id.toggle_visibility_img)

        name.setText(groupLabel)
        name.tag = groupId // store ID, not label

        fun updateIcon() {
            toggleIcon.setImageResource(
                if (prefs.isGroupVisible(groupId)) R.drawable.icon_eye
                else R.drawable.icon_eye_cross
            )
        }

        updateIcon()

        // Toggle visibility (ID-based)
        toggle.setOnClickListener {
            val newValue = !prefs.isGroupVisible(groupId)
            prefs.setGroupVisible(groupId, newValue)
            updateIcon()
        }

        // ------------------- Rename -------------------
        name.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                val newLabel = s?.toString()?.trim() ?: return
                if (newLabel.isEmpty()) return

                val id = name.tag as String

                // Only update label, NOT identity
                prefs.setGroupLabel(id, newLabel)
            }
        })

        // ------------------- Delete -------------------
        delete.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_groups_delete, null)
            FontManager.applyFont(this, dialogView)

            val dialog = android.app.Dialog(this).apply {
                setContentView(dialogView)
                setCancelable(true)
            }

            val yes = dialogView.findViewById<WdButton>(R.id.yes_button)
            val no = dialogView.findViewById<WdButton>(R.id.no_button)
            val radioGroup = dialogView.findViewById<RadioGroup>(R.id.groups)

            val currentGroupId = name.tag as String

            val targetGroups = prefs.getGroupIds()
                .filter { it != currentGroupId }

            var selectedGroupId: String? = null

            targetGroups.forEach { targetId ->
                val radio = RadioButton(this).apply {
                    text = prefs.getGroupLabel(targetId)
                    setOnClickListener { selectedGroupId = targetId }
                }
                radioGroup.addView(radio)
            }

            yes.setOnClickListener {
                if (selectedGroupId == null) {
                    Toast.makeText(this, "Select a target group", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // move apps + delete
                groupsManager.deleteGroup(currentGroupId, selectedGroupId!!)

                // remove from prefs
                val updated = prefs.getGroupIds().toMutableSet()
                updated.remove(currentGroupId)
                prefs.setGroupIds(updated)

                container.removeView(row)
                dialog.dismiss()
            }

            no.setOnClickListener { dialog.dismiss() }

            dialog.show()
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(row)
    }

    // ------------------- Helpers -------------------
    private fun setupButton(id: Int, action: () -> Unit) {
        findViewById<View>(id).setOnClickListener { action() }
    }

    private fun openIntent(intent: Intent, error: String) {
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
        else Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}