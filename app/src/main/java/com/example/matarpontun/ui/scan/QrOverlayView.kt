package com.example.matarpontun.ui.scan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class QrOverlayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private var rect: RectF? = null

    fun setRect(r: RectF?) {
        rect = r
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        rect?.let { canvas.drawRect(it, paint) }
    }
}
