package com.github.programmerr47.flickrawesomeclient.services

import com.github.programmerr47.flickrawesomeclient.db.RecentSearch
import com.github.programmerr47.flickrawesomeclient.db.RecentSearchDao
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.concurrent.TimeUnit

interface RecentSearcher {
    fun save(search: String)
    fun getFiltered(start: String): Single<List<String>>
}

class RecentSearchService(
        private val dao: RecentSearchDao,
        private val timeProvider: TimeProvider = SystemTimeProvider,
        private val expiresAfter: Long = TimeUnit.DAYS.toMillis(7)
) : RecentSearcher {
    override fun save(search: String) {
        var expirationTimeMs = timeProvider.currentMs() + expiresAfter
        if(expirationTimeMs < expiresAfter) expirationTimeMs = Long.MAX_VALUE

        dao.insert(RecentSearch(search, expirationTimeMs))
    }

    override fun getFiltered(start: String) = dao.getFiltered(start)
            .map {
                val ms = timeProvider.currentMs()
                it.filter { it.expirationMs > ms }.map { it.value }
            }

    private fun String.isValidRecent() = length >= 3
}

class RecentSearchSubject(
        private val recentSearcher: RecentSearcher,
        private val ioScheduler: Scheduler,
        private val defaultDebounce: Int
) {
    private val recentSearchSubject = PublishRelay.create<String>()

    val recentsObservable: Observable<List<String>> by lazy {
        recentSearchSubject
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMapSingle {
                    if (it.isValidRecent()) recentSearcher.getFiltered(it)
                    else Single.just(emptyList())
                }
                .subscribeOn(ioScheduler)
    }

    fun accept(search: String) = recentSearchSubject.accept(search)

    fun save(search: String) {
        if (search.isValidRecent()) {
            ioScheduler.scheduleDirect {
                recentSearcher.save(search)
            }
        }
    }

    private fun String.isValidRecent() = length >= 3
}
