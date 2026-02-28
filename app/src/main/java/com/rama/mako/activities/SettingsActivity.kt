package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.rama.mako.CsActivity
import com.rama.mako.R
import com.rama.mako.GroupsManager
import com.rama.mako.widgets.WdButton
import com.rama.mako.widgets.WdCheckbox

class SettingsActivity : CsActivity() {

    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }
    private val groupsManager by lazy { GroupsManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_settings)

        applyEdgeToEdgePadding(findViewById(android.R.id.content))

        // --- Setup basic buttons ---
        setupButton(R.id.about_button) { startActivity(Intent(this, AboutActivity::class.java)) }
        setupButton(R.id.close_button) { finish() }
        setupButton(R.id.activate_button) {
            openIntent(
                Intent(Settings.ACTION_HOME_SETTINGS),
                "Unable to open launcher settings"
            )
        }
        setupButton(R.id.wallpaper_button) {
            openIntent(
                Intent(Intent.ACTION_SET_WALLPAPER),
                "No wallpaper app available"
            )
        }

        findViewById<View>(R.id.reset_button).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }

        findViewById<View>(R.id.change_apps_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }

        // --- Clock format ---
        val clockFormatGroup = findViewById<RadioGroup>(R.id.clock_format_group)
        val showClock = prefs.getBoolean("show_clock", true)
        val clockFormat = prefs.getString("clock_format", "system")
        when {
            !showClock -> clockFormatGroup.check(R.id.clock_none)
            clockFormat == "24" -> clockFormatGroup.check(R.id.clock_24)
            clockFormat == "12" -> clockFormatGroup.check(R.id.clock_12)
            else -> clockFormatGroup.check(R.id.clock_system)
        }
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

        // --- Checkboxes ---
        bindWdCheckbox(R.id.show_date, "show_date", true, dependentViewId = R.id.show_year_day)
        bindWdCheckbox(R.id.show_year_day, "show_year_day", true)
        bindWdCheckbox(R.id.show_battery, "show_battery", true)
        bindWdCheckbox(R.id.use_pixel_font, "use_pixel_font", false) { refreshFont() }

        // --- Groups management ---
        val groupsContainer = findViewById<LinearLayout>(R.id.groups)
        val groups = groupsManager.getGroups().toMutableList()
        groups.forEach { group -> addGroupRow(group, groupsContainer, groups) }

        findViewById<WdButton>(R.id.add_group).setOnClickListener {
            var newName = "------ New Group"
            var counter = 1
            while (groups.contains(newName)) {
                counter++
                newName = "------ New Group $counter"
            }

            groups.add(newName)
            groups.sortBy { it.lowercase() }
            groupsManager.saveGroups(groups)
            prefs.edit().putBoolean("group_visibility_$newName", true).apply()

            // Rebuild UI
            groupsContainer.removeAllViews()
            groups.forEach { g -> addGroupRow(g, groupsContainer, groups) }
        }
    }

    // --- Add a single group row in the UI ---
    private fun addGroupRow(group: String, container: LinearLayout, groups: MutableList<String>) {
        val row = layoutInflater.inflate(R.layout.list_item_group, container, false)
        val nameEdit = row.findViewById<EditText>(R.id.group_name)
        val deleteBtn = row.findViewById<ImageView>(R.id.delete_group)
        val toggleBtn = row.findViewById<ImageView>(R.id.toggle_visibility)

        nameEdit.setText(group)
        nameEdit.tag = group

        // Visibility
        val isVisible = groupsManager.isGroupVisible(group)
        toggleBtn.setImageResource(if (isVisible) R.drawable.icon_visibility else R.drawable.icon_visibility_off)
        toggleBtn.setOnClickListener {
            val newVisibility = !groupsManager.isGroupVisible(group)
            groupsManager.setGroupVisibility(group, newVisibility)
            toggleBtn.setImageResource(if (newVisibility) R.drawable.icon_visibility else R.drawable.icon_visibility_off)
        }

        // Rename
        nameEdit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val oldName = nameEdit.tag as String
                val newName = s?.toString()?.trim() ?: return
                if (oldName != newName) {
                    groupsManager.renameGroup(oldName, newName)
                    val index = groups.indexOf(oldName)
                    if (index != -1) groups[index] = newName
                    groups.sortBy { it.lowercase() }
                    groupsManager.saveGroups(groups)
                    nameEdit.tag = newName
                }
            }
        })

        // Delete
        deleteBtn.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_confirmation, null)
            val dialog = android.app.Dialog(this)
            dialog.setContentView(dialogView)
            dialog.setCancelable(true)

            val title = dialogView.findViewById<TextView>(R.id.title)
            val yesButton = dialogView.findViewById<WdButton>(R.id.yes_button)
            val noButton = dialogView.findViewById<WdButton>(R.id.no_button)

            title.text =
                "Are you sure you want to delete this group?\nThis action cannot be undone."
            yesButton.setOnClickListener {
                groupsManager.deleteGroup(nameEdit.text.toString())
                groups.remove(nameEdit.text.toString())
                container.removeView(row)
                dialog.dismiss()
            }
            noButton.setOnClickListener { dialog.dismiss() }

            dialog.show()
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(row)
    }

    // --- Checkbox binding helper ---
    private fun bindWdCheckbox(
        wdCheckboxId: Int,
        prefKey: String,
        defaultValue: Boolean,
        dependentViewId: Int? = null,
        onChange: ((Boolean) -> Unit)? = null
    ) {
        val wdCheckbox = findViewById<WdCheckbox>(wdCheckboxId)
        val dependentView = dependentViewId?.let { findViewById<View>(it) }
        val isChecked = prefs.getBoolean(prefKey, defaultValue)

        wdCheckbox.setChecked(isChecked)
        dependentView?.visibility = if (isChecked) View.VISIBLE else View.GONE

        wdCheckbox.setOnCheckedChangeListener { checked ->
            prefs.edit().putBoolean(prefKey, checked).apply()
            dependentView?.visibility = if (checked) View.VISIBLE else View.GONE
            onChange?.invoke(checked)
        }
    }

    // --- Button helper ---
    private fun setupButton(id: Int, action: () -> Unit) {
        findViewById<View>(id).setOnClickListener { action() }
    }

    // --- Safe intent launcher ---
    private fun openIntent(intent: Intent, errorMsg: String) {
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
        else Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
    }
}