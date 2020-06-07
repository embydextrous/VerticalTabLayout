package com.iammert.tabscrollattacher

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

class SegmentedButton(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val textBounds = Rect()
    private val rectF: RectF
    private val paint: Paint
    private val textPaint: TextPaint = TextPaint()

    private var clipAmount: Float = 0f
    private var clipLeftToRight: Boolean = false

    private var staticLayout: StaticLayout? = null
    private var staticLayoutOverlay: StaticLayout? = null

    private var textSize: Float = 0f
    private var isTextAllCaps: Boolean = false
    private var textTypefaceResId: Int = 0
    private var hasTextColorOnSelection: Boolean = false
    private var textColorOnSelection: Int = 0
    private var buttonWidth: Int = 0
    private var buttonWeight: Float = 0f
    private var selectorColor = Color.BLACK
    private var hasDrawable = false
    private var hasDrawableHeight = false
    private var hasDrawableWidth = false
    private var hasDrawableTint = false
    private var hasDrawableTintOnSelection = false
    private var bitmapNormalColor: PorterDuffColorFilter? = null
    private var bitmapClipColor: PorterDuffColorFilter? = null
    private var bitmap: Bitmap? = null
    private var bitmapX = 0f
    private var bitmapY = 0f
    private var drawableWidth: Int = 0
    private var drawableHeight: Int = 0
    private var drawablePadding: Int = 0
    private var drawableGravity: DrawableGravity? = null
    private var drawableTintOnSelection: Int = 0
    private var textX = 0f
    private var textY = 0f

    var text: String? = null
        set(value) {
            field = if (isTextAllCaps) {
                value?.toUpperCase()
            } else {
                value
            }
            initText()
            invalidate()
        }
    var textColor: Int = 0
    var radius: Int = 0
    var drawable: Int = 0
        set(value) {
            field = value
            hasDrawable = true
            initBitmap()
        }
    var drawableTint: Int = 0
        set(color) {
            field = color
            if (color != Color.WHITE && color != Color.TRANSPARENT) {
                hasDrawableTint = true
                bitmapNormalColor = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }

    private lateinit var bitmapPaint: Paint

    init {
        if (attrs != null) {
            getAttributes(attrs)
        }

        initText()
        initBitmap()
        rectF = RectF()
        paint = Paint().apply {
            color = selectorColor
            isAntiAlias = true
        }
    }

    private fun initText() {
        textPaint.run {
            isAntiAlias = true
            this@run.textSize = this@SegmentedButton.textSize
            color = textColor
            if (textTypefaceResId != 0) {
                typeface = ResourcesCompat.getFont(context, textTypefaceResId)
            }
        }

        val width = textPaint.measureText(text).toInt()
        staticLayout = StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
        staticLayoutOverlay = StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
    }

    private fun initBitmap() {
        if (!hasDrawable || drawable == 0)
            return

        bitmap = getBitmapFromDrawableResource()
        if (hasDrawableWidth || hasDrawableHeight) {
            bitmap = getResizedBitmap(bitmap!!, drawableWidth, drawableHeight)
        }

        bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        if (hasDrawableTint) {
            bitmapNormalColor = PorterDuffColorFilter(drawableTint, PorterDuff.Mode.SRC_IN)
            bitmapPaint.colorFilter = bitmapNormalColor
        }

        if (hasDrawableTintOnSelection) {
            bitmapClipColor = PorterDuffColorFilter(drawableTintOnSelection, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun getBitmapFromDrawableResource(): Bitmap {
        var icon = AppCompatResources.getDrawable(context, drawable)

        if (icon is BitmapDrawable) {
            return icon.bitmap
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            icon = DrawableCompat.wrap(icon!!).mutate()
        }

        val bitmap = Bitmap.createBitmap(icon!!.intrinsicWidth,
                icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        icon.setBounds(0, 0, canvas.width, canvas.height)
        icon.draw(canvas)

        return bitmap
    }

    private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height

        val scaleWidth = if (hasDrawableWidth) newWidth.toFloat() / width else 1.0f
        val scaleHeight = if (hasDrawableHeight) newHeight.toFloat() / height else 1.0f

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }

    private fun getAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SegmentedButton)

        textColorOnSelection = typedArray.getColor(R.styleable.SegmentedButton_sb_textColor_onSelection, Color.WHITE)
        hasTextColorOnSelection = typedArray.hasValue(R.styleable.SegmentedButton_sb_textColor_onSelection)

        drawableTintOnSelection = typedArray.getColor(R.styleable.SegmentedButton_sb_drawableTint_onSelection, Color.WHITE)
        hasDrawableTintOnSelection = typedArray.hasValue(R.styleable.SegmentedButton_sb_drawableTint_onSelection)

        text = typedArray.getString(R.styleable.SegmentedButton_sb_text)
        isTextAllCaps = typedArray.getBoolean(R.styleable.SegmentedButton_sb_textAllCaps, false)
        textSize = typedArray.getDimension(R.styleable.SegmentedButton_sb_textSize, spToPx(context, 12))
        textColor = typedArray.getColor(R.styleable.SegmentedButton_textColor, Color.GRAY)
        textTypefaceResId = typedArray.getResourceId(R.styleable.SegmentedButton_sb_textTypefaceResId, 0)

        try {
            buttonWeight = typedArray.getFloat(R.styleable.SegmentedButton_android_layout_weight, 0f)
            buttonWidth = typedArray.getDimensionPixelSize(R.styleable.SegmentedButton_android_layout_width, 0)
        } catch (ex: Exception) {
            buttonWeight = 1f
        }

        drawable = typedArray.getResourceId(R.styleable.SegmentedButton_drawable, 0)
        drawableTint = typedArray.getColor(R.styleable.SegmentedButton_drawableTint, -1)
        drawableWidth = typedArray.getDimensionPixelSize(R.styleable.SegmentedButton_sb_drawableWidth, -1)
        drawableHeight = typedArray.getDimensionPixelSize(R.styleable.SegmentedButton_sb_drawableHeight, -1)
        drawablePadding = typedArray.getDimensionPixelSize(R.styleable.SegmentedButton_sb_drawablePadding, 0)

        hasDrawable = typedArray.hasValue(R.styleable.SegmentedButton_drawable)
        hasDrawableTint = typedArray.hasValue(R.styleable.SegmentedButton_drawableTint)
        hasDrawableWidth = typedArray.hasValue(R.styleable.SegmentedButton_sb_drawableWidth)
        hasDrawableHeight = typedArray.hasValue(R.styleable.SegmentedButton_sb_drawableHeight)

        drawableGravity = DrawableGravity.getById(typedArray.getInteger(R.styleable.SegmentedButton_sb_drawableGravity, 0))
        typedArray.recycle()
    }

    private fun measureTextWidth(width: Int) {
        val bitmapWidth = if (hasDrawable && drawableGravity!!.isHorizontal) bitmap!!.width else 0
        val textWidth = width - (bitmapWidth + paddingLeft + paddingRight)

        if (textWidth < 0) {
            return
        }

        staticLayout = StaticLayout(text, textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
        staticLayoutOverlay = StaticLayout(text, textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthRequirement = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightRequirement = View.MeasureSpec.getSize(heightMeasureSpec)

        var width = 0
        val bitmapWidth = if (hasDrawable) bitmap!!.width else 0
        val textWidth = staticLayout?.width ?: 0

        var height = paddingTop + paddingBottom
        val bitmapHeight = if (hasDrawable) bitmap!!.height else 0
        val textHeight = staticLayout?.height ?: 0

        when (widthMode) {
            View.MeasureSpec.EXACTLY -> if (width < widthRequirement) {
                width = widthRequirement
                measureTextWidth(width)
            }
            View.MeasureSpec.AT_MOST -> {
                width += paddingLeft + paddingRight + textWidth
                if (drawableGravity!!.isHorizontal) {
                    width += bitmapWidth
                }
            }
            View.MeasureSpec.UNSPECIFIED -> width = textWidth + bitmapWidth
        }

        textPaint.getTextBounds(text, 0, text?.length ?: 0, textBounds)

        when (heightMode) {
            View.MeasureSpec.EXACTLY -> {
                if (drawableGravity!!.isHorizontal) {
                    height = heightRequirement
                    val h = Math.max(textHeight, bitmapHeight) + paddingTop + paddingBottom
                    if (heightRequirement < h) {
                        height = h
                    }
                } else {
                    val h = textHeight + bitmapHeight + paddingTop + paddingBottom
                    height = if (heightRequirement < h) {
                        h
                    } else {
                        heightRequirement + paddingTop - paddingBottom
                    }
                }
            }
            View.MeasureSpec.AT_MOST -> {
                val vHeight = if (drawableGravity!!.isHorizontal) {
                    Math.max(textHeight, bitmapHeight)
                } else {
                    textHeight + bitmapHeight + drawablePadding
                }

                height = vHeight + paddingTop * 2 + paddingBottom * 2
            }
            View.MeasureSpec.UNSPECIFIED -> {
            }
        }

        calculate(width, height)
        setMeasuredDimension(width, height)
    }

    private fun calculate(width: Int, height: Int) {
        val textHeight = staticLayout?.height?.toFloat() ?: 0f
        val textWidth = staticLayout?.width?.toFloat() ?: 0f
        val textBoundsWidth = textBounds.width().toFloat()

        var bitmapHeight = 0f
        var bitmapWidth = 0f

        if (hasDrawable) {
            bitmapHeight = bitmap!!.height.toFloat()
            bitmapWidth = bitmap!!.width.toFloat()
        }

        if (drawableGravity!!.isHorizontal) {
            when {
                height > Math.max(textHeight, bitmapHeight) -> {
                    textY = height / 2f - textHeight / 2f + paddingTop - paddingBottom
                    bitmapY = height / 2f - bitmapHeight / 2f + paddingTop - paddingBottom
                }
                textHeight > bitmapHeight -> {
                    textY = paddingTop.toFloat()
                    bitmapY = textY + textHeight / 2f - bitmapHeight / 2f
                }
                else -> {
                    bitmapY = paddingTop.toFloat()
                    textY = bitmapY + bitmapHeight / 2f - textHeight / 2f
                }
            }

            textX = paddingLeft.toFloat()
            bitmapX = textWidth

            var remainingSpace = width - (textBoundsWidth + bitmapWidth)
            if (remainingSpace > 0) {
                remainingSpace /= 2f
            }

            if (drawableGravity == DrawableGravity.RIGHT) {
                textX = remainingSpace + paddingLeft - paddingRight.toFloat() - drawablePadding / 2f
                bitmapX = textX + textBoundsWidth + drawablePadding
            } else if (drawableGravity === DrawableGravity.LEFT) {
                bitmapX = remainingSpace + paddingLeft - paddingRight.toFloat() - drawablePadding / 2f
                textX = bitmapX + bitmapWidth + drawablePadding
            }
        } else {

            if (drawableGravity == DrawableGravity.TOP) {
                bitmapY = paddingTop.toFloat() - paddingBottom.toFloat() - drawablePadding / 2f
                val vHeight = (height - (textHeight + bitmapHeight)) / 2f
                if (vHeight > 0) {
                    bitmapY += vHeight
                }
                textY = bitmapY + bitmapHeight + drawablePadding
            } else if (drawableGravity == DrawableGravity.BOTTOM) {
                textY = paddingTop.toFloat() - paddingBottom.toFloat() - drawablePadding / 2f
                val vHeight = height - (textHeight + bitmapHeight)
                if (vHeight > 0) {
                    textY += vHeight / 2f
                }
                bitmapY = textY + textHeight + drawablePadding
            }

            when {
                width > Math.max(textBoundsWidth, bitmapWidth) -> {
                    textX = width / 2f - textBoundsWidth / 2f + paddingLeft - paddingRight
                    bitmapX = width / 2f - bitmapWidth / 2f + paddingLeft - paddingRight
                }
                textBoundsWidth > bitmapWidth -> {
                    textX = paddingLeft.toFloat()
                    bitmapX = textX + textBoundsWidth / 2f - bitmapWidth / 2f
                }
                else -> {
                    bitmapX = paddingLeft.toFloat()
                    textX = bitmapX + bitmapWidth / 2f - textBoundsWidth / 2f
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()

        if (clipLeftToRight) {
            canvas.translate(-width * (clipAmount - 1), 0f)
        } else {
            canvas.translate(width * (clipAmount - 1), 0f)
        }

        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.run {
            drawRoundRect(rectF, radius.toFloat(), radius.toFloat(), paint)
            restore()
            save()
            translate(textX, textY)
        }

        if (hasTextColorOnSelection) {
            textPaint.color = textColor
        }
        staticLayout?.draw(canvas)

        canvas.restore()
        canvas.save()

        if (hasDrawable) {
            bitmapPaint.colorFilter = bitmapNormalColor
            canvas.drawBitmap(bitmap!!, bitmapX, bitmapY, bitmapPaint)
        }

        if (clipLeftToRight) {
            canvas.clipRect(width * (1 - clipAmount), 0f, width.toFloat(), height.toFloat())
        } else {
            canvas.clipRect(0f, 0f, width * clipAmount, height.toFloat())
        }

        canvas.save()

        canvas.translate(textX, textY)
        if (hasTextColorOnSelection) {
            textPaint.color = textColorOnSelection
        }
        staticLayoutOverlay?.draw(canvas)
        canvas.restore()

        if (hasDrawable) {
            if (hasDrawableTintOnSelection) {
                bitmapPaint.colorFilter = bitmapClipColor
            }
            canvas.drawBitmap(bitmap!!, bitmapX, bitmapY, bitmapPaint)
        }

        canvas.restore()
    }

    fun clipToLeft(clip: Float) {
        clipLeftToRight = false
        clipAmount = 1.0f - clip
        invalidate()
    }

    fun clipToRight(clip: Float) {
        clipLeftToRight = true
        clipAmount = 1.0f - clip
        invalidate()
    }

    fun setSelectorColor(color: Int) {
        paint.color = color
    }

    private enum class DrawableGravity(private val intValue: Int) {
        LEFT(0),
        TOP(1),
        RIGHT(2),
        BOTTOM(3);

        val isHorizontal: Boolean
            get() = intValue == 0 || intValue == 2

        companion object {

            fun getById(id: Int): DrawableGravity? {
                for (e in values()) {
                    if (e.intValue == id) return e
                }
                return null
            }
        }
    }

    fun spToPx(context: Context, size: Int): Float {
        return size * context.resources.displayMetrics.scaledDensity
    }
}
