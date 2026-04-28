package com.rama.mako.activities.settings

import com.rama.mako.R
import com.rama.mako.activities.SettingsActivity
import com.rama.mako.managers.PrefsManager.PrefKeys
import com.rama.mako.widgets.WdCheckbox
import com.rama.mako.widgets.WdPinField

class SettingsPinController(private val activity: SettingsActivity) {

    private val prefs get() = activity.prefs

    fun setup() {
        setupLockSettingsToggle()
        setupRandomizedKeypadToggle()
        setupPinField()
    }

    private fun setupLockSettingsToggle() {
        val checkbox = activity.findViewById<WdCheckbox>(R.id.lock_settings)
        val isEnabled = prefs.getBoolean(PrefKeys.SECURITY_KEYPAD_VISIBLE, false)
        checkbox.setChecked(isEnabled)

        checkbox.setOnCheckedChangeListener { checked ->
            prefs.setBoolean(PrefKeys.SECURITY_KEYPAD_VISIBLE, checked)
        }
    }

    private fun setupRandomizedKeypadToggle() {
        val checkbox = activity.findViewById<WdCheckbox>(R.id.randomized_keypad)
        val isRandomized = prefs.getBoolean(PrefKeys.SECURITY_KEYPAD_RANDOMIZED, true)
        checkbox.setChecked(isRandomized)

        checkbox.setOnCheckedChangeListener { checked ->
            prefs.setBoolean(PrefKeys.SECURITY_KEYPAD_RANDOMIZED, checked)
        }
    }

    private fun setupPinField() {
        val pinField = activity.findViewById<WdPinField>(R.id.pin_field_widget)

        // Pre-fill with a masked placeholder if a pin is already saved
        val existingPin = prefs.getPin()
        if (existingPin.isNotEmpty()) {
            pinField.setPin(existingPin)
        }

        pinField.onPinSaved = { pin ->
            if (pin.length >= 4) {
                prefs.setPin(pin)
                activity.runOnUiThread {
                    android.widget.Toast.makeText(
                        activity,
                        activity.getString(R.string.pin_saved_label),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                activity.runOnUiThread {
                    android.widget.Toast.makeText(
                        activity,
                        activity.getString(R.string.pin_too_short_label),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
