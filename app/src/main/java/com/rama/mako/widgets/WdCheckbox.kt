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
        checkBox.setSaveEnabled(false)
        checkBox.id = generateViewId()
        attrs?.let { setAttrs(context, it) }
    }

    private fun setAttrs(context: Context, attrs: AttributeSet) {
        for (i in 0 until attrs.attributeCount) {
            val name = attrs.getAttributeName(i)
            when (name) {
                "text" -> {
                    val resId = attrs.getAttributeResourceValue(i, 0)
                    if (resId != 0) {
                        checkBox.text = context.getString(resId)
                    }
                }
            }
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