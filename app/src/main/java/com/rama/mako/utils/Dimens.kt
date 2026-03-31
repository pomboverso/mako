package com.rama.mako.utils

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

fun Context.dp(value: Int): Int =
    (value * resources.displayMetrics.density).roundToInt()

fun Context.sp(value: Float): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        value,
        resources.displayMetrics
    ).roundToInt()