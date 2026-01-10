package com.rama.mako

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.util.Locale
import android.text.format.DateFormat
import java.util.Calendar

class ClockManager(
    private val timeTextView: TextView,
    private val dateTextView: TextView,
    private val prefs: SharedPreferences
) {
    private val handler = Handler(Looper.getMainLooper())
    private val calendar = Calendar.getInstance()

    private val runnable = object : Runnable {
        override fun run() {
            val showClock = prefs.getBoolean("show_clock", true)
            val showDate = prefs.getBoolean("show_date", true)

            calendar.timeInMillis = System.currentTimeMillis()
            val locale = Locale.getDefault()

            if (showClock) {
                val timeFormat = DateFormat.getTimeFormat(timeTextView.context)
                timeTextView.text = timeFormat.format(calendar.time)
            }

            if (showDate) {
                val dateFormat = DateFormat.getDateFormat(dateTextView.context)
                val weekday = calendar.getDisplayName(
                    Calendar.DAY_OF_WEEK,
                    Calendar.LONG,
                    locale
                ) ?: ""

                val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)

                dateTextView.text =
                    "$weekday :: ${dateFormat.format(calendar.time)} :: $dayOfYear/$totalDays"
                        .uppercase(locale)
            }

            handler.postDelayed(this, 1000)
        }
    }

    fun start() = handler.post(runnable)
    fun stop() = handler.removeCallbacks(runnable)
}


