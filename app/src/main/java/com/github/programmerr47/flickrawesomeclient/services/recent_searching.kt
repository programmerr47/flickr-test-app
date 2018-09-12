package com.github.programmerr47.flickrawesomeclient.services

import com.github.programmerr47.flickrawesomeclient.db.RecentSearch
import com.github.programmerr47.flickrawesomeclient.db.RecentSearchDao
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.computation
import java.util.concurrent.TimeUnit

interface RecentSearcher {
    fun save(search: String)
    fun getFiltered(start: String): Single<List<String>>
    fun clean()
}

class RecentSearchService(
        private val dao: RecentSearchDao,
        private val expiresAfterMs: Long,
        private val timeProvider: TimeProvider = SystemTimeProvider
) : RecentSearcher {
    override fun save(search: String) {
        var expirationTimeMs = timeProvider.currentMs() + expiresAfterMs
        if(expirationTimeMs < expiresAfterMs) expirationTimeMs = Long.MAX_VALUE

        dao.insert(RecentSearch(search, expirationTimeMs))
    }

    override fun getFiltered(start: String) = dao.getFiltered(start)
            .map {
                val ms = timeProvider.currentMs()
                it.filter { it.expirationMs > ms }.map { it.value }
            }

    override fun clean() = dao.clearExpired(timeProvider.currentMs())
}

class RecentSearchSubject(
        private val recentSearcher: RecentSearcher,
        private val ioScheduler: Scheduler,
        private val defaultDebounceMs: Long,
        private val computationScheduler: Scheduler = computation()
) {
    private val recentSearchSubject = PublishRelay.create<String>()

    val recentsObservable: Observable<List<String>> by lazy {
        recentSearchSubject
                .debounce(defaultDebounceMs, TimeUnit.MILLISECONDS, computationScheduler)
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
