package com.github.programmerr47.flickrawesomeclient.pages

import android.content.Context
import com.github.programmerr47.flickrawesomeclient.R
import com.github.programmerr47.flickrawesomeclient.util.isNetworkAvailable
import com.github.programmerr47.flickrawesomeclient.util.showToast
import java.net.UnknownHostException

fun handleCommonError(context: Context, throwable: Throwable) = context.run {
    when {
        !isNetworkAvailable -> showToast(R.string.error_no_connection)
        throwable hasCause { it is UnknownHostException } -> showToast(R.string.error_unknown_host)
        else -> showToast(throwable.localizedMessage)
    }
}

private inline infix fun Throwable.hasCause(predicate: (Throwable) -> Boolean): Boolean {
    var thr: Throwable? = this
    while (thr != null) {
        if (predicate(thr)) return true
        thr = thr.cause
    }

    return false
}