package io.github.sgpublic.bilidownload.core.data.parcelable

import android.os.Parcelable
import io.github.sgpublic.bilidownload.core.util.fromGson
import io.github.sgpublic.bilidownload.core.util.toGson
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.*

@Suppress("PropertyName", "SpellCheckingInspection")
@Parcelize
data class DashIndexJson(
    var video: DASHData = DASHData(),
    var audio: DASHData = DASHData(),
    val subtitles: LinkedList<SubtitleData> = LinkedList()
) : Parcelable, Serializable {
    @Parcelize
    data class DASHData(
        var id: Int = 0,
        var base_url: String = "",
        var backup_url: LinkedList<String> = LinkedList(),
        var bandwidth: Long = 0L,
        var codecid: Int = 0,
        var size: Long = 0L,
        var md5: String = "",
        var no_rexcode: Boolean = false,
        var frame_rate: String = "",
        var width: Int = 0,
        var height: Int = 0
    ): Parcelable, Serializable

    @Parcelize
    data class SubtitleData(
        var id_str: String = "",
        var lan: String = "",
        var lan_doc: String = "",
        var subtitle_url: String = "",
    ): Parcelable

    fun toJson(): String {
        return this.toGson()
    }

    companion object {
        fun fromStr(str: String): DashIndexJson {
            return DashIndexJson::class.fromGson(str)
        }
    }
}