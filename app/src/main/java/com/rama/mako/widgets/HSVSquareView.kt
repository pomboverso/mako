package com.rama.mako.widgets.color

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class HSVSquareView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var bitmap: Bitmap? = null
    private var hue: Float = 0f

    var onSaturationValueChanged: ((Float, Float) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        generateBitmap(w, h)
    }

    private fun generateBitmap(w: Int, h: Int) {

        if (w <= 0 || h <= 0) return

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(w * h)

        val hsv = floatArrayOf(hue, 1f, 1f)

        var index = 0

        for (y in 0 until h) {
            val value = 1f - (y / h.toFloat())

            for (x in 0 until w) {
                val saturation = x / w.toFloat()

                hsv[0] = hue
                hsv[1] = saturation
                hsv[2] = value

                pixels[index++] = Color.HSVToColor(hsv)
            }
        }

        bitmap?.setPixels(pixels, 0, w, 0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {

            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {

                val s = (event.x / width).coerceIn(0f, 1f)
                val v = 1f - (event.y / height).coerceIn(0f, 1f)

                onSaturationValueChanged?.invoke(s, v)

                return true
            }
        }

        return super.onTouchEvent(event)
    }

    fun setHue(h: Float) {
        hue = h
        generateBitmap(width, height)
        invalidate()
    }
}