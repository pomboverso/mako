package com.rama.mako.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.view.View
import android.view.View.generateViewId
import android.view.ViewGroup
import android.widget.*
import com.rama.mako.CsActivity
import com.rama.mako.R
import com.rama.mako.managers.AppsProvider
import com.rama.mako.managers.FontManager
import com.rama.mako.managers.GroupsManager
import com.rama.mako.managers.IconManager
import com.rama.mako.managers.PrefsManager
import com.rama.mako.managers.PrefsManager.PrefKeys
import com.rama.mako.widgets.WdButton
import com.rama.mako.widgets.WdCheckbox
import android.text.TextWatcher

class SettingsActivity : CsActivity() {

    private val prefs by lazy { PrefsManager.getInstance(this) }
    private val groupsManager by lazy { GroupsManager(this, AppsProvider(this)) }
    private lateinit var appsProvider: AppsProvider
    private lateinit var iconManager: IconManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_settings)

        applyEdgeToEdgePadding(findViewById(android.R.id.content))
        appsProvider = AppsProvider(this)
        iconManager = IconManager(this, appsProvider)

        setupBasicButtons()
        setupClockFormat()
        setupFontStyle()
        setupIconsSection()
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

    private fun showAppPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pick_clock_app, null)
        FontManager.applyFont(this, dialogView)

        val dialog = android.app.Dialog(this).apply {
            setContentView(dialogView)
            setCancelable(true)
        }

        val listView = dialogView.findViewById<ListView>(R.id.app_list)
        val closeBtn = dialogView.findViewById<WdButton>(R.id.close_button)

        val apps = appsProvider.getAll()

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
                    iconManager.getIcon(app)
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
            ViewGroup.LayoutParams.WRAP_CONTENT
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
        bindWdCheckbox(
            R.id.show_icons,
            PrefKeys.APPS_ICONS,
            false,
            listOf(R.id.icons_options_container)
        )

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
                R.id.use_battery_temperature_celsius,
                R.id.show_battery_charge_status
            )
        )

        bindWdCheckbox(R.id.show_battery_temperature, PrefKeys.BATTERY_TEMPERATURE, false)
        bindWdCheckbox(
            R.id.use_battery_temperature_celsius,
            PrefKeys.BATTERY_TEMPERATURE_CELSIUS,
            false
        )
        bindWdCheckbox(R.id.show_battery_charge_status, PrefKeys.BATTERY_CHARGE_STATUS, false)
    }

    private fun setupIconsSection() {
        val group = findViewById<RadioGroup>(R.id.icon_source_group)
        val selectIconPackButton = findViewById<WdButton>(R.id.select_icon_pack_button)

        when (prefs.getIconSource()) {
            PrefsManager.IconSource.MONOCHROME -> group.check(R.id.icon_source_monochrome)
            PrefsManager.IconSource.ICON_PACK -> group.check(R.id.icon_source_icon_pack)
            else -> group.check(R.id.icon_source_system)
        }

        refreshIconPackSection()

        group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.icon_source_monochrome -> prefs.setIconSource(PrefsManager.IconSource.MONOCHROME)
                R.id.icon_source_icon_pack -> prefs.setIconSource(PrefsManager.IconSource.ICON_PACK)
                else -> prefs.setIconSource(PrefsManager.IconSource.SYSTEM)
            }

            if (
                id == R.id.icon_source_icon_pack &&
                prefs.getIconPackPackage().isBlank()
            ) {
                showIconPackPickerDialog()
            }

            if (id == R.id.icon_source_monochrome && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(
                    this,
                    getString(R.string.monochrome_fallback_toast),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            refreshIconPackSection()
        }

        selectIconPackButton.setOnClickListener {
            showIconPackPickerDialog()
        }
    }

    private fun refreshIconPackSection() {
        val iconPackControls = findViewById<View>(R.id.icon_pack_controls)
        val selectedIconPack = findViewById<TextView>(R.id.selected_icon_pack_label)

        val currentPackage = prefs.getIconPackPackage()
        val currentLabel = if (currentPackage.isBlank()) {
            null
        } else {
            iconManager.getIconPackLabel(currentPackage)
        }

        selectedIconPack.text = if (currentLabel != null) {
            getString(R.string.selected_icon_pack_label, currentLabel)
        } else if (currentPackage.isNotBlank()) {
            getString(R.string.selected_icon_pack_label, currentPackage)
        } else {
            getString(R.string.icon_pack_not_selected_label)
        }

        iconPackControls.visibility =
            if (prefs.getIconSource() == PrefsManager.IconSource.ICON_PACK) View.VISIBLE else View.GONE
    }

    private fun showIconPackPickerDialog() {
        val iconPacks = iconManager.getInstalledIconPacks()
        if (iconPacks.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_icon_pack_found_label), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_pick_icon_pack, null)
        FontManager.applyFont(this, dialogView)

        val dialog = android.app.Dialog(this).apply {
            setContentView(dialogView)
            setCancelable(true)
        }

        val listView = dialogView.findViewById<ListView>(R.id.icon_pack_list)
        val closeBtn = dialogView.findViewById<WdButton>(R.id.close_button)
        val selectedPackage = prefs.getIconPackPackage()

        val adapter = object : BaseAdapter() {
            override fun getCount() = iconPacks.size
            override fun getItem(position: Int) = iconPacks[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    R.layout.list_item_icon_pack,
                    parent,
                    false
                )

                val iconPack = iconPacks[position]
                val labelPrefix = if (iconPack.packageName == selectedPackage) "[*] " else "[ ] "

                view.findViewById<TextView>(R.id.icon_pack_label).text =
                    labelPrefix + iconPack.label
                view.findViewById<ImageView>(R.id.icon_pack_icon).setImageDrawable(iconPack.icon)

                FontManager.applyFont(parent.context, view)
                return view
            }
        }

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedIconPack = iconPacks[position]
            prefs.setIconPackPackage(selectedIconPack.packageName)
            prefs.setIconSource(PrefsManager.IconSource.ICON_PACK)
            findViewById<RadioGroup>(R.id.icon_source_group).check(R.id.icon_source_icon_pack)

            Toast.makeText(
                this,
                getString(R.string.icon_pack_selected_toast, selectedIconPack.label),
                Toast.LENGTH_SHORT
            ).show()

            refreshIconPackSection()
            dialog.dismiss()
        }

        closeBtn.setOnClickListener {
            refreshIconPackSection()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

            groupsManager.getGroupIds().forEach { id ->
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

        val originalText = name.text.toString()

        name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentText = s?.toString() ?: ""

                saveButton.visibility =
                    if (currentText != originalText && currentText.isNotBlank())
                        View.VISIBLE
                    else
                        View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        saveButton.setOnClickListener {
            val newLabel = name.text.toString().trim()
            if (newLabel.isNotEmpty()) {
                val id = name.tag as String
                prefs.setGroupLabel(id, newLabel)
                Toast.makeText(this, "Label Updated", Toast.LENGTH_SHORT).show()
            }
            saveButton.visibility = View.GONE
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

            val targetGroups = groupsManager.getGroupIds()
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