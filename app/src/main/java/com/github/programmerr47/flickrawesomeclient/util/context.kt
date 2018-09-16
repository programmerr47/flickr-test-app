package com.github.programmerr47.flickrawesomeclient.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.content.res.AppCompatResources
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlin.reflect.KClass

val Context.isNetworkAvailable: Boolean get() {
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun Context.showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Context.showToast(@StringRes msgId: Int) = Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show()

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int) = AppCompatResources.getDrawable(this, drawableRes)

fun Context.startActivity(clazz: KClass<*>, intentFun: Intent.() -> Unit) =
        startActivity(Intent(this, clazz.java).apply(intentFun))

val Context.inputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

val Context.connectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager