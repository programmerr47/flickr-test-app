package com.github.programmerr47.flickrawesomeclient.util.sugar

import android.support.v4.view.ViewPager

class SimplePageChangeListener : ViewPager.OnPageChangeListener {
    private var scrolled: (Int, Float, Int) -> Unit = { _, _, _ -> }
    private var selected: (Int) -> Unit = {}
    private var scrollState: (Int) -> Unit = {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) =
        scrolled(position, positionOffset, positionOffsetPixels)

    override fun onPageSelected(position: Int) = selected(position)

    override fun onPageScrollStateChanged(state: Int) = scrollState(state)

    fun scrolled(scrolled: (Int, Float, Int) -> Unit) = apply { this.scrolled = scrolled }
    fun selected(selected: (Int) -> Unit) = apply { this.selected = selected }
    fun scrollState(scrollState: (Int) -> Unit) = apply { this.scrollState = scrollState }
}

fun onPageChangeListener() = SimplePageChangeListener()