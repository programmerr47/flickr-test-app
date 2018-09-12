package com.github.programmerr47.flickrawesomeclient.services

import com.github.programmerr47.flickrawesomeclient.db.RecentSearch
import com.github.programmerr47.flickrawesomeclient.db.RecentSearchDao
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.junit.Test

class RecentSearchesServiceTest {

    private val testTimeProvider = TestTimeProvider()
    private val testDao = TestRecentSearchDao()

    @Test
    fun checkSuccessSaving() {
        val searcher = RecentSearchService(testDao, 200, testTimeProvider)

        testTimeProvider.sleep(100)
        searcher.save("test_str")

        assertEquals(RecentSearch("test_str", 300), testDao.entries.first())
    }

    @Test
    fun checkSuccessFiltering() {
        val searcher = RecentSearchService(testDao, 4, testTimeProvider)
        testDao.entries.addAll(listOf(
                RecentSearch("str1", 1),
                RecentSearch("str2", 2),
                RecentSearch("str3", 3),
                RecentSearch("st4r", 4),
                RecentSearch("str5", 5)
        ))

        testTimeProvider.sleep(2)
        val result = searcher.getFiltered("str").blockingGet()

        assertEquals(2, result.size)
        assertEquals("str3", result[0])
        assertEquals("str5", result[1])
    }

    @Test
    fun checkExpirationMechanism() {
        val searcher = RecentSearchService(testDao, 10, testTimeProvider)
        searcher.save("str1")
        testTimeProvider.sleep(9)
        searcher.save("str2")
        testTimeProvider.sleep(5)


        val result = searcher.getFiltered("str").blockingGet()

        assertEquals(1, result.size)
        assertEquals("str2", result[0])

        assertEquals(2, testDao.entries.size)
        assertEquals(RecentSearch("str1", 10), testDao.entries[0])
        assertEquals(RecentSearch("str2", 19), testDao.entries[1])
    }

    @Test
    fun checkReassigningValueAndRefreshingExpirationTime() {
        val searcher = RecentSearchService(testDao, 10, testTimeProvider)
        searcher.save("str1")
        testTimeProvider.sleep(9)
        searcher.save("str1")
        testTimeProvider.sleep(5)


        val result = searcher.getFiltered("str").blockingGet()

        assertEquals(1, result.size)
        assertEquals("str1", result[0])

        assertEquals(2, testDao.entries.size)
        assertEquals(RecentSearch("str1", 10), testDao.entries[0])
        assertEquals(RecentSearch("str1", 19), testDao.entries[1])
    }

    private class TestTimeProvider : TimeProvider {
        private var ms: Long = 0

        override fun currentMs() = ms

        fun sleep(ms: Long) {
            this.ms += ms
        }
    }

    private class TestRecentSearchDao : RecentSearchDao {
        val entries: MutableList<RecentSearch> = arrayListOf()

        override fun getFiltered(start: String) = Single.just(entries.filter { it.value.startsWith(start) })

        override fun insert(recentSearch: RecentSearch) {
            entries += recentSearch
        }

        override fun clearExpired(timeMs: Long) {
            throw UnsupportedOperationException()
        }

    }
}