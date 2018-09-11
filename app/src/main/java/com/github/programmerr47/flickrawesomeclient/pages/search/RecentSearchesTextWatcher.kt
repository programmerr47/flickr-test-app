package com.github.programmerr47.flickrawesomeclient.pages.search

import android.text.Editable
import com.github.programmerr47.flickrawesomeclient.services.RecentSearchesService
import com.github.programmerr47.flickrawesomeclient.util.sugar.AbstractTextWatcher
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.concurrent.TimeUnit.*

class RecentSearchesTextWatcher(
        val service: RecentSearchesService,
        val ioScheduler: Scheduler
) : AbstractTextWatcher() {

    private val recentSearchSubject = PublishRelay.create<String>()

    val recentsObservable: Observable<List<String>> by lazy {
        recentSearchSubject
                .debounce(300, MILLISECONDS)
                .distinctUntilChanged()
                .switchMapSingle {
                    if (it.isValidRecent()) service.getFiltered(it)
                    else Single.just(emptyList())
                }
                .subscribeOn(ioScheduler)
    }

    fun update(search: String) {
        if (search.isValidRecent()) {
            ioScheduler.scheduleDirect {
                service.update(search)
            }
        }
    }

    private fun String.isValidRecent() = length >= 3

    override fun afterTextChanged(s: Editable) = recentSearchSubject.accept(s.toString().trim())
}