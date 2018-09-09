package com.github.programmerr47.flickrawesomeclient.util

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

fun View.showKeyboard() {
    isFocusableInTouchMode = true
    requestFocus()
    val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.inflater() = LayoutInflater.from(context)

var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(value) { visibility = if (value) View.VISIBLE else View.GONE
    }


@SuppressLint("NewApi")
fun View.setStateListElevationAnimator(elevation: Float) {
    if (isLollipop()) {
        val stateListAnimator = StateListAnimator()
        stateListAnimator.addState(IntArray(0), ObjectAnimator.ofFloat(this, "elevation", elevation).setDuration(0))
        this.stateListAnimator = stateListAnimator
    }
}

fun EditText.setOnImeOptionsClickListener(listener: (TextView) -> Unit) = setOnEditorActionListener { v, actionId, event ->
    if (actionId == this@setOnImeOptionsClickListener.imeOptions || event.isEnterPressed()) {
        listener(v)
        true
    } else false
}

private fun KeyEvent?.isEnterPressed() = this?.let {
    action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER
} ?: false