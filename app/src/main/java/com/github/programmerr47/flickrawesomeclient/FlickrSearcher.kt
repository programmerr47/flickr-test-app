package com.github.programmerr47.flickrawesomeclient

import android.support.v4.util.LruCache
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Scheduler

class FlickrSearcher(
        private val flickrApi: FlickrApi,
        private val ioScheduler: Scheduler
) {
    private val cache: LruCache<String, PhotoList> by lazy { SearchLruCache(30000) }

    fun searchPhotos(text: String) = createDeferred {
        cache[text]?.let {
            Observable.just(it)
        } ?: requestFirst(text)
    }

    fun searchMorePhotos(text: String) = createDeferred {
        cache[text]?.let { cached ->
            if (cached.canTakeMore()) {
                requestMore(text, cached.page + 1)
                        .map { cached + it }
                        .cacheResult(text)
            } else {
                Observable.just(cached)
            }
        } ?: requestFirst(text)
    }

    private fun <T> createDeferred(supplier: () -> ObservableSource<T>) =
            Observable.defer(supplier).subscribeOn(ioScheduler)

    private fun requestFirst(text: String) = flickrApi.searchPhotos(text)
            .map { it.body }
            .cacheResult(text)

    private fun requestMore(text: String, page: Int) = flickrApi.searchPhotos(text, page).map { it.body }

    private fun Observable<PhotoList>.cacheResult(key: String) = doOnNext { cache.put(key, it) }

    private class SearchLruCache(
            overallEntriesCount: Int
    ) : LruCache<String, PhotoList>(overallEntriesCount) {

        override fun sizeOf(key: String, value: PhotoList) = value.list.size
    }
}

