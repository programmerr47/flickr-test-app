package com.github.programmerr47.flickrawesomeclient.util

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView

fun DiffUtil.Callback.calculateDiff() = DiffUtil.calculateDiff(this)

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.dispatchUpdatesFrom(diffResult: DiffUtil.DiffResult) =
        diffResult.dispatchUpdatesTo(this)