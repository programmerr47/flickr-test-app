package com.github.programmerr47.flickrawesomeclient

import android.support.v4.util.LruCache
import io.reactivex.Observable
import io.reactivex.Scheduler

class FlickrSearcher(
        private val flickrApi: FlickrApi,
        private val ioScheduler: Scheduler
) {
    private val cache: LruCache<String, PhotoList> by lazy { SearchLruCache(3000) }

    fun searchPhotos(text: String) = Observable.defer {
        cache[text]?.let {
            Observable.just(it)
        } ?:
        flickrApi.searchPhotos(text)
                .map { it -> it.body }
                .doOnNext { cache.put(text, it) }
    }
            .subscribeOn(ioScheduler)

    private class SearchLruCache(
            overallEntriesCount: Int
    ) : LruCache<String, PhotoList>(overallEntriesCount) {

        override fun sizeOf(key: String, value: PhotoList) = value.list.size
    }
}

