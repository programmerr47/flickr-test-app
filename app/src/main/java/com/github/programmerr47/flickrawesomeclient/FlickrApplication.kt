package com.github.programmerr47.flickrawesomeclient

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.github.programmerr47.flickrawesomeclient.db.AppDatabase
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.net.FlickrApi
import com.github.programmerr47.flickrawesomeclient.net.PhotoDeserializer
import com.github.programmerr47.flickrawesomeclient.services.FlickrSearcher
import com.github.programmerr47.flickrawesomeclient.services.RecentSearchService
import com.github.programmerr47.flickrawesomeclient.services.RecentSearcher
import com.github.programmerr47.flickrawesomeclient.util.isNetworkAvailable
import com.github.programmerr47.flickrawesomeclient.util.sugar.addQueryParams
import com.github.programmerr47.flickrawesomeclient.util.sugar.adjustRequestUrl
import com.github.salomonbrys.kodein.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers.io
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

//todo move to Kodein 5.2.0
class FlickrApplication : Application(), KodeinAware {
    override val kodein = Kodein {
        bind<Context>("appContext") with singleton { this@FlickrApplication }

        bind<Interceptor>("flickrInterceptor") with singleton { createFlickInterceptor() }
        bind<Interceptor>("cachingInterceptor") with singleton { createCachingInterceptor(instance("appContext")) }
        bind<Interceptor>("offlineCachingInterceptor") with singleton { createOfflineCachingInterceptor(instance("appContext")) }

        bind<Cache>("netCache") with singleton { createCache(instance("appContext")) }
        bind<OkHttpClient>() with singleton { createOkHttpClient() }
        bind<Gson>() with singleton { createGson() }
        bind<Retrofit>() with singleton { createRetrofit() }

        bind<Scheduler>("ioScheduler") with singleton { io() }

        bind<FlickrApi>() with singleton { instance<Retrofit>().create(FlickrApi::class.java) }
        bind<FlickrSearcher>() with singleton { FlickrSearcher(instance(), instance("ioScheduler")) }

        bind<AppDatabase>() with singleton { createDb(instance("appContext")) }
        bind<RecentSearcher>() with singleton {
            RecentSearchService(instance<AppDatabase>().recentSearchDao())
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
            .addNetworkInterceptor(instance("cachingInterceptor"))
            .addInterceptor(instance("offlineCachingInterceptor"))
            .addInterceptor(instance("flickrInterceptor"))
            .cache(instance("netCache"))
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

    /**
     * This is just a base logic of just dummy caching queries.
     * In fact it can lead to artifacts in a paging system.
     * Suppose we cached 5 pages of data. Since then search result has been changed.
     * For example some photos on a first page, will be now on a 4 page.
     * Now we turn on network on a phone, but it will not immediately appear,
     * since we, for example, in metro. Then we just load 1-3 pages from cache, and before
     * loading 4 page internet connection was established. So we load in a 4th page an actual
     * data, not the cached one. And we have repeated items.
     *
     * Of course, it is not the common case. Moreover it is pretty rare case.
     * So as an MVP this solutions is just fine.
     * <br><br>
     * But programmer, <strong>remember<strong>, todo you need to enhance it later
     */
    private fun createCachingInterceptor(context: Context) = Interceptor {
        val response = it.proceed(it.request())
        val cacheControl = if (isNetworkAvailable(context)) {
            CacheControl.Builder().maxAge(60, TimeUnit.SECONDS).build() //todo to settings.xml
        } else {
            createStaleCacheControl()
        }

        response.newBuilder()
                .header(HEADER_CACHE, cacheControl.toString())
                .build()
    }

    private fun createOfflineCachingInterceptor(context: Context) = Interceptor {
        var request = it.request()

        if (!isNetworkAvailable(context)) {
            request = request.newBuilder().cacheControl(createStaleCacheControl()).build()
        }

        it.proceed(request)
    }

    private fun createStaleCacheControl() = CacheControl.Builder().maxStale(28, TimeUnit.DAYS).build()

    private fun createCache(context: Context): Cache {
        val cacheSize: Long = 10 * 1024 * 1024
        val cacheDir = File(context.cacheDir, "net_responses")
        return Cache(cacheDir, cacheSize)
    }

    private fun createDb(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "app_db").build()

    companion object {
        private const val HEADER_CACHE = "Cache-Control"

        lateinit var appContext: Context
    }
}