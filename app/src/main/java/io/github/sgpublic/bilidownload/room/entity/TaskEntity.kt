package io.github.sgpublic.bilidownload.room.entity

import androidx.annotation.IntRange
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.sgpublic.bilidownload.data.parcelable.DashIndexJson
import io.github.sgpublic.bilidownload.data.parcelable.EntryJson

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    var cid: Long = 0,
    @IntRange(from = 1, to = 5)
    var status: Int = STATUS_WAITING,
    var entry: EntryJson? = null,
    var index: DashIndexJson? = null,
) {
    companion object {
        const val STATUS_WAITING = 1
        const val STATUS_PROCESSING = 2
        const val STATUS_FINISHED = 3
        const val STATUS_PAUSE = 4
        const val STATUS_ERROR = 5
    }
}