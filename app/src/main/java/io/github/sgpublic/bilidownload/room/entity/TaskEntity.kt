package io.github.sgpublic.bilidownload.room.entity

import androidx.annotation.IntRange
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.core.data.parcelable.DashIndexJson
import io.github.sgpublic.bilidownload.core.data.parcelable.EntryJson
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.util.writeAndClose
import java.io.File

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    var cid: Long,
    var sid: Long,
    @IntRange(from = 1, to = 6)
    var status: Int = STATUS_WAITING,
    var taskId: Long = -1,
    var message: String = "",
    var entry: EntryJson,
    var index: DashIndexJson? = null,
) {
    companion object {
        const val STATUS_WAITING = 1
        const val STATUS_PREPARING = 2
        const val STATUS_PROCESSING = 3
        const val STATUS_FINISHED = 4
        const val STATUS_PAUSING = 5
        const val STATUS_PAUSED = 6
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TaskEntity) return false
        return other.cid == cid
    }

    override fun hashCode(): Int {
        return cid.toInt()
    }

    fun getBaseDir(): String =
        ConfigManager.BASE_DIR.canonicalPath +
            "/s_${entry.season_id}/" +
            "${entry.ep.episode_id}"

    fun getTagDir(): String = getBaseDir() +
            entry.video_quality

    fun save() {
        Application.DATABASE.TasksDao().save(this)
        File(getBaseDir(), "entry.json").writeAndClose(entry.toJson())
        File(getTagDir(), "index.json").writeAndClose(index?.toJson() ?: return)
    }
}

