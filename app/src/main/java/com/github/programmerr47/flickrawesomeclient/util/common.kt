package com.github.programmerr47.flickrawesomeclient.util

import android.content.res.TypedArray
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.github.programmerr47.flickrawesomeclient.FlickrApplication.Companion.appContext

inline fun <T: TypedArray> T.use(block: T.() -> Unit) {
    try {
        block()
    } finally {
        recycle()
    }
}

fun showToast(msg: String) = Toast.makeText(appContext, msg, LENGTH_SHORT).show()