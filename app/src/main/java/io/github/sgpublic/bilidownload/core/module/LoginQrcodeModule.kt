package io.github.sgpublic.bilidownload.core.module

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.util.LogCat
import io.github.sgpublic.bilidownload.core.util.createQRCodeBitmap
import okhttp3.Call
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.net.UnknownHostException
import java.util.*

class LoginQrcodeModule(private val context: AppCompatActivity) {
    private var onLoad: QrcodeLoadCallback? = null
    private var onConfirm: QrcodeConfirmCallback? = null

    fun setQrcodeOnLoadCallback(onLoad: QrcodeLoadCallback) {
        this.onLoad = onLoad
    }

    fun setQrcodeOnConfirmCallback(onConfirm: QrcodeConfirmCallback) {
        this.onConfirm = onConfirm
    }

    fun getQrcode() {
        onLoad ?: return
        onLoad?.onGetStart()
        val call: Call = ApiModule().getTvQrcodeRequest()
        call.enqueue(object : ApiModule.BaseOkHttpCallback(onLoad) {
            override fun onParseData(data: JSONObject) {
                Thread.sleep(500)
                val dataObj = data.getJSONObject("data")
                onLoad?.onResolve(dataObj.getString("url")
                    .createQRCodeBitmap(500, 500))
                startConfirmScanResult(dataObj.getString("auth_code"))
            }

            override fun onFailedResponse(data: JSONObject) {
                Thread.sleep(500)
                super.onFailedResponse(data)
            }

            override fun onWrongPassword() { }
        })
    }

    private var timer: Timer? = null
    fun startConfirmScanResult(authCode: String) {
        if (timer == null) {
            timer = Timer()
        }
        timer?.schedule(object : TimerTask() {
            override fun run() {
                val call = ApiModule().getTvQrcodeResultRequest(authCode)
                try {
                    val result = call.execute().body.string()
                    try {
                        val json = JSONObject(result)
                        LogCat.i("确认扫描结果，code: ${json.getInt("code")}，message：${json.getString("message")}")
                        when(json.getInt("code")) {
                            0 -> {
                                cancel()
                                timer?.cancel()
                                timer = null
                                val data = json.getJSONObject("data")
                                onConfirm?.onSuccess(
                                    data.getLong("mid"),
                                    data.getString("access_token"),
                                    data.getString("refresh_token"),
                                    ApiModule.TS + data.getLong("expires_in") * 1000,
                                )
                            }
                            86038 -> {
                                cancel()
                                timer?.cancel()
                                timer = null
                                onConfirm?.onExpired()
                            }
                            86039 -> { }
                            else -> onConfirm?.postFailure(-114, json.getString("message"), null)
                        }
                    } catch (e: JSONException) {
                        cancel()
                        timer?.cancel()
                        timer = null
                        onConfirm?.postFailure(-113, null, e)
                    }
                } catch (e: IOException) {
                    if (e is UnknownHostException) {
                        onConfirm?.postFailure(-111, context.getString(R.string.error_network), e)
                    } else {
                        onConfirm?.postFailure(-112, e.message, e)
                    }
                    cancel()
                    timer?.cancel()
                    timer = null
                }
            }
        }, 3000, 2000)
    }

    interface QrcodeLoadCallback :
        ApiModule.Callback {
        fun onGetStart()
        fun onResolve(qrcode: Bitmap)
    }

    interface QrcodeConfirmCallback : ApiModule.Callback {
        fun onScanned()
        fun onExpired()
        fun onSuccess(mid: Long, accessKey: String, refreshKey: String, expiresIn: Long)
    }
}