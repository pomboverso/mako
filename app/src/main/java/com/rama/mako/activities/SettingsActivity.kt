package com.rama.mako.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import com.rama.mako.BaseFullscreenActivity
import com.rama.mako.R
import com.rama.mako.widgets.WdButton
import com.rama.mako.widgets.WdCheckbox

class SettingsActivity : BaseFullscreenActivity(
) {
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    private val groupPrefs by lazy { getSharedPreferences("groups", MODE_PRIVATE) }
    private val groupsListPrefs by lazy {
        getSharedPreferences(
            "groups_list",
            MODE_PRIVATE
        )
    }

    private fun getGroups(): MutableList<String> {
        return groupsListPrefs.getStringSet("groups", mutableSetOf("Default"))!!
            .toMutableList()
    }

    private fun renameGroup(oldName: String, newName: String) {
        val all = groupPrefs.all.toMutableMap()

        val editor = groupPrefs.edit()

        all.forEach { (pkg, group) ->
            if (group == oldName) {
                editor.putString(pkg, newName)
            }
        }

        editor.apply()
    }

    private fun deleteGroup(groupName: String) {
        // Remove from group list
        val groups = getGroups()
        if (!groups.contains(groupName)) return
        groups.remove(groupName)
        groupsListPrefs.edit().putStringSet("groups", groups.toSet()).apply()

        // Move apps in this group to "Ungrouped"
        val editor = groupPrefs.edit()
        groupPrefs.all.forEach { (pkg, group) ->
            if (group == groupName) {
                editor.putString(pkg, getString(R.string.ungrouped_label))
            }
        }
        editor.apply()
    }

    private fun addGroupRow(group: String, container: LinearLayout, groups: MutableList<String>) {
        val row = layoutInflater.inflate(R.layout.list_item_group, container, false)
        val edit = row.findViewById<EditText>(R.id.group_name)
        val deleteBtn = row.findViewById<Button>(R.id.delete_group)

        edit.setText(group)

        edit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val oldName = edit.tag as String
                val newName = s?.toString()?.trim() ?: return
                if (oldName != newName) {
                    renameGroup(oldName, newName)
                    val index = groups.indexOf(oldName)
                    if (index != -1) groups[index] = newName
                    groupsListPrefs.edit().putStringSet("groups", groups.toSet()).apply()
                    edit.tag = newName
                }
            }
        })

        deleteBtn.setOnClickListener {
            deleteGroup(edit.text.toString())
            groups.remove(edit.text.toString())
            container.removeView(row)
        }

        // Track original name
        edit.tag = group

        container.addView(row)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_settings)

        val root = findViewById<View>(android.R.id.content)
        applyEdgeToEdgePadding(root)

        // Setup buttons
        setupButton(R.id.about_button) { startActivity(Intent(this, AboutActivity::class.java)) }
        setupButton(R.id.close_button) { finish() }

        setupButton(R.id.activate_button) {
            openIntent(Intent(Settings.ACTION_HOME_SETTINGS), "Unable to open launcher settings")
        }
        setupButton(R.id.wallpaper_button) {
            openIntent(Intent(Intent.ACTION_SET_WALLPAPER), "No wallpaper app available")
        }

        findViewById<View>(R.id.reset_button).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.change_apps_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }

        // Clock radio buttons
        val clockFormatGroup = findViewById<RadioGroup>(R.id.clock_format_group)

        // Restore saved state
        val showClock = prefs.getBoolean("show_clock", true)
        val clockFormat = prefs.getString("clock_format", "system")

        when {
            !showClock -> clockFormatGroup.check(R.id.clock_none)
            clockFormat == "24" -> clockFormatGroup.check(R.id.clock_24)
            clockFormat == "12" -> clockFormatGroup.check(R.id.clock_12)
            else -> clockFormatGroup.check(R.id.clock_system)
        }

        // Update prefs when user selects a radio button
        clockFormatGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.clock_none -> prefs.edit().putBoolean("show_clock", false)
                    .remove("clock_format").apply()

                R.id.clock_system -> prefs.edit().putBoolean("show_clock", true)
                    .putString("clock_format", "system").apply()

                R.id.clock_24 -> prefs.edit().putBoolean("show_clock", true)
                    .putString("clock_format", "24").apply()

                R.id.clock_12 -> prefs.edit().putBoolean("show_clock", true)
                    .putString("clock_format", "12").apply()
            }
        }

        // Checkboxes (without charge status)
        bindWdCheckbox(R.id.show_date, "show_date", false, dependentViewId = R.id.show_year_day)
        bindWdCheckbox(R.id.show_year_day, "show_year_day", false)
        bindWdCheckbox(R.id.show_battery, "show_battery", false)

        // Set Groups
        val groupsContainer = findViewById<LinearLayout>(R.id.groups)
        val groups = getGroups()

        groups.forEachIndexed { index, group ->
            val row = layoutInflater.inflate(
                R.layout.list_item_group,
                groupsContainer,
                false
            )

            val edit = row.findViewById<EditText>(R.id.group_name)
            val deleteBtn = row.findViewById<Button>(R.id.delete_group)

            edit.setText(group)

            edit.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    val oldName = groups[index]
                    val newName = s?.toString()?.trim() ?: return

                    if (oldName != newName) {
                        renameGroup(oldName, newName)
                        groups[index] = newName
                        groupsListPrefs.edit().putStringSet("groups", groups.toSet()).apply()
                    }
                }
            })

            deleteBtn.setOnClickListener {
                val oldName = groups[index]
                deleteGroup(oldName)
                groups.removeAt(index)
                groupsContainer.removeView(row)
            }

            groupsContainer.addView(row)
        }

        val addGroupBtn = findViewById<WdButton>(R.id.add_group)
        addGroupBtn.setOnClickListener {
            val groupsContainer = findViewById<LinearLayout>(R.id.groups)
            val groups = getGroups()

            val defaultName = "New Group"
            // Ensure unique default name
            var newName = defaultName
            var counter = 1
            while (groups.contains(newName)) {
                counter++
                newName = "$defaultName $counter"
            }

            // Add to SharedPreferences list
            groups.add(newName)
            groupsListPrefs.edit().putStringSet("groups", groups.toSet()).apply()

            // Add the row dynamically
            addGroupRow(newName, groupsContainer, groups)
        }
    }

    // Helper to bind a checkbox to SharedPreferences
    private fun bindWdCheckbox(
        wdCheckboxId: Int,
        prefKey: String,
        defaultValue: Boolean,
        dependentViewId: Int? = null
    ) {
        val wdCheckbox = findViewById<WdCheckbox>(wdCheckboxId)
        val dependentView = dependentViewId?.let { findViewById<View>(it) }

        // Initialize state
        val isChecked = prefs.getBoolean(prefKey, defaultValue)
        wdCheckbox.setChecked(isChecked)
        dependentView?.visibility = if (isChecked) View.VISIBLE else View.GONE

        wdCheckbox.setOnCheckedChangeListener { checked ->
            prefs.edit().putBoolean(prefKey, checked).apply()
            dependentView?.visibility = if (checked) View.VISIBLE else View.GONE
        }
    }

    // Helper to bind a click listener
    private fun setupButton(id: Int, action: () -> Unit) {
        findViewById<View>(id).setOnClickListener { action() }
    }

    // Safely open an intent
    private fun openIntent(intent: Intent, errorMsg: String) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
