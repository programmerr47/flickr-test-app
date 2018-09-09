package com.github.programmerr47.flickrawesomeclient

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {
    @GET("?method=flickr.photos.search&per_page=40")
    fun searchPhotos(@Query("text") searchText: String,
                     @Query("page") page: Int = 1): Single<PhotoListResponse>
}