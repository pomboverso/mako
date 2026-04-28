package com.rama.mako.activities

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.rama.mako.CsActivity
import com.rama.mako.R

class LockActivity : CsActivity() {

    private lateinit var pinDisplay: EditText
    private lateinit var buttons: List<Button>

    private val pinBuilder = StringBuilder()

    private val correctPin = "1234" // TODO: replace with secure storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.view_lock)

        val root = findViewById<View>(android.R.id.content)
        applyEdgeToEdgePadding(root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setupViews()
        setupActions()
    }

    private fun setupViews() {
        pinDisplay = findViewById(R.id.pin_display)

        buttons = listOf(
            findViewById(R.id.btn0),
            findViewById(R.id.btn1),
            findViewById(R.id.btn2),
            findViewById(R.id.btn3),
            findViewById(R.id.btn4),
            findViewById(R.id.btn5),
            findViewById(R.id.btn6),
            findViewById(R.id.btn7),
            findViewById(R.id.btn8),
            findViewById(R.id.btn9),
        )
    }

    override fun onResume() {
        super.onResume()
        shuffleKeypad()
        clearPin()
    }

    private fun shuffleKeypad() {
        val shuffled = (0..9).shuffled()

        buttons.forEachIndexed { index, button ->
            val digit = shuffled[index]
            button.text = digit.toString()

            button.setOnClickListener {
                appendDigit(digit)
            }
        }
    }

    private fun appendDigit(digit: Int) {
        if (pinBuilder.length >= 10) return

        pinBuilder.append(digit)
        updateDisplay()
    }

    private fun updateDisplay() {
        pinDisplay.setText("•".repeat(pinBuilder.length))
    }

    private fun clearPin() {
        pinBuilder.clear()
        pinDisplay.setText("")
    }

    private fun setupActions() {
        findViewById<View>(R.id.clear_button).setOnClickListener {
            clearPin()
        }

        findViewById<View>(R.id.unlock_button).setOnClickListener {
            validatePin()
        }

        findViewById<View>(R.id.close_button).setOnClickListener {
            finish()
        }
    }

    private fun validatePin() {
        if (pinBuilder.toString() == correctPin) {
            finish()
        } else {
            clearPin()
            shuffleKeypad() // re-randomize on failure
        }
    }
}