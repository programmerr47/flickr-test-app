package com.github.programmerr47.flickrawesomeclient.util

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.content.res.AppCompatResources
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import kotlin.reflect.KClass

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

fun DiffUtil.Callback.calculateDiff() = DiffUtil.calculateDiff(this)

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.dispatchUpdatesFrom(diffResult: DiffUtil.DiffResult) =
        diffResult.dispatchUpdatesTo(this)

inline fun <T: TypedArray> T.use(block: T.() -> Unit) {
    try {
        block()
    } finally {
        recycle()
    }
}

fun View.showKeyboard() {
    isFocusableInTouchMode = true
    requestFocus()
    val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.inflater() = LayoutInflater.from(context)

var View.visible: Boolean
    get() = visibility == VISIBLE
    set(value) { visibility = if (value) VISIBLE else GONE  }

fun Fragment.hideKeyboard() = activity?.hideKeyboard()

fun Activity.hideKeyboard() {
    currentFocus?.run {
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
    }
}

fun Activity.finishNoAnim() {
    finish()
    overridePendingTransition(0, 0)
}

fun <T : View> Activity.bindable(@IdRes id: Int) = lazy { findViewById<T>(id) }

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int) = AppCompatResources.getDrawable(this, drawableRes)

fun Context.startActivity(clazz: KClass<*>, intentFun: Intent.() -> Unit) =
        startActivity(Intent(this, clazz.java).apply(intentFun))

val Context.inputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
