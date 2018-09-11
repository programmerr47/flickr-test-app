package com.github.programmerr47.flickrawesomeclient.db

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import io.reactivex.Single

/**
 * I've putted all db related classes in one file since they are too small by themself,
 * so it is just more concise and comfort.
 *
 * After we will have several entities and daos, we will need to split that file
 * on several files and then on several packages
 */

@Database(entities = [RecentSearch::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
}

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recentsearch WHERE value LIKE :start || '%'")
    fun getFiltered(start: String): Single<List<RecentSearch>>

    @Insert(onConflict = REPLACE)
    fun insert(recentSearch: RecentSearch)

    @Query("DELETE FROM recentsearch WHERE expirationMs < :timeMs")
    fun clearAll(timeMs: Long)
}

@Entity
data class RecentSearch(
        @PrimaryKey val value: String,
        @ColumnInfo val expirationMs: Long
)