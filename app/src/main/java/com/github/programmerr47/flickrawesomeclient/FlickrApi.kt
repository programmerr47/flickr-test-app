package com.github.programmerr47.flickrawesomeclient

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {
    @GET("?method=flickr.photos.search&per_page=20")
    fun searchPhotos(@Query("text") searchText: String): Observable<PhotoListResponse>
}