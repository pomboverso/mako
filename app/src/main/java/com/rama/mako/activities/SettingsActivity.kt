package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.generateViewId
import android.view.ViewGroup
import android.widget.*
import com.rama.mako.CsActivity
import com.rama.mako.R
import com.rama.mako.managers.FontManager
import com.rama.mako.managers.GroupsManager
import com.rama.mako.managers.PrefsManager
import com.rama.mako.managers.PrefsManager.PrefKeys
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

        findViewById<WdButton>(R.id.export_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "prefs_backup.json")
            }

            startActivityForResult(intent, 1001)
        }

        findViewById<WdButton>(R.id.clear_prefs_button).setOnClickListener {
            prefs.clearAllPrefs()
                .onSuccess {
                    Toast.makeText(this, "Reset done", Toast.LENGTH_SHORT).show()
                }
                .onFailure {
                    Toast.makeText(this, "Reset failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            data?.data?.let { prefs.exportToUri(this, it) }
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
            PrefsManager.FontStyle.JERSEY_25 -> group.check(R.id.font_jersey)
            PrefsManager.FontStyle.MONTSERRAT -> group.check(R.id.font_montserrat)
            PrefsManager.FontStyle.ROBOTO_SLAB -> group.check(R.id.font_robotoslab)
            PrefsManager.FontStyle.QUICKSAND -> group.check(R.id.font_quicksand)
            else -> group.check(R.id.font_default)
        }

        group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.font_jersey -> prefs.setFontStyle(PrefsManager.FontStyle.JERSEY_25)
                R.id.font_quicksand -> prefs.setFontStyle(PrefsManager.FontStyle.QUICKSAND)
                R.id.font_robotoslab -> prefs.setFontStyle(PrefsManager.FontStyle.ROBOTO_SLAB)
                R.id.font_montserrat -> prefs.setFontStyle(PrefsManager.FontStyle.MONTSERRAT)
                R.id.font_default -> prefs.setFontStyle(PrefsManager.FontStyle.DEFAULT)
            }
            refreshFont()
        }
    }

    // ------------------- Clock format -------------------
    private fun setupClockFormat() {
        val group = findViewById<RadioGroup>(R.id.clock_format_group)

        when {
            prefs.getClockFormat() == PrefsManager.ClockFormat.NONE -> group.check(R.id.clock_none)
            prefs.getClockFormat() == PrefsManager.ClockFormat.HOUR_24 -> group.check(R.id.clock_24)
            prefs.getClockFormat() == PrefsManager.ClockFormat.HOUR_12 -> group.check(R.id.clock_12)
            else -> group.check(R.id.clock_system)
        }

        group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.clock_none -> prefs.setClockFormat(PrefsManager.ClockFormat.NONE)
                R.id.clock_system -> prefs.setClockFormat(PrefsManager.ClockFormat.DEFAULT)
                R.id.clock_24 -> prefs.setClockFormat(PrefsManager.ClockFormat.HOUR_24)
                R.id.clock_12 -> prefs.setClockFormat(PrefsManager.ClockFormat.HOUR_12)
            }
        }
    }

    // ------------------- Checkboxes -------------------
    private fun setupCheckboxes() {
        bindWdCheckbox(R.id.show_date, PrefKeys.DATE_VISIBLE, false, listOf(R.id.show_year_day))
        bindWdCheckbox(R.id.show_search, PrefKeys.APPS_SEARCH, false)
        bindWdCheckbox(R.id.show_icons, PrefKeys.APPS_ICONS, false)

        bindWdCheckbox(
            R.id.show_group_header,
            PrefKeys.GROUPS_HEADERS,
            false,
            listOf(R.id.has_collapsible_groups)
        )

        bindWdCheckbox(R.id.has_collapsible_groups, PrefKeys.GROUPS_COLLAPSIBLE, false)

        bindWdCheckbox(R.id.show_year_day, PrefKeys.DATE_YEAR_DAY, false)

        bindWdCheckbox(
            R.id.show_battery,
            PrefKeys.BATTERY_VISIBLE,
            false,
            listOf(
                R.id.show_battery_temperature,
                R.id.show_battery_charge_status
            )
        )

        bindWdCheckbox(R.id.show_battery_temperature, PrefKeys.BATTERY_TEMPERATURE, false)
        bindWdCheckbox(R.id.show_battery_charge_status, PrefKeys.BATTERY_CHARGE_STATUS, false)
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
        val container = findViewById<LinearLayout>(R.id.groups)

        fun render() {
            container.removeAllViews()

            prefs.getGroupIds().forEach { id ->
                val label = prefs.getGroupLabel(id)
                addGroupRow(id, label, container, mutableListOf())
            }
        }

        render()

        findViewById<WdButton>(R.id.add_group).setOnClickListener {
            groupsManager.createGroup(getString(R.string.new_group_header))
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
        val saveButton = row.findViewById<FrameLayout>(R.id.save_changes_button)

        name.setText(groupLabel)
        name.tag = groupId
        name.setSaveEnabled(false)
        name.id = generateViewId()

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
        name.setText(groupLabel)
        name.tag = groupId

        saveButton.setOnClickListener {
            val newLabel = name.text.toString().trim()
            if (newLabel.isNotEmpty()) {
                val id = name.tag as String
                prefs.setGroupLabel(id, newLabel)
                Toast.makeText(this, "Label Updated", Toast.LENGTH_SHORT).show()
            }
        }

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
                    id = generateViewId()
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