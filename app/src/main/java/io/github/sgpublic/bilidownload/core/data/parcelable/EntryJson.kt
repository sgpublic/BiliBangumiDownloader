package io.github.sgpublic.bilidownload.core.data.parcelable

import android.os.Parcelable
import io.github.sgpublic.bilidownload.core.data.SeriesData
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.util.fromGson
import io.github.sgpublic.bilidownload.core.util.toGson
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Suppress("PropertyName", "SpellCheckingInspection")
@Parcelize
data class EntryJson(
    // DASH: 2, FLV: 1
    var media_type: Int = 2,
    var has_dash_audio: Boolean = true,
    var is_completed: Boolean = false,
    var total_bytes: Long = 0,
    var downloaded_bytes: Long = 0,
    var title: String = "",
    var type_tag: String = "",
    var cover: String = "",
    var video_quality: Int = ConfigManager.QUALITY,
    var prefered_video_quality: Int = 0,
    var guessed_total_bytes: Int = 0,
    var total_time_milli: Int = 0,
    var danmaku_count: Int = 0,
    var time_update_stamp: Long = 0,
    var time_create_stamp: Long = 0,
    var can_play_in_advance: Boolean = true,
    var interrupt_transform_temp_file: Boolean = false,
    var quality_pithy_description: String = "",
    var quality_superscript: String = "",
    var cache_version_code: Int = 0,
    var preferred_audio_quality: Int = 0,
    var audio_quality: Int = 0,
    var season_id: Long = 0,
    var source: Source = Source(),
    var ep: Ep = Ep()
) : Parcelable, Serializable {
    @Parcelize
    data class Source(
        var av_id: Long = 0,
        var cid: Long = 0,
        var website: String = "bangumi"
    ): Parcelable, Serializable

    @Parcelize
    data class Ep(
        var av_id: Long = 0,
        var page: Int = 0,
        var danmaku: Long = 0,
        var cover: String = "",
        var episode_id: Long = 0,
        var index: String = "",
        var index_title: String = "",
        var from: String = "bangumi",
        var season_type: Int = 1,
        var width: Int = 0,
        var height: Int = 0,
        var rotate: Int = 0,
        var link: String = "https://www.bilibili.com/bangumi/play/ep$episode_id",
        var bvid: String = "",
        var sort_index: Int = 0
    ): Parcelable, Serializable

    fun toEpisodeData(): EpisodeData {
        return EpisodeData().let {
            it.index = ep.index
            it.aid = ep.av_id
            it.cid = source.cid
            it.cover = cover
            it.title = title
            it.bvid = ep.bvid
            return@let it
        }
    }

    fun toSeriesData(): SeriesData {
        return SeriesData().also {
            it.cover = cover
            it.title = title
            it.seasonId = season_id
            it.seasonType = ep.season_type
            it.seasonTypeName = type_tag
        }
    }

    override fun toString(): String = "${ep.index} ${ep.index_title}"

    fun toJson(): String {
        return this.toGson()
    }

    companion object {
        const val PAYMENT_NORMAL = 2
        const val PAYMENT_VIP = 13

        fun fromStr(str: String): EntryJson {
            return EntryJson::class.fromGson(str)
        }
    }
}