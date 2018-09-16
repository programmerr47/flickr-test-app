package com.github.programmerr47.flickrawesomeclient.services

import android.arch.paging.PagedList
import android.support.v4.util.LruCache
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.net.FlickrApi
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleSource
import java.util.concurrent.Executor

class FlickrSearcher(
        private val flickrApi: FlickrApi,
        private val searchConfig: PagedList.Config,
        private val backgroundExecutor: Executor,
        private val backgroundScheduler: Scheduler,
        private val foregroundExecutor: Executor
) {
    private val cache: LruCache<String, PagedList<Photo>> by lazy { LruCache<String, PagedList<Photo>>(10) }


    fun searchPhotos(text: String) = createDefered {
        if (text !in cache) {
            val dataSource = PhotoDataSource(text, flickrApi)
            cache[text] = PagedList.Builder<Int, Photo>(dataSource, searchConfig)
                    .setFetchExecutor(backgroundExecutor)
                    .setNotifyExecutor(foregroundExecutor)
                    .build()
        }

        Single.just(cache[text])
    }

    fun searchForce(text: String) = createDefered {
        cache.remove(text)
        searchPhotos(text)
    }

    private fun createDefered(supplier: () -> SingleSource<PagedList<Photo>>) =
            Single.defer(supplier).subscribeOn(backgroundScheduler)

    private operator fun <K, V> LruCache<K, V>.contains(key: K) = get(key) != null
    private operator fun <K, V> LruCache<K, V>.set(key: K, value: V) = put(key, value)
}

