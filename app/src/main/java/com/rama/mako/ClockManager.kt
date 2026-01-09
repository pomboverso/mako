package com.rama.mako

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class ClockManager(
    private val timeTextView: TextView,
    private val dateTextView: TextView,
    private val prefs: SharedPreferences
) {
    private val handler = Handler(Looper.getMainLooper())

    private val timeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")

    private val dateFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val runnable = object : Runnable {
        override fun run() {
            val showClock = prefs.getBoolean("show_clock", true)
            val showDate = prefs.getBoolean("show_date", true)

            val now = LocalDateTime.now()
            val locale = Locale.getDefault()

            if (showClock) {
                timeTextView.text = now.format(timeFormatter)
            }

            if (showDate) {
                val weekday = now.dayOfWeek
                    .getDisplayName(TextStyle.FULL, locale)

                val dayOfYear = now.dayOfYear
                val totalDays = now.toLocalDate().lengthOfYear()

                dateTextView.text =
                    "$weekday :: ${now.format(dateFormatter)} :: $dayOfYear/$totalDays"
                        .uppercase(locale)
            }

            handler.postDelayed(this, 1000)
        }
    }

    fun start() = handler.post(runnable)
    fun stop() = handler.removeCallbacks(runnable)
}

