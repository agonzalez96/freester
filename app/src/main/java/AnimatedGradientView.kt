package com.example.freester

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class AnimatedGradientView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var angle = 0f

    private val colorStart1 = Color.parseColor("#00CFC1")
    private val colorStart2 = Color.parseColor("#5EFCE8")
    private val colorEnd1 = Color.parseColor("#0B132B")
    private val colorEnd2 = Color.parseColor("#3A0CA3")

    fun setAngle(value: Float) {
        angle = value
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        if (w == 0f || h == 0f) return

        val cx = w / 2
        val cy = h / 2

        val rad = Math.toRadians(angle.toDouble())
        val dx = cos(rad).toFloat()
        val dy = sin(rad).toFloat()

        val startX = cx - dx * w
        val startY = cy - dy * h
        val endX = cx + dx * w
        val endY = cy + dy * h

        val fraction = 0.5f + 0.5f * sin(rad).toFloat()

        val startColor = blend(colorStart1, colorStart2, fraction)
        val endColor = blend(colorEnd1, colorEnd2, fraction)

        paint.shader = LinearGradient(
            startX, startY,
            endX, endY,
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(0f, 0f, w, h, paint)
    }

    private fun blend(c1: Int, c2: Int, t: Float): Int {
        val r = (Color.red(c1) * (1 - t) + Color.red(c2) * t).toInt()
        val g = (Color.green(c1) * (1 - t) + Color.green(c2) * t).toInt()
        val b = (Color.blue(c1) * (1 - t) + Color.blue(c2) * t).toInt()
        return Color.rgb(r, g, b)
    }
}
