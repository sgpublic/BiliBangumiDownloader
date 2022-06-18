package io.github.sgpublic.bilidownload.core.module

import io.github.sgpublic.bilidownload.core.data.UserData
import org.json.JSONObject

class UserInfoModule(
    private val accessKey: String,
    private val mid: Long
) {
    fun getInfo(callback: Callback) {
        val call = ApiModule(accessKey).getUserInfoRequest(mid.toString())
        call.enqueue(object : ApiModule.BaseOkHttpCallback(callback) {
            override fun onParseData(data: JSONObject) {
                val dataObj = data.getJSONObject("data")
                val uData = UserData()
                uData.face = dataObj.getString("face")
                uData.level = dataObj.getInt("level")
                uData.name = dataObj.getString("name")
                when (dataObj.getString("sex")) {
                    "男" -> { uData.sex = 1 }
                    "女" -> { uData.sex = 2 }
                    else -> { uData.sex = 0 }
                }
                uData.sign = dataObj.getString("sign")

                val vip = dataObj.getJSONObject("vip")
                uData.vipLabel = vip.getJSONObject("label").getString("text")
                uData.vipType = vip.getInt("type")
                uData.vipState = vip.getInt("status")
                callback.onResult(uData)
            }
        })
    }

    interface Callback : ApiModule.Callback {
        fun onResult(data: UserData)
    }
}