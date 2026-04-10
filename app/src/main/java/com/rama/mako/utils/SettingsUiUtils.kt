package com.rama.mako.utils

import android.app.Activity
import android.content.Intent
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast
import com.rama.mako.activities.SettingsActivity

object SettingsUiUtils {

    fun setupButton(activity: SettingsActivity, id: Int, action: () -> Unit) {
        val view = activity.findViewById<View>(id)
        setClickWithHaptics(view, action)
    }

    fun setClickWithHaptics(view: View, action: () -> Unit) {
        view.setOnClickListener {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            action()
        }
    }

    fun openIntent(activity: Activity, intent: Intent, error: String) {
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            Toast.makeText(activity, error, Toast.LENGTH_LONG).show()
        }
    }
}