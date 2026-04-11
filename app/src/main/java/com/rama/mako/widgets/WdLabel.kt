package com.rama.mako.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.rama.mako.R

class WdLabel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val iconImage: ImageView
    private val iconText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.wd_label, this, true)
        iconImage = findViewById(R.id.icon_image)
        iconText = findViewById(R.id.icon_text)

        attrs?.let { setAttrs(context, it) }
    }

    private fun setAttrs(context: Context, attrs: AttributeSet) {
        for (i in 0 until attrs.attributeCount) {
            val name = attrs.getAttributeName(i)
            val value = attrs.getAttributeValue(i)
            when (name) {
                "text" -> {
                    val resId = attrs.getAttributeResourceValue(i, 0)
                    if (resId != 0) {
                        iconText.text = context.getString(resId)
                    } else {
                        iconText.text = attrs.getAttributeValue(i) // fallback
                    }
                }

                "icon" -> {
                    val resId = attrs.getAttributeResourceValue(i, 0)
                    if (resId != 0) iconImage.setImageResource(resId)
                }
            }
        }
    }

    fun setText(text: String) {
        iconText.text = text
    }

    fun setIcon(resId: Int) {
        iconImage.setImageResource(resId)
    }
}