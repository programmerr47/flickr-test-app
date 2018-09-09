package com.github.programmerr47.flickrawesomeclient.widgets

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.github.programmerr47.flickrawesomeclient.R
import com.github.programmerr47.flickrawesomeclient.util.use
import com.github.programmerr47.flickrawesomeclient.util.visible

typealias PositionChangeListener = (top: Int, left: Int, factor: Float) -> Unit
typealias DismissListener = () -> Unit

/**
 * Copied from https://github.com/chuross/flinglayout
 * @author chuross
 */
class FlingLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var isDragEnabled = true
    var isDismissEnabled = true
    var positionChangeListener: PositionChangeListener? = null
    var dismissListener: DismissListener? = null

    private val threshold = 1500
    private var dragHelper: ViewDragHelper? = null
    private var defaultChildX: Int? = null
    private var defaultChildY: Int? = null

    init {
        dragHelper = ViewDragHelper.create(this, 1f, object: ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int) = isDragEnabled && child.visible

            override fun getViewVerticalDragRange(child: View) = 1

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int) = top

            override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
                super.onViewPositionChanged(changedView, left, top, dx, dy)
                val defChildY = defaultChildY ?: return
                val rangeY = (measuredHeight / 2)
                val distance = Math.abs(top - defChildY)

                val factor = Math.min(1f, distance.toFloat() / rangeY)
                positionChangeListener?.invoke(top, left, factor)
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                super.onViewReleased(releasedChild, xvel, yvel)
                releasedChild.let { this@FlingLayout.onViewReleased(it, yvel) }
            }

        })

        attrs?.let {
            context.obtainStyledAttributes(it, R.styleable.FlingLayout, defStyleAttr, 0).use {
                isDragEnabled = getBoolean(R.styleable.FlingLayout_fl_canDrag, true)
                isDismissEnabled = getBoolean(R.styleable.FlingLayout_fl_canDismiss, true)
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return dragHelper?.shouldInterceptTouchEvent(ev) ?: false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper?.processTouchEvent(event)
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (childCount == 0) return
        if (childCount > 1) throw IllegalStateException("Child must be single: current child count = $childCount")

        getChildAt(0)?.run {
            defaultChildX = this.left
            defaultChildY = this.top
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (dragHelper?.continueSettling(true) == true) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun onViewReleased(target: View, yvel: Float) {
        val x = defaultChildX ?: return
        val y = defaultChildY ?: return

        if (Math.abs(yvel) < threshold) {
            dragHelper?.settleCapturedViewAt(x, y)
            invalidate()
            return
        }
        if (!isDismissEnabled) return

        val targetY = if (yvel > 0) measuredHeight else -target.measuredHeight
        dragHelper?.smoothSlideViewTo(target, x, targetY)
        invalidate()

        dismissListener?.invoke()
    }
}