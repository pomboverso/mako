package com.rama.mako.activities

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.rama.mako.R

class ListItemActivity @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val iconImage: ImageView
    private val iconText: TextView

    init {
        // Inflate the XML layout
        LayoutInflater.from(context).inflate(R.layout.list_item, this, true)
        iconImage = findViewById(R.id.icon_image)
        iconText = findViewById(R.id.icon_text)

        // Read custom attributes from XML
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ListItem, 0, 0)
            val text = typedArray.getString(R.styleable.ListItem_text)
            val iconRes = typedArray.getResourceId(R.styleable.ListItem_icon, 0)

            iconText.text = text
            if (iconRes != 0) {
                iconImage.setImageResource(iconRes)
            }

            typedArray.recycle()
        }
    }
}
