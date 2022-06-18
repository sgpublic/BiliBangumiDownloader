package io.github.sgpublic.bilidownload.core.module

import io.github.sgpublic.bilidownload.core.data.parcelable.DashIndexJson
import io.github.sgpublic.bilidownload.core.data.parcelable.EntryJson
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.module.PlayModule.Locale.Companion.from
import io.github.sgpublic.bilidownload.core.util.LogCat
import io.github.sgpublic.bilidownload.core.util.isSimplifyChinese
import io.github.sgpublic.bilidownload.core.util.take
import okhttp3.Call
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class PlayModule(private val accessKey: String, private val entry: EntryJson) {
    companion object {
        private val invalidCode = HashSet<Int>().also {
            it.add(-10403) // {"code":-10403,"message":"抱歉您所在地区不可观看！","ttl":1}
            it.add(-412) // {"code":-412,"message":"请求被拦截","ttl":1,"data":null}
        }
    }

    fun getPlayUrl(callback: Callback, qn: Int = ConfigManager.QUALITY, canLogin: Boolean = true) {
        val iterator = ConfigManager.API_SERVERS.iterator()
        val helper = ApiModule(accessKey)
        val call = helper.getEpisodeRequest(entry.source.cid, entry.ep.episode_id, qn)
        call.enqueue(object : ApiModule.BaseOkHttpCallback(callback) {
            override fun onParseData(data: JSONObject) {
                try {
                    callback.onResolveAvailableQuality(parseEpisodeQuality(data))
                    val index = parsePlayUrl(data)
                    if (index == null) {
                        callback.postFailure(-505, null, null)
                        return
                    }
                    getSubtitles(index, callback)
                } catch (e: JSONException) {
                    LogCat.d(e.message.toString(), e)
                    if (iterator.hasNext()) {
                        callNext()
                    } else {
                        callback.postFailure(-503, null, e)
                    }
                }
            }

            override fun onFailedResponse(data: JSONObject) {
                val code = data.getInt("code")
                if (invalidCode.contains(code) && iterator.hasNext()) {
                    callNext()
                } else {
                    callback.postFailure(-504, data.getString("message"), null)
                }
            }

            override fun onWrongPassword() {
                if (canLogin) super.onWrongPassword()
            }

            private fun callNext() {
                helper.getEpisodeRequest(
                    entry.source.cid, entry.ep.episode_id,
                    qn, iterator.next(), Locale.of(entry.title)
                ).enqueue(this)
            }
        })
    }

    private fun parsePlayUrl(json: JSONObject): DashIndexJson? {
        if (json.isNull("dash")) {
            return null
        }
        val index = DashIndexJson()
        val dash = json.getJSONObject("dash")
        val videoObj = dash.getJSONArray("video")
        var video: JSONObject? = null
        for (i in 0 until videoObj.length()) {
            val data = videoObj.getJSONObject(i)
            val id = data.getInt("id")
            if (id > entry.video_quality || (video != null && video.getInt("id") >= id)) {
                continue
            }
            video = data
        }
        if (video == null) {
            return null
        }
        index.video.apply {
            base_url = video.getString("base_url")
            backup_url.clear()
            val backup = video.getJSONArray("backup_url")
            for (j in 0 until backup.length()) {
                backup_url.add(backup.getString(j))
            }
            bandwidth = video.getLong("bandwidth")
            codecid = video.getInt("codecid")
            size = video.getLong("size")
            md5 = video.getString("md5")
            frame_rate = video.getString("frame_rate")
            width = video.getInt("width")
            height = video.getInt("height")
        }

        val audioObj = dash.getJSONArray("audio")
        var audio: JSONObject? = null
        for (i in 0 until audioObj.length()) {
            val data = audioObj.getJSONObject(i)
            val id = data.getInt("id") % 100
            if (id > entry.video_quality || (audio != null && audio.getInt("id") % 100 >= id)) {
                continue
            }
            audio = data
        }
        if (audio == null) {
            return null
        }
        index.audio.apply {
            id = audio.getInt("id")
            base_url = audio.getString("base_url")
            backup_url.clear()
            val backup = audio.getJSONArray("backup_url")
            for (j in 0 until backup.length()) {
                backup_url.add(backup.getString(j))
            }
            bandwidth = audio.getLong("bandwidth")
            codecid = audio.getInt("codecid")
            size = audio.getLong("size")
            md5 = audio.getString("md5")
            frame_rate = audio.getString("frame_rate")
            width = audio.getInt("width")
            height = audio.getInt("height")
        }
        return index
    }

    private fun parseEpisodeQuality(json: JSONObject): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        if (json.isNull("support_formats")) {
            val acceptDescription: JSONArray = json.getJSONArray("accept_description")
            val acceptQuality: JSONArray = json.getJSONArray("accept_quality")
            for (i in 0 until acceptDescription.length()) {
                map[acceptQuality.getInt(i)] = acceptDescription.getString(i)
            }
        } else {
            val supportFormats: JSONArray = json.getJSONArray("support_formats")
            for (array_index in 0 until supportFormats.length()) {
                val supportFormat: JSONObject = supportFormats.getJSONObject(array_index)
                val newDescription: String = supportFormat.getString("new_description")
                val acceptQuality: Int = supportFormat.getInt("quality")
                map[acceptQuality] = newDescription
            }
        }
        return map
    }

    private fun getSubtitles(index: DashIndexJson, callback: Callback) {
        val call = ApiModule().getSubtitlesRequest(entry.source.cid, entry.ep.bvid)
        call.enqueue(object : ApiModule.BaseOkHttpCallback(callback) {
            override fun onFailure(call: Call, e: IOException) {
                LogCat.d("fail to get subtitles")
                onResolvePlayData()
            }

            override fun onParseData(data: JSONObject) {
                val subtitle = getSubtitles(data.getJSONObject("data"))
                index.subtitles.addAll(subtitle)
                onResolvePlayData()
            }

            override fun onFailedResponse(data: JSONObject) {
                LogCat.d("fail to get subtitles: ${data.getString("message")}")
                onResolvePlayData()
            }

            private fun onResolvePlayData() {
                callback.onResolvePlayData(entry, index)
            }
        })
    }

    private fun getSubtitles(json: JSONObject): LinkedList<DashIndexJson.SubtitleData> {
        val data = LinkedList<DashIndexJson.SubtitleData>()
        if (!json.isNull("subtitle")) {
            val subtitles = json.getJSONObject("subtitle")
                .getJSONArray("subtitles")
            for (i in 0 until subtitles.length()) {
                val index = subtitles.getJSONObject(i)
                val subtitle = DashIndexJson.SubtitleData()
                subtitle.id_str = index.getString("id_str")
                subtitle.lan = index.getString("lan")
                subtitle.lan_doc = index.getString("lan_doc")
                subtitle.subtitle_url = index.getString("subtitle_url").let {
                    return@let if (it.startsWith("//")) "http:${it}" else it
                }
                data.add(subtitle)
            }
        }
        return data
    }

    interface Callback : ApiModule.Callback {
        fun onResolveAvailableQuality(qualities: Map<Int, String>) { }
        fun onResolvePlayData(entry: EntryJson, index: DashIndexJson) { }
    }

    enum class Locale(
        val locale: String
    ) {
        /** 大陆地区 */
        CN("cn"),
        /** 香港地区 */
        HK("hk"),
        /** 台湾地区 */
        TW("tw");

        override fun toString(): String {
            return locale
        }

        companion object {
            val DEFAULT: Locale get() = HK
            private val pattern = Pattern.compile("(仅限|僅限)(港?)(澳?)(台?|台灣?|台湾?)(等?)(地区|地區)")

            private const val TAG_HK = "港"
            private const val TAG_TW = "台"

            fun of(sessionTitle: String): Locale {
                val matcher = pattern.matcher(sessionTitle)
                if (!matcher.find()) {
                    return sessionTitle.isSimplifyChinese()
                        .take(CN, DEFAULT)
                }
                val find = matcher.group()
                if (find.contains(TAG_HK)) {
                    return HK
                }
                if (find.contains(TAG_TW)) {
                    return TW
                }
                return DEFAULT
            }

            fun from(locale: Locale?): String? {
                return locale.takeIf { it != CN }?.locale
            }
        }
    }
}

fun PlayModule.Locale?.toKeyPair(): Pair<String, String>? {
    return from(this)?.let { "area" to it }
}