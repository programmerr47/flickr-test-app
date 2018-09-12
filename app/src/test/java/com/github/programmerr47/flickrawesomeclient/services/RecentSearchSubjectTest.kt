package com.github.programmerr47.flickrawesomeclient.services

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers.trampoline
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit.*

class RecentSearchSubjectTest {

    private val testScheduler = TestScheduler()
    private val testObserver = TestObserver<List<String>>()

    @Test
    fun checkSavingValidText() {
        val testSearcher: RecentSearcher = mock {}
        val testSubject = testSubject(testSearcher)
        testSubject.save("str")
        verify(testSearcher).save("str")
    }

    @Test
    fun checkSavingToSmallText() {
        val testSearcher: RecentSearcher = mock {}
        val testSubject = testSubject(testSearcher)
        testSubject.save("st")
        verify(testSearcher, never()).save(any())
    }

    @Test
    fun checkNotResultTooEarly() {
        val testSubject = testSubject(mock {
            on { getFiltered(any()) } doReturn Single.just(listOf("str1", "str2", "str3"))
        })

        testSubject.recentsObservable.subscribe(testObserver)
        testSubject.accept("s")

        testObserver.assertNoValues()
        testObserver.assertNoErrors()
    }

    @Test
    fun checkAcceptingTooSmallText() {
        val testSubject = testSubject(mock {
            on { getFiltered(any()) } doReturn Single.just(listOf("str1", "str2", "str3"))
        })

        testSubject.recentsObservable.subscribe(testObserver)
        testSubject.accept("s")

        testScheduler.advanceTimeBy(110, MILLISECONDS)
        testObserver.assertValue(emptyList())
    }

    @Test
    fun checkSimpleAcceptingText() {
        val testSubject = testSubject(mock {
            on { getFiltered(any()) } doReturn Single.just(listOf("str1", "str2", "str3"))
        })

        testSubject.recentsObservable.subscribe(testObserver)
        testSubject.accept("str")

        testScheduler.advanceTimeBy(110, MILLISECONDS)
        testObserver.assertValue(listOf("str1", "str2", "str3"))
    }

    @Test
    fun checkReceivingResultOnlyOnceWithAcceptingSameText() {
        val testSubject = testSubject(mock {
            on { getFiltered(any()) } doReturn Single.just(listOf("str1", "str2", "str3"))
        })
        testSubject.recentsObservable.subscribe(testObserver)

        testSubject.accept("str")
        testScheduler.advanceTimeBy(110, MILLISECONDS)
        testObserver.assertValue(listOf("str1", "str2", "str3"))
        testObserver.values().clear()

        testSubject.accept("str")
        testScheduler.advanceTimeBy(110, MILLISECONDS)
        testObserver.assertNoValues()
    }

    @Test
    fun checkDebouncingWhileAcceptingSeveralValuesPrettyFast() {
        val list = listOf("str11", "str12", "str21", "str22")
        val testSubject = testSubject(mock {
            on { getFiltered(any()) } doAnswer {
                Single.just(list.filter { str -> str.startsWith(it.arguments[0].toString()) })
            }
        })
        testSubject.recentsObservable.subscribe(testObserver)

        testSubject.accept("str")
        testScheduler.advanceTimeBy(90, MILLISECONDS)
        testObserver.assertNoValues()

        testSubject.accept("str1")
        testScheduler.advanceTimeBy(20, MILLISECONDS)
        testObserver.assertNoValues()

        testScheduler.advanceTimeBy(90, MILLISECONDS)
        testObserver.assertValue(listOf("str11", "str12"))
    }

    private fun testSubject(testSearcher: RecentSearcher) = RecentSearchSubject(testSearcher, trampoline(), 100, testScheduler)
}