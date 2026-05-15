package com.rama.mako.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.Toast
import com.rama.mako.R
import com.rama.mako.managers.ThemeManager
import com.rama.mako.widgets.WdButton

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

        val preview = view.findViewById<android.view.View>(R.id.preview)
        val hexInput = view.findViewById<EditText>(R.id.hex_input)
        val applyButton = view.findViewById<WdButton>(R.id.apply_button)
        val closeButton = view.findViewById<WdButton>(R.id.close_button)

        preview.background.setTint(initialColor)

        hexInput.setText(
            String.format("#%06X", 0xFFFFFF and initialColor)
        )

        applyButton.setOnClickListener {

            val raw = hexInput.text.toString().trim()

            try {
                val color = Color.parseColor(raw)

                preview.background.setTint(color)

                onColorSelected(color)

                dialog.dismiss()

            } catch (_: Exception) {

                Toast.makeText(
                    activity,
                    "Invalid color",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}