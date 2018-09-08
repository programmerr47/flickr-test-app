package com.github.programmerr47.flickrawesomeclient

import android.app.Application
import android.util.Log
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import io.reactivex.schedulers.Schedulers.io
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class FlickrApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(io()))
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder()
                        .registerTypeAdapter(Photo::class.java, PhotoDeserializer())
                        .create()))
                .baseUrl("https://api.flickr.com/services/rest/")
                .client(OkHttpClient.Builder()
                        .addInterceptor {
                            var request = it.request()
                            request = request.newBuilder()
                                    .url(request.url().newBuilder()
                                            .addQueryParameter("api_key", "41a3fd8458421cf8a4ae0f836014ef35")
                                            .addQueryParameter("format", "json")
                                            .addQueryParameter("nojsoncallback", "1")
                                            .build())
                                    .build()
                            it.proceed(request)
                        }
                        .build())
                .build()

        api = retrofit.create(FlickrApi::class.java)
        flickrSearcher = FlickrSearcher(api, io())
    }

    companion object {
        lateinit var api: FlickrApi
        lateinit var flickrSearcher: FlickrSearcher
    }
}