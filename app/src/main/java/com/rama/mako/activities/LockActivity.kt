package com.rama.mako.activities

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.rama.mako.CsActivity
import com.rama.mako.R

class LockActivity : CsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.view_lock)

        val root = findViewById<View>(android.R.id.content)
        applyEdgeToEdgePadding(root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    override fun onResume() {
        super.onResume()
        val buttons = listOf(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)
        val shuffled = (0..9).shuffled()

        buttons.forEachIndexed { index, button ->
            val digit = shuffled[index]
            button.text = digit.toString()

            button.setOnClickListener {
                appendDigit(digit)
            }
        }
    }
}