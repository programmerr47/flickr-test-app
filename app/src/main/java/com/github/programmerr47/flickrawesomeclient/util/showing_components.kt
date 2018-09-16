package com.github.programmerr47.flickrawesomeclient.util

import android.app.Activity
import android.content.Context
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.View

fun FragmentManager.commitTransaction(block: FragmentTransaction.() -> Unit) =
        beginTransaction().apply(block).commit()

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