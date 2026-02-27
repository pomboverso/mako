package com.rama.mako.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.FrameLayout
import com.rama.mako.R

class WdCheckbox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val checkBox: CheckBox

    init {
        LayoutInflater.from(context).inflate(R.layout.wd_checkbox, this, true)
        checkBox = findViewById(R.id.checkbox)

        // Read attribute from theme or XML if set
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, intArrayOf(R.attr.text))
            val label = typedArray.getString(0)
            setText(label ?: "")
            typedArray.recycle()
        }
    }

    fun setText(text: String) {
        checkBox.text = text
    }

    fun setChecked(checked: Boolean) {
        checkBox.isChecked = checked
    }

    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        checkBox.setOnCheckedChangeListener { _, isChecked -> listener(isChecked) }
    }

    fun isChecked(): Boolean = checkBox.isChecked
}