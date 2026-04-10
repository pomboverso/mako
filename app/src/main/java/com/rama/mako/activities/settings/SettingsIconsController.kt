package com.rama.mako.activities.settings

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.FontManager
import com.rama.mako.managers.PrefsManager
import com.rama.mako.utils.SettingsUiUtils
import com.rama.mako.widgets.WdButton

class SettingsIconsController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs
    private val iconManager get() = activity.iconManager

    fun setup() {
        setupIconsSection()
    }

    private fun setupIconsSection() {
        val group = activity.findViewById<RadioGroup>(R.id.icon_source_group)
        val selectIconPackButton = activity.findViewById<WdButton>(R.id.select_icon_pack_button)

        when (prefs.getIconSource()) {
            PrefsManager.IconSource.NONE -> group.check(R.id.icon_source_none)
            PrefsManager.IconSource.MONOCHROME -> group.check(R.id.icon_source_monochrome)
            PrefsManager.IconSource.ICON_PACK -> group.check(R.id.icon_source_icon_pack)
            else -> group.check(R.id.icon_source_system)
        }

        refreshIconPackSection()

        group.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.icon_source_none -> prefs.setIconSource(PrefsManager.IconSource.NONE)
                R.id.icon_source_monochrome -> prefs.setIconSource(PrefsManager.IconSource.MONOCHROME)
                R.id.icon_source_icon_pack -> prefs.setIconSource(PrefsManager.IconSource.ICON_PACK)
                else -> prefs.setIconSource(PrefsManager.IconSource.SYSTEM)
            }

            if (id == R.id.icon_source_monochrome && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.monochrome_fallback_toast),
                    Toast.LENGTH_SHORT
                ).show()
            }

            refreshIconPackSection()
        }

        SettingsUiUtils.setClickWithHaptics(selectIconPackButton) {
            showIconPackPickerDialog()
        }
    }

    private fun refreshIconPackSection() {
        val iconPackControls = activity.findViewById<View>(R.id.icon_pack_controls)
        val selectedIconPack = activity.findViewById<TextView>(R.id.selected_icon_pack_label)

        val currentPackage = prefs.getIconPackPackage()
        val currentLabel =
            if (currentPackage.isBlank()) null else iconManager.getIconPackLabel(currentPackage)

        selectedIconPack.text = when {
            currentLabel != null -> activity.getString(
                R.string.selected_icon_pack_label,
                currentLabel
            )

            currentPackage.isNotBlank() -> activity.getString(
                R.string.selected_icon_pack_label,
                currentPackage
            )

            else -> activity.getString(R.string.icon_pack_not_selected_label)
        }

        iconPackControls.visibility =
            if (prefs.getIconSource() == PrefsManager.IconSource.ICON_PACK) View.VISIBLE else View.GONE
    }

    private fun showIconPackPickerDialog() {
        val iconPacks = iconManager.getInstalledIconPacks()
        if (iconPacks.isEmpty()) {
            Toast.makeText(
                activity,
                activity.getString(R.string.no_icon_pack_found_label),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_pick_icon_pack, null)
        FontManager.applyFont(activity, dialogView)

        val dialog = android.app.Dialog(activity).apply {
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
                val view = convertView ?: activity.layoutInflater.inflate(
                    R.layout.list_item_icon_pack, parent, false
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

        listView.setOnItemClickListener { _, itemView, position, _ ->
            itemView.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            val selectedIconPack = iconPacks[position]
            prefs.setIconPackPackage(selectedIconPack.packageName)
            prefs.setIconSource(PrefsManager.IconSource.ICON_PACK)
            activity.findViewById<RadioGroup>(R.id.icon_source_group)
                .check(R.id.icon_source_icon_pack)
            Toast.makeText(
                activity,
                activity.getString(R.string.icon_pack_selected_toast, selectedIconPack.label),
                Toast.LENGTH_SHORT
            ).show()
            refreshIconPackSection()
        }

        SettingsUiUtils.setClickWithHaptics(closeBtn) {
            refreshIconPackSection()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}