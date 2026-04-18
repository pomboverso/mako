package com.rama.mako.activities.settings

import android.view.View
import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.PrefsManager.PrefKeys
import com.rama.mako.widgets.WdCheckbox

class SettingsCheckboxController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs

    fun setup() {
        bindWdCheckbox(R.id.show_date, PrefKeys.DATE_VISIBLE, false, listOf(R.id.show_year_day))
        bindWdCheckbox(
            R.id.show_search,
            PrefKeys.APPS_SEARCH,
            false,
            dependentViewIds = listOf(R.id.always_show_search)
        )
        bindWdCheckbox(R.id.always_show_search, PrefKeys.APPS_SEARCH_ALWAYS_VISIBLE, false)
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
            listOf(R.id.show_battery_temperature, R.id.show_battery_charge_status)
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
        val checkbox = activity.findViewById<WdCheckbox>(wdCheckboxId)
        val dependents = dependentViewIds?.map { activity.findViewById<View>(it) }

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
}