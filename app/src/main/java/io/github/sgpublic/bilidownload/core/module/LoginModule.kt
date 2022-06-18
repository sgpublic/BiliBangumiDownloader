package io.github.sgpublic.bilidownload.core.module

import androidx.test.annotation.Beta
import org.json.JSONObject

class LoginModule {
    @Beta
    fun refreshToken(accessToken: String, refreshToken: String, callback: Callback) {
        val helper = ApiModule(accessToken)
        val call = helper.getRefreshTokenRequest(refreshToken)
        call.enqueue(object : ApiModule.BaseOkHttpCallback(callback) {
            override fun onParseData(data: JSONObject) {
                if (data.getInt("code") != 0) {
                    callback.postFailure(-154, data.getString("message"), null)
                    return
                }
                val dataObj = data.getJSONObject("data")
                callback.onSuccess(
                    dataObj.getLong("mid"), dataObj.getString("access_token"),
                    dataObj.getString("refresh_token"),
                    dataObj.getLong("expires_in") * 1000L + ApiModule.TS
                )
            }
        })
    }

    interface Callback :
        ApiModule.Callback {
        fun onLimited()
        fun onSuccess(mid: Long, accessKey: String, refreshKey: String, expiresIn: Long)
    }
}