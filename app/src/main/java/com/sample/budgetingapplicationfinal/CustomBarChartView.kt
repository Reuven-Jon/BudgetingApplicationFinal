package com.sample.budgetingapplicationfinal

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.text.DecimalFormat

/**
 * Simple bar chart: one bar per entry in `dataMap`.
 * Keys = labels (“Income”, “Expense”), values = totals.
 */
class CustomBarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet?=null, defStyle:Int=0
) : View(context, attrs, defStyle) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 32f
        color = Color.DKGRAY
    }
    private val currencyFormat = DecimalFormat("R#,##0.00")

    // Map from label→value in Rands
    private var dataMap: Map<String, Float> = emptyMap()

    /** Call this to update and redraw the bars */
    fun setData(data: Map<String, Float>) {
        dataMap = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataMap.isEmpty()) {
            // No data placeholder
            labelPaint.textSize = 48f
            canvas.drawText("No Data", width/2f, height/2f, labelPaint)
            return
        }

        // Prepare values
        val labels = dataMap.keys.toList()
        val values = labels.map { dataMap[it]!! }
        val maxVal = values.maxOrNull() ?: 0f

        // guard zero max
        if (maxVal <= 0f) {
            labelPaint.textSize = 48f
            canvas.drawText("No Data", width/2f, height/2f, labelPaint)
            return
        }

        // Compute bar widths & positions
        val barCount = labels.size
        val space = width / (barCount * 2 + 1).toFloat() // spacing
        val barWidth = space

        val chartBottom = height * 0.8f
        val chartHeight = height * 0.75f // leave top margin

        labels.forEachIndexed { i, label ->
            val xStart = space * (1 + 2*i)
            val xEnd = xStart + barWidth

            val value = dataMap[label]!!
            val ratio = value / maxVal
            val barTop = chartBottom - (chartHeight * ratio)

            // choose color
            paint.color = if (label.contains("Income", true))
                Color.rgb(76,175,80)
            else
                Color.rgb(244,67,54)

            // draw bar
            canvas.drawRect(xStart, barTop, xEnd, chartBottom, paint)

            // value label above bar
            labelPaint.textSize = 24f
            canvas.drawText(
                currencyFormat.format(value.toDouble()),
                (xStart + xEnd)/2,
                barTop - 8f,
                labelPaint
            )

            // label below bar
            labelPaint.textSize = 32f
            canvas.drawText(
                label,
                (xStart + xEnd)/2,
                chartBottom + 40f,
                labelPaint
            )
        }
    }
}
