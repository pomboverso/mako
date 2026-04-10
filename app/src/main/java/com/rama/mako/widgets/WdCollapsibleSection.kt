package com.rama.mako.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.rama.mako.R
import com.rama.mako.managers.PrefsManager
import com.rama.mako.managers.PrefsManager.PrefKeys

class WdCollapsibleSection @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val header: LinearLayout
    private val indicator: TextView
    private val labelView: TextView
    private val content: LinearLayout

    private var key: String? = null
    private var defaultExpanded: Boolean = true
    private val prefs = PrefsManager.getInstance(context)

    init {
        orientation = VERTICAL

        LayoutInflater.from(context)
            .inflate(R.layout.wd_collapsible_section, this, true)

        header = findViewById(R.id.section_header)
        indicator = findViewById(R.id.section_indicator)
        labelView = findViewById(R.id.section_label)
        content = findViewById(R.id.section_content)

        // Read XML attrs
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.WdCollapsibleSection)

            labelView.text = ta.getString(R.styleable.WdCollapsibleSection_label) ?: ""

            key = resolveKey(
                ta.getString(R.styleable.WdCollapsibleSection_key)
            )

            defaultExpanded = ta.getBoolean(
                R.styleable.WdCollapsibleSection_defaultExpanded,
                true
            )

            ta.recycle()
        }

        val expanded = loadState()

        applyState(expanded)

        header.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

            val newState = !isExpanded()
            applyState(newState)
            saveState(newState)
        }
    }

    private fun resolveKey(raw: String?): String? {
        if (raw == null) return null

        return try {
            PrefsManager.PrefKeys::class.java
                .getDeclaredField(raw)
                .get(null) as? String
        } catch (e: Exception) {
            null
        }
    }

    // Inflate children inside component
    override fun onFinishInflate() {
        super.onFinishInflate()

        // Move all children (except internal layout) into content
        val children = mutableListOf<View>()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.id != R.id.section_root) {
                children.add(child)
            }
        }

        children.forEach {
            removeView(it)
            content.addView(it)
        }
    }

    private fun isExpanded(): Boolean {
        return content.visibility == View.VISIBLE
    }

    private fun applyState(expanded: Boolean) {
        content.visibility = if (expanded) View.VISIBLE else View.GONE
        indicator.text = if (expanded) "[-]" else "[+]"
    }

    private fun saveState(expanded: Boolean) {
        key?.let {
            prefs.setBoolean("$it", expanded)
        }
    }

    private fun loadState(): Boolean {
        return key?.let {
            prefs.getBoolean("$it", defaultExpanded)
        } ?: defaultExpanded
    }
}