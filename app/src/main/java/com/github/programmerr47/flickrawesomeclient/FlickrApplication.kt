package com.github.programmerr47.flickrawesomeclient

import android.app.Application
import com.github.salomonbrys.kodein.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers.io
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class FlickrApplication : Application(), KodeinAware {
    override val kodein = Kodein {
        bind<Interceptor>("flickrInterceptor") with singleton { createFlickInterceptor() }
        bind<OkHttpClient>() with singleton { createOkHttpClient() }
        bind<Gson>() with singleton { createGson() }
        bind<Scheduler>("ioScheduler") with singleton { io() }
        bind<Retrofit>() with singleton { createRetrofit() }
        bind<FlickrApi>() with singleton { instance<Retrofit>().create(FlickrApi::class.java) }
        bind<FlickrSearcher>() with singleton { FlickrSearcher(instance(), instance("ioScheduler")) }
    }

    private fun createRetrofit() = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(instance("ioScheduler")))
            .addConverterFactory(GsonConverterFactory.create(instance()))
            .baseUrl("https://api.flickr.com/services/rest/")
            .client(instance())
            .build()

    private fun createGson() = GsonBuilder()
            .registerTypeAdapter(Photo::class.java, PhotoDeserializer())
            .create()

    private fun createOkHttpClient() = OkHttpClient.Builder()
            .addInterceptor(instance("flickrInterceptor"))
            .build()

    private fun createFlickInterceptor() = Interceptor {
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
}