package io.github.sgpublic.bilidownload.data.parcelable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Suppress("PropertyName", "SpellCheckingInspection")
@Parcelize
data class EntryJson(
    var media_type: Int = 0,
    var has_dash_audio: Boolean = false,
    var is_completed: Boolean = true,
    var total_bytes: Long = 0,
    var downloaded_bytes: Long = 0,
    var title: String = "",
    var type_tag: String = "",
    var cover: String = "",
    var video_quality: Int = 0,
    var prefered_video_quality: Int = 0,
    var guessed_total_bytes: Int = 0,
    var total_time_milli: Int = 0,
    var danmaku_count: Int = 0,
    var time_update_stamp: Int = 0,
    var time_create_stamp: Int = 0,
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
        var website: String = ""
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
        var from: String = "",
        var season_type: Int = 0,
        var width: Int = 0,
        var height: Int = 0,
        var rotate: Int = 0,
        var link: String = "",
        var bvid: String = "",
        var sort_index: Int = 0
    ): Parcelable, Serializable
}