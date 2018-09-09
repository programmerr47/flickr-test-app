package com.github.programmerr47.flickrawesomeclient

import android.support.v4.util.LruCache
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleSource

class FlickrSearcher(
        private val flickrApi: FlickrApi,
        private val ioScheduler: Scheduler
) {
    private val cache: LruCache<String, PhotoList> by lazy { SearchLruCache(30000) }

    fun searchPhotos(text: String) = createDeferred {
        cache[text]?.let {
            Single.just(it)
        } ?: requestFirst(text)
    }

    fun searchForce(text: String) = requestFirst(text)

    fun searchMorePhotos(text: String) = createDeferred {
        cache[text]?.let { cached ->
            if (cached.canTakeMore()) {
                requestMore(text, cached.page + 1)
                        .map { cached + it }
                        .cacheResult(text)
            } else {
                Single.just(cached)
            }
        } ?: requestFirst(text)
    }

    private fun <T> createDeferred(supplier: () -> SingleSource<T>) =
            Single.defer(supplier).subscribeOn(ioScheduler)

    private fun requestFirst(text: String) = flickrApi.searchPhotos(text)
            .map { it.body }
            .cacheResult(text)

    private fun requestMore(text: String, page: Int) = flickrApi.searchPhotos(text, page).map { it.body }

    private fun Single<PhotoList>.cacheResult(key: String) = doOnSuccess { cache.put(key, it) }

    private class SearchLruCache(
            overallEntriesCount: Int
    ) : LruCache<String, PhotoList>(overallEntriesCount) {

        override fun sizeOf(key: String, value: PhotoList) = value.list.size
    }
}

