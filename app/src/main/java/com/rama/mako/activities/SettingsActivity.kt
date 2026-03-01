package com.rama.mako.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
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
        setupCheckboxes()
        setupGroups()

        val appOsSettingsBtn = findViewById<WdButton>(R.id.app_os_settings)
        appOsSettingsBtn.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    getString(R.string.unable_open_settings_toast),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ------------------- Basic buttons -------------------
    private fun setupBasicButtons() {
        setupButton(R.id.about_button) { startActivity(Intent(this, AboutActivity::class.java)) }
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
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }

        findViewById<View>(R.id.change_apps_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }
    }

    // ------------------- Clock format -------------------
    private fun setupClockFormat() {
        val clockFormatGroup = findViewById<RadioGroup>(R.id.clock_format_group)
        when {
            !prefs.isClockVisible() -> clockFormatGroup.check(R.id.clock_none)
            prefs.getClockFormat() == "24" -> clockFormatGroup.check(R.id.clock_24)
            prefs.getClockFormat() == "12" -> clockFormatGroup.check(R.id.clock_12)
            else -> clockFormatGroup.check(R.id.clock_system)
        }

        clockFormatGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.clock_none -> prefs.setClockNone()
                R.id.clock_system -> prefs.setClockSystem()
                R.id.clock_24 -> prefs.setClock24()
                R.id.clock_12 -> prefs.setClock12()
            }
        }
    }

    // ------------------- Checkboxes -------------------
    private fun setupCheckboxes() {
        bindWdCheckbox(R.id.show_date, "show_date", true, dependentViewId = R.id.show_year_day)
        bindWdCheckbox(R.id.show_year_day, "show_year_day", true)
        bindWdCheckbox(R.id.show_battery, "show_battery", true)
        bindWdCheckbox(R.id.use_pixel_font, "use_pixel_font", false) { refreshFont() }
    }

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
            prefs.setBoolean(prefKey, checked)
            dependentView?.visibility = if (checked) View.VISIBLE else View.GONE
            onChange?.invoke(checked)
        }
    }

    // ------------------- Groups management -------------------
    private fun setupGroups() {
        val groupsContainer = findViewById<LinearLayout>(R.id.groups)
        val groups = groupsManager.getGroups().toMutableList()
        groups.forEach { group -> addGroupRow(group, groupsContainer, groups) }

        findViewById<WdButton>(R.id.add_group).setOnClickListener {
            var newName = getString(R.string.new_group_header)
            var counter = 1

            while (groups.contains(newName)) {
                counter++
                newName = getString(R.string.new_group_header_count, counter)
            }

            groups.add(newName)
            groups.sortBy { it.lowercase() }
            groupsManager.saveGroups(groups)
            prefs.setBoolean("group_visibility_$newName", true)

            // Rebuild UI
            groupsContainer.removeAllViews()
            groups.forEach { g -> addGroupRow(g, groupsContainer, groups) }
        }
    }

    private fun addGroupRow(group: String, container: LinearLayout, groups: MutableList<String>) {
        val row = layoutInflater.inflate(R.layout.list_item_group, container, false)
        FontManager.applyFont(this, row)
        val nameEdit = row.findViewById<EditText>(R.id.group_name)
        val deleteBtn = row.findViewById<FrameLayout>(R.id.delete_group)
        val toggleBtn = row.findViewById<FrameLayout>(R.id.toggle_visibility)
        val toggleBtnImg = row.findViewById<ImageView>(R.id.toggle_visibility_img)

        nameEdit.setText(group)
        nameEdit.tag = group

        // Visibility toggle
        toggleBtnImg.setImageResource(
            if (prefs.isGroupVisible(group)) R.drawable.icon_eye
            else R.drawable.icon_eye_cross
        )
        toggleBtn.setOnClickListener {
            val newVisibility = !prefs.isGroupVisible(group)
            prefs.setBoolean("group_visibility_$group", newVisibility)
            toggleBtnImg.setImageResource(
                if (newVisibility) R.drawable.icon_eye else R.drawable.icon_eye_cross
            )
        }

        // Rename
        nameEdit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val oldName = nameEdit.tag as String
                val newName = s?.toString()?.trim() ?: return
                if (oldName != newName && newName.isNotEmpty()) {
                    groupsManager.renameGroup(oldName, newName)
                    val index =
                        groups.indexOfFirst { it.trim().equals(oldName.trim(), ignoreCase = true) }
                    if (index != -1) groups[index] = newName
                    nameEdit.tag = newName
                }
            }
        })

        // Delete group
        deleteBtn.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_groups_delete, null)
            FontManager.applyFont(this, dialogView)
            val dialog = android.app.Dialog(this)
            dialog.setContentView(dialogView)
            dialog.setCancelable(true)

            val yesButton = dialogView.findViewById<WdButton>(R.id.yes_button)
            val noButton = dialogView.findViewById<WdButton>(R.id.no_button)

            yesButton.setOnClickListener {
                val groupName = nameEdit.text.toString()
                groupsManager.deleteGroup(groupName)
                groups.remove(groupName)
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

    // ------------------- Helpers -------------------
    private fun setupButton(id: Int, action: () -> Unit) {
        findViewById<View>(id).setOnClickListener { action() }
    }

    private fun openIntent(intent: Intent, errorMsg: String) {
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
        else Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
    }
}