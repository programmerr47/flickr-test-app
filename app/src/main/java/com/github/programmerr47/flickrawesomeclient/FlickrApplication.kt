package com.github.programmerr47.flickrawesomeclient

import android.app.Application
import android.arch.paging.PagedList
import android.arch.persistence.room.Room
import android.content.Context
import android.os.Handler
import android.os.Looper
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
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.*

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

        bind<Executor>("uiExecutor") with singleton { createUiExecutor() }
        bind<Executor>("ioExecutor") with singleton { createIoExecutor(instance("appContext")) }
        bind<Scheduler>("ioScheduler") with singleton { Schedulers.from(instance("ioExecutor")) }

        bind<FlickrApi>() with singleton { instance<Retrofit>().create(FlickrApi::class.java) }

        bind<AppDatabase>() with singleton { createDb(instance("appContext")) }
        bind<RecentSearcher>() with singleton { createRecentSearcher(instance("appContext")) }

        bind<PagedList.Config>("searchListConfig") with provider {
            val defPerPage = instance<Context>().resources.getInteger(R.integer.query_search_photos_per_page)
            PagedList.Config.Builder()
                    .setEnablePlaceholders(true)
                    .setPageSize(defPerPage)
                    .build()
        }
        bind<FlickrSearcher>() with singleton { FlickrSearcher(
                instance(),
                instance("searchListConfig"),
                instance("ioExecutor"),
                instance("ioScheduler"),
                instance("uiExecutor")
        ) }
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
        val cacheControl = if (context.isNetworkAvailable) {
            CacheControl.Builder().maxAge(60, SECONDS).build() //todo to settings.xml
        } else {
            createStaleCacheControl(context)
        }

        response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
    }

    private fun createOfflineCachingInterceptor(context: Context) = Interceptor {
        var request = it.request()

        if (!context.isNetworkAvailable) {
            request = request.newBuilder().cacheControl(createStaleCacheControl(context)).build()
        }

        it.proceed(request)
    }

    private fun createStaleCacheControl(context: Context) = CacheControl.Builder()
            .maxStale(context.resources.getInteger(R.integer.net_cache_expiration_days), DAYS)
            .build()

    private fun createCache(context: Context): Cache {
        val cacheSize: Long = context.resources.getInteger(R.integer.net_cache_size_mb).toLong()
        val cacheDir = File(context.cacheDir, "net_responses")
        return Cache(cacheDir, cacheSize)
    }

    private fun createDb(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "app_db").build()

    private fun createRecentSearcher(context: Context): RecentSearcher {
        val expiresAfterD = context.resources.getInteger(R.integer.net_cache_expiration_days).toLong()
        return RecentSearchService(instance<AppDatabase>().recentSearchDao(), DAYS.toMillis(expiresAfterD)).apply {
            instance<Scheduler>("ioScheduler").scheduleDirect {
                clean() //no sure it is right place to do that, but for the first solution is acceptable
            }
        }
    }

    private fun createIoExecutor(context: Context): Executor {
        val threadCount = context.resources.getInteger(R.integer.thread_io_count)
        return Executors.newFixedThreadPool(threadCount)
    }

    private fun createUiExecutor(): Executor {
        val uiHandler = Handler(Looper.getMainLooper())
        return Executor { uiHandler.post(it) }
    }
}