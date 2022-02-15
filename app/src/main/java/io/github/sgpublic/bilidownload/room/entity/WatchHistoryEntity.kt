package io.github.sgpublic.bilidownload.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.sgpublic.bilidownload.module.BaseAPI

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    var cid: Long = 0,
    var ep_index: Int = 0,
    var sid: Long = 0,
    var duration: Long = -1,
    var watch_time: Long = BaseAPI.TS
)