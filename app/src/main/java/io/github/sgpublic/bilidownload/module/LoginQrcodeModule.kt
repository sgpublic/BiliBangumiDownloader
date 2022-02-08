package io.github.sgpublic.bilidownload.module

import android.content.Context
import android.graphics.Bitmap
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.util.MyLog
import io.github.sgpublic.bilidownload.util.QRCodeUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.net.UnknownHostException
import java.util.*

class LoginQrcodeModule(private val context: Context) {
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
        onLoad!!.onGetStart()
        val call: Call = BaseAPI().getTvQrcodeRequest()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Thread.sleep(500)
                if (e is UnknownHostException) {
                    onLoad!!.onFailure(-101, context.getString(R.string.error_network), e)
                } else {
                    onLoad!!.onFailure(-102, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Thread.sleep(500)
                val result = response.body?.string().toString()
                try {
                    val json = JSONObject(result)
                    if (json.getInt("code") == 0) {
                        val data = json.getJSONObject("data")
                        onLoad!!.onResolve(QRCodeUtil.createQRCodeBitmap(
                            data.getString("url"), 500, 500
                        ))
                        startConfirmScanResult(data.getString("auth_code"))
                    } else {
                        onLoad!!.onFailure(-104, json.getString("message"), null)
                    }
                } catch (e: JSONException) {
                    onLoad!!.onFailure(-103, null, e)
                }
            }
        })
    }

    private var timer: Timer? = null
    fun startConfirmScanResult(authCode: String) {
        if (timer == null) {
            timer = Timer()
        }
        timer?.schedule(object : TimerTask() {
            override fun run() {
                val call = BaseAPI().getTvQrcodeResultRequest(authCode)
                try {
                    val result = call.execute().body?.string().toString()
                    try {
                        val json = JSONObject(result)
                        MyLog.d("确认扫描结果，code: ${json.getInt("code")}，message：${json.getString("message")}")
                        when(json.getInt("code")) {
                            0 -> {
                                cancel()
                                timer?.cancel()
                                timer = null
                                val data = json.getJSONObject("data")
                                onConfirm!!.onSuccess(
                                    data.getLong("mid"),
                                    data.getString("access_token"),
                                    data.getString("refresh_token"),
                                    BaseAPI.TS + data.getLong("expires_in") * 1000,
                                )
                            }
                            86038 -> {
                                cancel()
                                timer?.cancel()
                                timer = null
                                onConfirm!!.onExpired()
                            }
                            86039 -> { }
                            else -> onConfirm!!.onFailure(-114, json.getString("message"), null)
                        }
                    } catch (e: JSONException) {
                        cancel()
                        timer?.cancel()
                        timer = null
                        onConfirm!!.onFailure(-113, null, e)
                    }
                } catch (e: IOException) {
                    if (e is UnknownHostException) {
                        onConfirm!!.onFailure(-111, context.getString(R.string.error_network), e)
                    } else {
                        onConfirm!!.onFailure(-112, e.message, e)
                    }
                    cancel()
                    timer?.cancel()
                    timer = null
                }
            }
        }, 3000, 2000)
    }

    interface QrcodeLoadCallback {
        fun onGetStart()
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onResolve(qrcode: Bitmap)
    }

    interface QrcodeConfirmCallback {
        fun onFailure(code: Int, message: String?, e: Throwable?)
        fun onScanned()
        fun onExpired()
        fun onSuccess(mid: Long, accessKey: String, refreshKey: String, expiresIn: Long)
    }
}