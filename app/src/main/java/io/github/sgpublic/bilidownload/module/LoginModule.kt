package io.github.sgpublic.bilidownload.module

import android.content.Context
import io.github.sgpublic.bilidownload.R
import io.reactivex.annotations.Beta
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class LoginModule(private val context: Context) {
    @Beta
    fun refreshToken(accessToken: String, refreshToken: String, callback: Callback) {
        val helper = BaseAPI(accessToken)
        val call = helper.getRefreshTokenRequest(refreshToken)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-151, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-152, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    var json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        json = json.getJSONObject("data")
                        callback.onSuccess(
                            json.getLong("mid"), json.getString("access_token"),
                            json.getString("refresh_token"),
                            json.getLong("expires_in") * 1000L + BaseAPI.TS
                        )
                    } else {
                        callback.onFailure(-154, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    callback.onFailure(-153, null, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onLimited()
        fun onSuccess(mid: Long, accessKey: String, refreshKey: String, expiresIn: Long)
    }
}