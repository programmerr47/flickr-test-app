package com.github.programmerr47.flickrawesomeclient.net

import com.github.programmerr47.flickrawesomeclient.models.PhotoListResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {
    @GET("?method=flickr.photos.search")
    fun searchPhotos(@Query("text") searchText: String,
                     @Query("page") page: Int = 1,
                     @Query("per_page") perPage: Int = 40): Single<PhotoListResponse>
}