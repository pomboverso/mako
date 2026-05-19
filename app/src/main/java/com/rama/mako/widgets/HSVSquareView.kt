package com.rama.mako.widgets.color

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.concurrent.thread

class HSVSquareView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var bitmap: Bitmap? = null
    private var hue: Float = 0f
    private var pendingHue: Float? = null
    private var isGenerating = false

    var onSaturationValueChanged: ((Float, Float) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        generateBitmapAsync(hue, w, h)
    }

    private fun generateBitmapAsync(forHue: Float, w: Int, h: Int) {
        if (w <= 0 || h <= 0) return

        // Render at half resolution for speed, scale up via paint
        val sampleW = (w / 2).coerceAtLeast(1)
        val sampleH = (h / 2).coerceAtLeast(1)

        isGenerating = true

        thread {
            val pixels = IntArray(sampleW * sampleH)
            val hsv = floatArrayOf(forHue, 1f, 1f)
            var index = 0

            for (y in 0 until sampleH) {
                val value = 1f - (y / sampleH.toFloat())
                for (x in 0 until sampleW) {
                    hsv[1] = x / sampleW.toFloat()
                    hsv[2] = value
                    pixels[index++] = Color.HSVToColor(hsv)
                }
            }

            val bmp = Bitmap.createBitmap(sampleW, sampleH, Bitmap.Config.RGB_565)
            bmp.setPixels(pixels, 0, sampleW, 0, 0, sampleW, sampleH)
            val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
            bmp.recycle()

            post {
                isGenerating = false
                bitmap = scaled
                invalidate()

                // If hue changed while we were rendering, re-render with the latest value
                val next = pendingHue
                if (next != null && next != forHue) {
                    pendingHue = null
                    generateBitmapAsync(next, w, h)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        bitmap?.let { canvas.drawBitmap(it, 0f, 0f, paint) }
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
        if (width <= 0 || height <= 0) return
        if (isGenerating) {
            // Drop intermediate hues while busy, only keep the latest
            pendingHue = h
        } else {
            generateBitmapAsync(h, width, height)
        }
    }
}