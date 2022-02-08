package io.github.sgpublic.bilidownload.module

import android.content.Context
import android.content.pm.PackageManager
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class UpdateModule(private val context: Context) {
    fun getUpdate(type: String = BuildConfig.TYPE_RELEASE, callback: Callback) {
        // TODO
        val helper = BaseAPI()
        val call = helper.getGithubReleaseRequest()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-711, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-712, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                try {
                    val verCodeNow = BuildConfig.VERSION_CODE
                    val json = JSONObject(result)
                    callback.onUpToDate()
                } catch (e: JSONException) {
                    callback.onFailure(-703, null, e)
                } catch (e: PackageManager.NameNotFoundException) {
                    callback.onFailure(-705, null, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable) {}
        fun onUpToDate() {}
        fun onUpdate(detailPage: String, isPreRelease: Boolean, dlUrl: String)
    }
}