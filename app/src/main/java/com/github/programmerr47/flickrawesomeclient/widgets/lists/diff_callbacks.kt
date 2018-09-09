package com.github.programmerr47.flickrawesomeclient.widgets.lists

import android.support.v7.util.DiffUtil

typealias DiffCallbackFactory<T> = (List<T>, List<T>) -> DiffUtil.Callback

class SimpleDiffCallback<T>(
        private val old: List<T>,
        private val new: List<T>
) : DiffUtil.Callback() {
    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size
    override fun areItemsTheSame(oldPos: Int, newPos: Int) = old[oldPos] == new[newPos]
    override fun areContentsTheSame(oldPos: Int, newPos: Int) = old[oldPos] == new[newPos]
}