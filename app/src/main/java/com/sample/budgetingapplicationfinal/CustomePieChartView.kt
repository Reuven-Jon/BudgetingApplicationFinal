package com.sample.budgetingapplicationfinal

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Draws a donut‐style pie chart (Income vs Expense) with:
 * - animationProgress (0→1) to “grow” the slices on load
 * - touch highlighting (tap a slice to offset it outward)
 *
 * Public method: setData(incomeSum, expenseSum)
 */
class CustomPieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }
    private val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.DKGRAY
    }
    private val legendTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
    }
    private val legendSwatchPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Data values
    private var incomeValue: Float = 0f
    private var expenseValue: Float = 0f

    // For drawing the pie circle
    private val pieBounds = RectF()

    // Legend swatch size in pixels
    private val legendSwatchSize = 40f

    // Number formats
    private val currencyFormat = DecimalFormat("R#,##0.00")
    private val percentFormat = DecimalFormat("0.#")

    // Animate slices from 0→full using this progress
    var animationProgress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    // 0=none, 1=income slice highlighted, 2=expense slice highlighted
    private var highlightedSlice: Int = 0

    /**
     * Update the raw data. Resets animationProgress→0 and clears highlight.
     */
    fun setData(income: Double, expense: Double) {
        incomeValue = income.toFloat()
        expenseValue = expense.toFloat()
        highlightedSlice = 0
        animationProgress = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val centerY = h / 2f

        // 1) Compute a “safe” pie diameter
        val maxPieW = w * 0.5f           // only use half the width for the pie
        val maxPieH = h * 0.8f           // only 80% of height
        val diameter = minOf(maxPieW, maxPieH)
        val radius = diameter / 2f

        // 2) Center the pie at x=25% of width, y=centerY
        val pieCenterX = w * 0.25f
        val pieCenterY = centerY

        // 3) Define the bounding rect
        pieBounds.set(
            pieCenterX - radius,
            pieCenterY - radius,
            pieCenterX + radius,
            pieCenterY + radius
        )

        // 4) If no data, show gray circle + “No Data”
        if (incomeValue <= 0f && expenseValue <= 0f) {
            slicePaint.color = Color.LTGRAY
            canvas.drawOval(pieBounds, slicePaint)

            val holeR = radius * 0.6f
            canvas.drawCircle(pieCenterX, pieCenterY, holeR, holePaint)

            centerTextPaint.textSize = radius * 0.2f
            canvas.drawText(
                "No Data",
                pieCenterX,
                pieCenterY + (centerTextPaint.textSize / 3f),
                centerTextPaint
            )
            return
        }

        // 5) Compute raw angles & percentages
        val total = incomeValue + expenseValue
        val rawIncomeAngle = (incomeValue / total) * 360f
        val rawExpenseAngle = (expenseValue / total) * 360f
        val incomePct = (incomeValue / total) * 100f
        val expensePct = (expenseValue / total) * 100f

        // 6) Apply animationProgress
        val incomeAngle = rawIncomeAngle * animationProgress
        val expenseAngle = rawExpenseAngle * animationProgress

        // 7) Compute highlight offsets (12dp)
        val offsetDist = 12f * resources.displayMetrics.density
        var incOffsetX = 0f
        var incOffsetY = 0f
        var expOffsetX = 0f
        var expOffsetY = 0f

        if (highlightedSlice == 1) {
            // Compute mid-angle of income slice
            val midAngle = -90f + (incomeAngle / 2f)
            val rad = Math.toRadians(midAngle.toDouble())
            incOffsetX = (offsetDist * Math.cos(rad)).toFloat()
            incOffsetY = (offsetDist * Math.sin(rad)).toFloat()
        } else if (highlightedSlice == 2) {
            val midAngle = -90f + incomeAngle + (expenseAngle / 2f)
            val rad = Math.toRadians(midAngle.toDouble())
            expOffsetX = (offsetDist * Math.cos(rad)).toFloat()
            expOffsetY = (offsetDist * Math.sin(rad)).toFloat()
        }

        // 8) Draw income slice with offset
        slicePaint.color = Color.rgb(76, 175, 80) // green
        pieBounds.offset(incOffsetX, incOffsetY)
        canvas.drawArc(pieBounds, -90f, incomeAngle, true, slicePaint)
        pieBounds.offset(-incOffsetX, -incOffsetY)

        // 9) Draw expense slice with offset
        slicePaint.color = Color.rgb(244, 67, 54) // red
        pieBounds.offset(expOffsetX, expOffsetY)
        canvas.drawArc(pieBounds, -90f + incomeAngle, expenseAngle, true, slicePaint)
        pieBounds.offset(-expOffsetX, -expOffsetY)

        // 10) Draw hollow center
        val holeR = radius * 0.6f
        canvas.drawCircle(pieCenterX, pieCenterY, holeR, holePaint)

        // 11) Draw total in center
        centerTextPaint.textSize = radius * 0.25f
        val totalText = currencyFormat.format(total.toDouble())
        canvas.drawText(
            totalText,
            pieCenterX,
            pieCenterY + (centerTextPaint.textSize / 3f),
            centerTextPaint
        )

        // 12) Draw legend on right half (start at x=55% of view)
        val legendStartX = w * 0.55f
        val lineHeight = legendSwatchSize + 20f
        val legendTop = centerY - lineHeight

        // Income legend
        legendSwatchPaint.color = Color.rgb(76, 175, 80)
        canvas.drawRect(
            legendStartX,
            legendTop,
            legendStartX + legendSwatchSize,
            legendTop + legendSwatchSize,
            legendSwatchPaint
        )
        legendTextPaint.textSize = legendSwatchSize * 0.8f
        val incText = "${percentFormat.format(incomePct)}% Income"
        canvas.drawText(
            incText,
            legendStartX + legendSwatchSize + 12f,
            legendTop + (legendSwatchSize * 0.8f),
            legendTextPaint
        )

        // Expense legend
        val expTop = legendTop + lineHeight
        legendSwatchPaint.color = Color.rgb(244, 67, 54)
        canvas.drawRect(
            legendStartX,
            expTop,
            legendStartX + legendSwatchSize,
            expTop + legendSwatchSize,
            legendSwatchPaint
        )
        val expText = "${percentFormat.format(expensePct)}% Expense"
        canvas.drawText(
            expText,
            legendStartX + legendSwatchSize + 12f,
            expTop + (legendSwatchSize * 0.8f),
            legendTextPaint
        )
    }

    /**
     * Detect taps on the pie. If tapped inside the income slice, set highlightedSlice=1.
     * If inside expense slice, highlightedSlice=2. Otherwise, 0 (no highlight).
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            val w = width.toFloat()
            val h = height.toFloat()
            val pieCenterX = w * 0.25f
            val pieCenterY = h / 2f

            val dx = x - pieCenterX
            val dy = y - pieCenterY
            val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()

            // Compute current radius and hole
            val maxPieW = w * 0.5f
            val maxPieH = h * 0.8f
            val diameter = minOf(maxPieW, maxPieH)
            val radius = diameter / 2f
            val holeR = radius * 0.6f

            // If tap is within hole or outside pie, clear highlight
            if (dist < holeR || dist > radius) {
                highlightedSlice = 0
                invalidate()
                return true
            }

            // Convert to angle where 0° is at 12 o’clock
            var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            touchAngle = (touchAngle + 450f) % 360f

            // Compute raw angles * animationProgress
            val total = incomeValue + expenseValue
            val rawIncAngle = (incomeValue / total) * 360f * animationProgress
            val rawExpAngle = (expenseValue / total) * 360f * animationProgress

            highlightedSlice = when {
                touchAngle < rawIncAngle -> 1
                touchAngle < rawIncAngle + rawExpAngle -> 2
                else -> 0
            }
            invalidate()
            return true
        }
        return super.onTouchEvent(event)
    }
}
