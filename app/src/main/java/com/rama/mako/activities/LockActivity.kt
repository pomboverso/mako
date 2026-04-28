package com.rama.mako.activities

import android.content.Intent
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
        setupKeypad()
        clearPin()
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

    override fun onBackPressed() {
        navigateToHome()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun setupKeypad() {
        val isRandomized = prefs.getBoolean(
            com.rama.mako.managers.PrefsManager.PrefKeys.SECURITY_KEYPAD_RANDOMIZED,
            true
        )

        val digits = if (isRandomized) (0..9).shuffled() else (0..9).toList()

        buttons.forEachIndexed { index, button ->
            val digit = digits[index]
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
            navigateToHome()
        }
    }

    private fun validatePin() {
        val savedPin = prefs.getPin()

        if (savedPin.isEmpty()) {
            finish()
            return
        }

        if (pinBuilder.toString() == savedPin) {
            setResult(RESULT_OK)
            finish()
        } else {
            clearPin()
            setupKeypad()
        }
    }
}