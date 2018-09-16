package com.github.programmerr47.flickrawesomeclient.services

import android.arch.paging.PageKeyedDataSource
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.net.FlickrApi

class PhotoDataSource(
        private val text: String,
        private val flickrApi: FlickrApi
) : PageKeyedDataSource<Int, Photo>() {

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Photo>) {
        flickrApi.searchPhotos(text, 1, params.requestedLoadSize).blockingGet().body.let {
            callback.onResult(it.list, 0, it.total, null, 2)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {
        flickrApi.searchPhotos(text, params.key, params.requestedLoadSize).blockingGet().body.let {
            callback.onResult(it.list, (it.page - 1).takeIf { it > 0 })
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Photo>) {
        flickrApi.searchPhotos(text, params.key, params.requestedLoadSize).blockingGet().body.let {
            val newKey = if (it.canTakeMore()) it.page + 1 else null
            callback.onResult(it.list, newKey)
        }
    }
}