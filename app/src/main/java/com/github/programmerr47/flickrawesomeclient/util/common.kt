package com.github.programmerr47.flickrawesomeclient.util

import android.content.res.TypedArray

inline fun <T: TypedArray> T.use(block: T.() -> Unit) {
    try {
        block()
    } finally {
        recycle()
    }
}