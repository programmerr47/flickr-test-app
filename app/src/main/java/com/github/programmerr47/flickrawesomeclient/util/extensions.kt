package com.github.programmerr47.flickrawesomeclient.util

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.annotation.SuppressLint
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.widget.EditText
import android.widget.TextView

fun FragmentManager.commitTransaction(block: FragmentTransaction.() -> Unit) =
        beginTransaction().apply(block).commit()

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
    action == ACTION_DOWN && keyCode == KEYCODE_ENTER
} ?: false