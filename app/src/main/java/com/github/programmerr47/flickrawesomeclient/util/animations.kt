package com.github.programmerr47.flickrawesomeclient.util

import android.view.View

fun View.fadeSlideUp() = slide(0f, -height.toFloat())

fun View.revealSlide()  = slide(1f, 0f)

private fun View.slide(alpha: Float, translationY: Float) {
    animate()
            .alpha(alpha)
            .translationY(translationY)
}
