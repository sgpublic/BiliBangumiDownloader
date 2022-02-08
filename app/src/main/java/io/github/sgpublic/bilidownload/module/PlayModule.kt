package io.github.sgpublic.bilidownload.module

import android.content.Context
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.util.MyLog
import okhttp3.Call
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class PlayModule(private val context: Context, accessKey: String) {
    private val helper = BaseAPI(accessKey)

    fun getPlayUrl(cid: Long, payment: Int, qn: Int , callback: Callback) {
        val indicator = ConfigManager.API_SERVERS.iterator()
        val call = helper.getEpisodeRequest(cid)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (indicator.hasNext()) {
                    helper.getEpisodeRequest(cid, proxy = indicator.next())
                        .enqueue(this)
                    return
                }
                if (e is UnknownHostException) {
                    callback.onFailure(-501, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-502, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        callback.onResolveAvailableQuality(getEpisodeQuality(json))
                        val urls = getPlayUrl(json, qn)
                        if (urls == null) {
                            callback.onFailure(-505, null, null)
                        } else {
                            callback.onResolvePlayUrl(urls.first, urls.second)
                        }
                        return
                    }
                    if (json.getInt("code") == -10403
                        && payment == EpisodeData.PAYMENT_NORMAL
                        && indicator.hasNext()) {
                        helper.getEpisodeRequest(cid, proxy = indicator.next())
                            .enqueue(this)
                        return
                    }
                    callback.onFailure(-504, json.getString("message"), null)
                } catch (e: JSONException) {
                    MyLog.d(e.message.toString(), e)
                    if (indicator.hasNext()) {
                        helper.getEpisodeRequest(cid, proxy = indicator.next())
                            .enqueue(this)
                        return
                    }
                    callback.onFailure(-503, null, e)
                }
            }
        })
    }

    private fun getPlayUrl(json: JSONObject, qn: Int): Pair<PlayUrl, Int>? {
        if (json.isNull("dash")) {
            return null
        }
        val dash = json.getJSONObject("dash")
        var video: String? = null
        val videoBackup: ArrayList<String> = arrayListOf()
        var vId = 0
        val videoObj = dash.getJSONArray("video")
        for (i in 0 until videoObj.length()) {
            val index = videoObj.getJSONObject(i)
            val id = index.getInt("id")
            if (id > qn || vId >= id) {
                continue
            }
            vId = id
            video = index.getString("base_url")
            videoBackup.clear()
            val backup = index.getJSONArray("backup_url")
            for (j in 0 until backup.length()) {
                videoBackup.add(backup.getString(j))
            }
        }
        if (video == null) {
            return null
        }
        var audio: String? = null
        var aId = 0
        val audioBackup: ArrayList<String> = arrayListOf()
        val audioObj = dash.getJSONArray("audio")
        for (i in 0 until audioObj.length()) {
            val index = audioObj.getJSONObject(i)
            val id = index.getInt("id") % 100
            if (id > qn || aId >= id) {
                continue
            }
            aId = id
            audio = index.getString("base_url")
            audioBackup.clear()
            val backup = index.getJSONArray("backup_url")
            for (j in 0 until backup.length()) {
                audioBackup.add(backup.getString(j))
            }
        }
        if (audio == null) {
            return null
        }
        return Pair(PlayUrl(video, videoBackup, audio, audioBackup), vId)
    }

    fun getAvailableQuality(list: List<EpisodeData>, callback: Callback){
        getPlayUrl(list[0].cid, list[0].payment, ConfigManager.DEFAULT_QUALITY, callback)
    }

    @Throws(JSONException::class)
    private fun getEpisodeQuality(json: JSONObject): Map<Int, String> {
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

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResolveAvailableQuality(qualities: Map<Int, String>) { }
        fun onResolvePlayUrl(url: PlayUrl, qn: Int) { }
    }

    data class PlayUrl(
        val video: String,
        val videoBackup: List<String>,
        val audio: String,
        val audioBackup: List<String>
    )
}