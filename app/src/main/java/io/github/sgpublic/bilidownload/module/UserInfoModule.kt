package io.github.sgpublic.bilidownload.module

import android.content.Context
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.data.UserData
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class UserInfoModule(private val context: Context, private val accessKey: String,
                     private val mid: Long) {
    fun getInfo(callback: Callback) {
        val call = BaseAPI(accessKey).getUserInfoRequest(mid.toString())
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.postFailure(-201, context.getString(R.string.error_network), e)
                } else {
                    callback.postFailure(-202, e.message, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        val data = json.getJSONObject("data")
                        val uData = UserData()
                        uData.face = data.getString("face")
                        uData.level = data.getInt("level")
                        uData.name = data.getString("name")
                        when (data.getString("sex")) {
                            "男" -> { uData.sex = 1 }
                            "女" -> { uData.sex = 2 }
                            else -> { uData.sex = 0 }
                        }
                        uData.sign = data.getString("sign")

                        val vip = data.getJSONObject("vip")
                        uData.vipLabel = vip.getJSONObject("label").getString("text")
                        uData.vipType = vip.getInt("type")
                        uData.vipState = vip.getInt("status")
                        callback.onResult(uData)
                    } else {
                        callback.postFailure(-204, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    callback.postFailure(-203, null, e)
                }
            }
        })
    }

    interface Callback : BaseAPI.BaseInterface {
        fun onResult(data: UserData)
    }
}