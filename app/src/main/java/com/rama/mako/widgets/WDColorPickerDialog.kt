package com.rama.mako.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.Toast
import com.rama.mako.R
import com.rama.mako.managers.ThemeManager
import com.rama.mako.widgets.WdButton
import com.rama.mako.widgets.color.HSVSquareView
import com.rama.mako.widgets.color.HueStripView

object ColorPickerDialog {

    fun show(
        activity: Activity,
        initialColor: Int,
        onColorSelected: (Int) -> Unit
    ) {

        val dialog = Dialog(activity)

        val view = LayoutInflater.from(activity)
            .inflate(R.layout.wd_color_picker_dialog, null)

        dialog.setContentView(view)
        dialog.window?.setLayout(
            MATCH_PARENT,
            WRAP_CONTENT
        )

        ThemeManager.applyTheme(activity, view)

        // Views
        val preview = view.findViewById<View>(R.id.preview)
        val hexInput = view.findViewById<EditText>(R.id.hex_input)
        val applyButton = view.findViewById<WdButton>(R.id.apply_button)
        val closeButton = view.findViewById<WdButton>(R.id.close_button)

        val hsvSquare = view.findViewById<HSVSquareView>(R.id.hsv_square)
        val hueSlider = view.findViewById<HueStripView>(R.id.hue_slider)

        // HSV state (single source of truth)
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(initialColor, hsv)

        var hue = hsv[0]
        var saturation = hsv[1]
        var value = hsv[2]

        fun updateUI() {
            val color = Color.HSVToColor(floatArrayOf(hue, saturation, value))

            preview.background.setTint(color)

            hexInput.setText(
                String.format("#%06X", 0xFFFFFF and color)
            )

            hsvSquare.setHue(hue)
        }

        // init
        updateUI()

        // Hue slider → updates square + preview
        hueSlider.onHueChanged = { newHue ->
            hue = newHue
            updateUI()
        }

        // HSV square → updates S/V + preview
        hsvSquare.onSaturationValueChanged = { s, v ->
            saturation = s
            value = v
            updateUI()
        }

        // Hex input sync (optional manual override)
        hexInput.setOnEditorActionListener { _, _, _ ->
            try {
                val color = Color.parseColor(hexInput.text.toString())
                Color.colorToHSV(color, hsv)

                hue = hsv[0]
                saturation = hsv[1]
                value = hsv[2]

                updateUI()

            } catch (_: Exception) {
                Toast.makeText(activity, activity.getString(R.string.toast_invalid_color), Toast.LENGTH_SHORT).show()
            }

            true
        }

        // Apply
        applyButton.setOnClickListener {
            val color = Color.HSVToColor(floatArrayOf(hue, saturation, value))
            onColorSelected(color)
            dialog.dismiss()
        }

        // Close
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}