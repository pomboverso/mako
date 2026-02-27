package com.rama.mako.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.rama.mako.R

class WdButtonNav @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val navText: TextView
    private val rootFrame: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.wd_button_nav, this, true)
        rootFrame = findViewById(R.id.root)
        navText = findViewById(R.id.button_label)

        // Forward clicks from the inner FrameLayout to this custom view
        rootFrame.setOnClickListener { performClick() }

        attrs?.let {
            val a = context.obtainStyledAttributes(it, intArrayOf(android.R.attr.text))
            a.getString(0)?.let { text -> navText.text = text }
            a.recycle()
        }
    }

    override fun performClick(): Boolean {
        // This is necessary to properly handle accessibility events
        super.performClick()
        return true
    }

    fun setText(text: String) {
        navText.text = text
    }
}