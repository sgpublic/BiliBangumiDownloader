package io.github.sgpublic.bilidownload.data.parcelable

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Suppress("PropertyName", "SpellCheckingInspection")
@Parcelize
data class FlvIndexJson(
    var from: String = "",
    var quality: Int = 0,
    var type_tag: String = "",
    var new_description: String = "",
    var display_desc: String = "",
    var superscript: String = "",
    var segment_list: ArrayList<Segment> = arrayListOf(),
    var parse_timestamp_milli: Int = 0,
    var available_period_milli: Int = 0,
    var user_agent: String = "",
    var is_downloaded: Boolean = false,
    var is_resolved: Boolean = true,
    var player_codec_config_list: ArrayList<out Serializable> = arrayListOf(),
    var marlin_token: String = "",
    var video_codec_id: Int = 0,
    var video_project: Boolean = false,
    var format: String = "",
    var player_error: Int = 0,
    var stream_limit: StreamLimit = StreamLimit(),
    var need_vip: Boolean = false,
    var need_login: Boolean = false,
    var intact: Boolean = false
) : Parcelable, Serializable {

    @Parcelize
    data class Segment(
        var url: String = "",
        var duration: Int = 0,
        var bytes: Int = 0,
        var meta_url: String = "",
        var md5: String = "",
        var order: Int = 0,
        var backup_urls: ArrayList<String> = arrayListOf()
    ) : Parcelable, Serializable {
    }

    @Parcelize
    data class StreamLimit(
        var title: String = "",
        var uri: String = "",
        var msg: String = ""
    ) : Parcelable, Serializable {
    }
}