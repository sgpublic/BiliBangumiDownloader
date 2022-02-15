package io.github.sgpublic.bilidownload.module

import android.content.Context
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.data.parcelable.DashIndexJson
import io.github.sgpublic.bilidownload.data.parcelable.EntryJson
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.util.MyLog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class PlayModule(private val context: Context, private val accessKey: String,
                 private val entry: EntryJson) {
    fun getPlayUrl(callback: Callback) {
        val iterator = ConfigManager.API_SERVERS.iterator()
        val helper = BaseAPI(accessKey)
        val call = helper.getEpisodeRequest(entry.source.cid)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (iterator.hasNext()) {
                    helper.getEpisodeRequest(entry.source.cid, proxy = iterator.next())
                        .enqueue(this)
                    return
                }
                if (e is UnknownHostException) {
                    callback.postFailure(-501, context.getString(R.string.error_network), e)
                } else {
                    callback.postFailure(-502, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        callback.onResolveAvailableQuality(parseEpisodeQuality(json))
                        val index = parsePlayUrl(json)
                        if (index != null) {
                            getSubtitles(index, callback)
                        } else {
                            callback.postFailure(-505, null, null)
                        }
                        return
                    }
                    if (json.getInt("code") == -10403
                        && iterator.hasNext()) {
                        helper.getEpisodeRequest(entry.source.cid, proxy = iterator.next())
                            .enqueue(this)
                        return
                    }
                    callback.postFailure(-504, json.getString("message"), null)
                } catch (e: JSONException) {
                    MyLog.d(e.message.toString(), e)
                    if (iterator.hasNext()) {
                        helper.getEpisodeRequest(entry.source.cid, proxy = iterator.next())
                            .enqueue(this)
                        return
                    }
                    callback.postFailure(-503, null, e)
                }
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
        val call = BaseAPI().getSubtitlesRequest(entry.source.cid, entry.ep.bvid)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                MyLog.d("fail to get subtitles", e)
                callback.onResolvePlayData(entry, index)
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val json = JSONObject(result)
                    if (json.getInt("code") != 0) {
                        MyLog.d("fail to get subtitles: ${json.getString("message")}")
                    } else {
                        index.subtitles.addAll(getSubtitles(json.getJSONObject("data")))
                    }
                } catch (e: JSONException) {
                    MyLog.d("fail to parse subtitles", e)
                }
                callback.onResolvePlayData(entry, index)
            }
        })
    }

    private fun getSubtitles(json: JSONObject): ArrayList<DashIndexJson.SubtitleData> {
        val data = arrayListOf<DashIndexJson.SubtitleData>()
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

    interface Callback : BaseAPI.BaseInterface {
        fun onResolveAvailableQuality(qualities: Map<Int, String>) { }
        fun onResolvePlayData(entry: EntryJson, index: DashIndexJson) { }
    }
}