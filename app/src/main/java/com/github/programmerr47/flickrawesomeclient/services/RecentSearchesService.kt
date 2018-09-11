package com.github.programmerr47.flickrawesomeclient.services

import com.github.programmerr47.flickrawesomeclient.db.RecentSearch
import com.github.programmerr47.flickrawesomeclient.db.RecentSearchDao
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit

class RecentSearchesService(
        private val dao: RecentSearchDao,
        private val timeProvider: TimeProvider = SystemTimeProvider,
        private val expiresAfter: Long = TimeUnit.DAYS.toMillis(7)
) {
    fun update(search: String) {
        var expirationTimeMs = timeProvider.currentMs() + expiresAfter
        if(expirationTimeMs < expiresAfter) expirationTimeMs = Long.MAX_VALUE

        dao.insert(RecentSearch(search, expirationTimeMs))
    }

    fun getFiltered(start: String) = dao.getFiltered(start)
            .map {
                val ms = timeProvider.currentMs()
                it.filter { it.expirationMs > ms }.map { it.value }
            }
}