package com.github.programmerr47.flickrawesomeclient.pages.search

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.reactivex.disposables.Disposable

class LoadMoreDetector : RecyclerView.OnScrollListener() {

    private var searchDisposable: Disposable? = null
    private var loadMoreFun: (() -> Disposable)? = null

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy > 0) {
            val visibleCount = recyclerView.layoutManager.childCount
            val totalCount = recyclerView.layoutManager.itemCount
            val firstVisiblePos = (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0

            if (visibleCount + firstVisiblePos >= totalCount && isNoSearching()) {
                searchDisposable = loadMoreFun?.invoke()
            }
        }
    }

    fun start(loadMoreFun: () -> Disposable) {
        this.loadMoreFun = loadMoreFun
    }

    fun stop() {
        loadMoreFun = null
        clearSearchDisposable()
    }

    fun search(searchFun: () -> Disposable) {
        if (!isNoSearching()) {
            clearSearchDisposable()
        }

        searchDisposable = searchFun()
    }

    private fun clearSearchDisposable() {
        searchDisposable?.dispose()
        searchDisposable = null
    }

    private fun isNoSearching() = searchDisposable?.isDisposed != false
}