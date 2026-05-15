package com.rama.mako.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import com.rama.mako.R

class WdColorPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val title: TextView
    private val hexValue: TextView
    private val colorPreview: View

    private var currentColor: Int = Color.WHITE

    private var listener: ((Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL

        LayoutInflater.from(context)
            .inflate(R.layout.wd_color_picker, this, true)

        title = findViewById(R.id.title)
        hexValue = findViewById(R.id.hexValue)
        colorPreview = findViewById(R.id.colorPreview)

        attrs?.let {

            val ta = context.obtainStyledAttributes(
                it,
                intArrayOf(R.attr.text)
            )

            title.text = ta.getString(0) ?: ""

            ta.recycle()
        }

        isClickable = true
        isFocusable = true
        isFocusableInTouchMode = false

        updateUI()

        setOnClickListener {
            listener?.invoke(currentColor)
        }
    }

    fun setTitle(text: String) {
        title.text = text
    }

    fun setColor(color: Int) {
        currentColor = color
        updateUI()
    }

    fun getColor(): Int {
        return currentColor
    }

    fun setOnColorClickListener(callback: (Int) -> Unit) {
        listener = callback
    }

    private fun updateUI() {
        val hex = String.format("#%06X", 0xFFFFFF and currentColor)

        hexValue.text = hex

        colorPreview.background.setTint(currentColor)
    }
}