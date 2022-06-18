package io.github.sgpublic.bilidownload.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.sgpublic.bilidownload.core.module.ApiModule

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    var cid: Long,
    var ep_index: Int,
    var sid: Long,
    var duration: Long = -1,
    var watch_time: Long = ApiModule.TS
)