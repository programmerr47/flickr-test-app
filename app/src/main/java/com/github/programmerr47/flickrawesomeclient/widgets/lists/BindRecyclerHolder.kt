package com.github.programmerr47.flickrawesomeclient.widgets.lists

import android.support.annotation.IdRes
import android.support.v7.widget.RecyclerView
import android.view.View

open class BindRecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    protected fun <T : View> bind(@IdRes id: Int) = itemView.findViewById<T>(id)
}