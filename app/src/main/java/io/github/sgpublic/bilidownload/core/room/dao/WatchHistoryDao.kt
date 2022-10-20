package io.github.sgpublic.bilidownload.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.sgpublic.bilidownload.core.room.entity.WatchHistoryEntity

@Dao
interface WatchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(history: WatchHistoryEntity)

    @Query("select * from watch_history where cid=:cid")
    fun getByCid(cid: Long): WatchHistoryEntity?

    @Query("select cid from watch_history where sid=:sid")
    fun getCidListBySeasonId(sid: Long): List<Long>

    @Query("select ep_index from watch_history where sid=:sid order by watch_time desc limit 1")
    fun getLatestBySeasonId(sid: Long): Int?

    @Query("select duration from watch_history where cid=:cid")
    fun getDurationByCid(cid: Long): Long?

    @Query("update watch_history set duration=:duration where cid=:cid")
    fun updateDurationByCid(cid: Long, duration: Long)
}