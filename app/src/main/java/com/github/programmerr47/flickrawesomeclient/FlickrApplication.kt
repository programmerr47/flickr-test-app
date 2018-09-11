package com.github.programmerr47.flickrawesomeclient

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.github.programmerr47.flickrawesomeclient.db.AppDatabase
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.net.FlickrApi
import com.github.programmerr47.flickrawesomeclient.net.PhotoDeserializer
import com.github.programmerr47.flickrawesomeclient.services.FlickrSearcher
import com.github.programmerr47.flickrawesomeclient.services.RecentSearchesService
import com.github.programmerr47.flickrawesomeclient.util.sugar.addQueryParams
import com.github.programmerr47.flickrawesomeclient.util.sugar.adjustRequestUrl
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
        bind<Retrofit>() with singleton { createRetrofit() }

        bind<Scheduler>("ioScheduler") with singleton { io() }

        bind<FlickrApi>() with singleton { instance<Retrofit>().create(FlickrApi::class.java) }
        bind<FlickrSearcher>() with singleton { FlickrSearcher(instance(), instance("ioScheduler")) }

        bind<AppDatabase>() with singleton { createDb(instance()) }
        bind<RecentSearchesService>() with singleton {
            RecentSearchesService(instance<AppDatabase>().recentSearchDao())
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
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
        val newRequest = it.adjustRequestUrl {
            addQueryParams(
                    "api_key" to "41a3fd8458421cf8a4ae0f836014ef35",
                    "format" to "json",
                    "nojsoncallback" to "1"
            )
        }
        it.proceed(newRequest)
    }

    private fun createDb(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "app_db").build()

    companion object {
        lateinit var appContext: Context
    }
}