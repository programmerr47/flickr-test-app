package com.github.programmerr47.flickrawesomeclient.services

/**
 * We need to introduce this small provider to make RecentSearchesService testable,
 * since we will control expiration flow, without using ugly Thread.sleep()
 */

interface TimeProvider {
    fun currentMs(): Long
}

object SystemTimeProvider : TimeProvider {
    override fun currentMs() = System.currentTimeMillis()
}