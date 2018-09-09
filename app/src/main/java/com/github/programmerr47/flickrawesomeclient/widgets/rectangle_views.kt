package com.github.programmerr47.flickrawesomeclient.widgets

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View
import com.github.programmerr47.flickrawesomeclient.R
import com.github.programmerr47.flickrawesomeclient.util.use

const val RECTANGLE_VIEW_DEFAULT_FACTOR = 0.6f

class RectangleImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    var factor = RECTANGLE_VIEW_DEFAULT_FACTOR
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    init {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.RectangleImageView, 0, 0).use {
                factor = getFloat(R.styleable.RectangleImageView_factor, RECTANGLE_VIEW_DEFAULT_FACTOR)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(width, (width * factor).toInt())
    }
}