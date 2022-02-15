package io.github.sgpublic.bilidownload.module

import android.content.Context
import android.content.pm.PackageManager
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import okhttp3.Call
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class UpdateModule(private val context: Context) {
    fun getUpdate(callback: Callback) {
        val helper = BaseAPI()
        val call = helper.getGithubReleaseRequest()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.postFailure(-711, context.getString(R.string.error_network), e)
                } else {
                    callback.postFailure(-712, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val json = JSONArray(result)
                    if (json.length() == 0) {
                        callback.onUpToDate()
                        return
                    }
                    parse(json.getJSONObject(0), callback)
                } catch (e: JSONException) {
                    callback.postFailure(-703, null, e)
                } catch (e: PackageManager.NameNotFoundException) {
                    callback.postFailure(-705, null, e)
                }
            }
        })
    }

    private fun parse(json: JSONObject, callback: Callback) {
        if (json.getBoolean("draft")) {
            callback.onUpToDate()
            return
        }
        val pre = json.getBoolean("prerelease")
        val detail = json.getString("body")
        val parse = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val format = SimpleDateFormat("yyMMdd", Locale.CHINA)
        val remoteVer = format.format(
            parse.parse(json.getString("created_at")) ?: Date(0)
        ).toIntOrNull() ?: 0
        if (remoteVer <= BuildConfig.VERSION_CODE) {
            callback.onUpToDate()
            return
        }
        val assets = json.getJSONArray("assets")
        if (assets.length() == 0) {
            callback.onUpToDate()
            return
        }
        val url = assets.getJSONObject(0).getString("browser_download_url")
        callback.onUpdate(pre, remoteVer, detail, url)
    }

    interface Callback : BaseAPI.BaseInterface {
        override fun onFailure(code: Int, message: String?, e: Throwable?) {}
        fun onUpToDate() {}
        fun onUpdate(isPreRelease: Boolean, remoteVer: Int, detail: String, url: String)
    }
}