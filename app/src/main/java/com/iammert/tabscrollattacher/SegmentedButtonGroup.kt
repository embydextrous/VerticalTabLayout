package com.iammert.tabscrollattacher

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import games.kapow.utils.ui.custom_views.FixedSpeedScroller
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.truncate

class SegmentedButtonGroup(context: Context,
                           attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    private val rectF = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mainGroup: LinearLayout
    private val buttons = mutableListOf<SegmentedButton>()

    var onButtonTap: ((Int) -> Unit)? = null
    private var viewPager: ViewPager? = null
    var selectorColor: Int = 0
        set(color) {
            field = color
            for (button in buttons) {
                button.setSelectorColor(color)
            }
        }
    private var animateSelectorDuration: Int = 200
    private var currentPosition: Int = 0
    private var currentPositionOffset: Float = 0f
    var bgColor: Int = 0
    private var radius: Int = 0

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class ButtonOutlineProvider : ViewOutlineProvider() {

        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.measuredWidth, view.measuredHeight, radius.toFloat())
        }
    }

    init {
        if (attrs != null) {
            getAttributes(attrs)
        }

        setWillNotDraw(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider = ButtonOutlineProvider()
        }

        isClickable = true

        val container = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
        }
        addView(container)

        mainGroup = LinearLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
        }
        container.addView(mainGroup)

        setContainerAttrs()

        viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)

                var maxWidth = 0

                buttons.asSequence()
                        .filter { button -> button.measuredWidth > maxWidth }
                        .forEach { button -> maxWidth = button.measuredWidth }

                if (maxWidth * buttons.size < minimumWidth - paddingLeft - paddingRight) {
                    maxWidth = (minimumWidth - paddingLeft - paddingRight)/buttons.size
                }

                for (button: SegmentedButton in buttons) {
                   button.layoutParams.width = maxWidth
                   button.requestLayout()
                }

                requestLayout()
            }
        })
    }

    fun setViewPager(viewPager: ViewPager) {
        this.viewPager = viewPager

        moveTo(viewPager.currentItem.toFloat())

        try {
            val scrollerField = ViewPager::class.java.getDeclaredField("mScroller")
            scrollerField.isAccessible = true

            val mScroller = FixedSpeedScroller(context).apply {
                scrollDuration = animateSelectorDuration
            }
            scrollerField.set(viewPager, mScroller)

        } catch (e: NoSuchFieldException) {
            //logger.error(e.message)
        } catch (e: IllegalArgumentException) {
            //logger.error(e.message)
        } catch (e: IllegalAccessException) {
            //logger.error(e.message)
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                moveTo(position + positionOffset)
            }

            override fun onPageSelected(position: Int) {
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        paint.run {
            style = Paint.Style.FILL
            color = bgColor
        }
        canvas.drawRoundRect(rectF, radius.toFloat(), radius.toFloat(), paint)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is SegmentedButton) {
            val position = buttons.size

            child.radius = radius
            child.setSelectorColor(selectorColor)

            mainGroup.addView(child, params)
            buttons.add(child)

            if (currentPosition == position) {
                child.clipToRight(0f)
            }

            child.setOnClickListener {
                if (currentPosition != position) {
                    viewPager?.run { currentItem = position } ?: animateTo(position, animateSelectorDuration)
                    onButtonTap?.invoke(position)
                }
            }
        } else {
            super.addView(child, index, params)
        }
    }

    private fun setContainerAttrs() {
        if (isInEditMode) {
            mainGroup.setBackgroundColor(bgColor)
        }
    }

    private fun getAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SegmentedButtonGroup)

        selectorColor = typedArray.getColor(R.styleable.SegmentedButtonGroup_selectorColor, Color.GRAY)
        animateSelectorDuration = typedArray.getInt(
                R.styleable.SegmentedButtonGroup_sbg_animateSelectorDuration, 500)
        radius = typedArray.getDimensionPixelSize(R.styleable.SegmentedButtonGroup_sbg_radius, 0)
        currentPosition = typedArray.getInt(R.styleable.SegmentedButtonGroup_sbg_position, 0)
        bgColor = typedArray.getColor(R.styleable.SegmentedButtonGroup_bgColor, Color.TRANSPARENT)

        typedArray.recycle()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) 1.0f else 0.5f
    }

    private fun animateTo(position: Int, duration: Int) {
        if (currentPosition == position) {
            return
        }

        ValueAnimator.ofFloat(currentPosition.toFloat(), position.toFloat()).apply {
            addUpdateListener { animation ->
                moveTo(animation.animatedValue as Float)
                invalidate()
            }
            interpolator = FastOutSlowInInterpolator()
            setDuration(duration.toLong())
            start()
        }
    }

    private fun moveTo(realPosition: Float) {
        val lastRealPosition = currentPosition + currentPositionOffset

        if (realPosition == lastRealPosition) {
            return
        }

        val position = truncate(realPosition).toInt()
        val positionOffset = realPosition - position
        val fromPosition1 = floor(lastRealPosition).toInt()
        val fromPosition2 = ceil(lastRealPosition).toInt()
        val toPosition1 = floor(realPosition).toInt()
        val toPosition2 = ceil(realPosition).toInt()

        // at max 4 buttons are involved when a move is made
        val buttons = mutableSetOf(fromPosition1, fromPosition2, toPosition1, toPosition2)
        buttons.run {
            remove(toPosition1)
            remove(toPosition2)
        }
        for (pos in buttons) {
            clipButtonSelectionFromLeft(pos, 1f)
        }
        clipButtonSelectionFromRight(toPosition1, positionOffset)

        if (toPosition1 != toPosition2) {
            clipButtonSelectionFromLeft(toPosition2, 1f - positionOffset)
        }

        currentPosition = truncate(realPosition).toInt()
        currentPositionOffset = realPosition - currentPosition
    }

    private fun clipButtonSelectionFromRight(position: Int, clip: Float) {
        if (position in 0..(buttons.size - 1)) {
            buttons[position].clipToRight(clip)
        }
    }

    private fun clipButtonSelectionFromLeft(position: Int, clip: Float) {
        if (position in 0..(buttons.size - 1)) {
            buttons[position].clipToLeft(clip)
        }
    }
}
