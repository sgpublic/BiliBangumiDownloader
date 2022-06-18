package io.github.sgpublic.bilidownload.core.module

import androidx.test.annotation.Beta
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.activity.Login
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.util.LogCat
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.UnknownHostException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

class ApiModule(private val accessToken: String) {
    companion object {
        private const val build = "6700300"
        private const val android_key = "4409e2ce8ffd12b8"
        private const val platform = "android"
        private const val METHOD_GET = 0
        private const val METHOD_POST = 1
        val TS: Long get() = System.currentTimeMillis()
    }

    constructor(): this("")

    fun getTvQrcodeRequest(): Call {
        val url = "https://passport.bilibili.com/x/passport-tv-login/qrcode/auth_code"
        val args: Map<String, Any> = mapOf(
            "appkey" to android_key,
            "local_id" to 0,
            "ts" to TS
        )
        return onReturn(url, args, null, METHOD_POST, true)
    }

    fun getTvQrcodeResultRequest(authCode: String): Call {
        val url = "https://passport.bilibili.com/x/passport-tv-login/qrcode/poll"
        val args: Map<String, Any> = mapOf(
            "appkey" to android_key,
            "auth_code" to authCode,
            "local_id" to 0,
            "ts" to TS
        )
        return onReturn(url, args, null, METHOD_POST, true)
    }

    @Deprecated("use qrcode instead.")
    fun getKeyRequest(): Call {
        val url = "https://passport.bilibili.com/api/oauth2/getKey"
        val args: Map<String, Any> = mapOf(
            "appkey" to android_key,
            "mobi_app" to platform,
            "platform" to platform,
            "ts" to TS,
        )
        return onReturn(url, args, null, METHOD_POST, true)
    }

    @Deprecated("use qrcode instead.")
    fun getLoginRequest(username: String, password_encrypted: String): Call {
        val url = "https://passport.bilibili.com/api/v3/oauth2/login"
        val args: Map<String, Any> = mapOf(
            "appkey" to android_key,
            "build" to build,
            "gee_type" to 10,
            "mobi_app" to platform,
            "password" to password_encrypted,
            "platform" to platform,
            "ts" to TS,
            "username" to username,
        )
        val headerArray: Map<String, Any> = mapOf(
            "User-Agent" to "Mozilla/5.0 (sgpublic2002@gmail.com)",
        )
        return onReturn(url, args, headerArray, METHOD_POST, true)
    }

    @Deprecated("use qrcode instead.")
    fun getLoginWebRequest(cookie: String, user_agent: String = "Mozilla/5.0 (sgpublic2002@gmail.com)"): Call {
        val url = "https://passport.bilibili.com/login/app/third"
        val args: Map<String, Any> = mapOf(
            "appkey" to "27eb53fc9058f8c3",
            "api" to "http://link.acg.tv/forum.php",
            "sign" to "67ec798004373253d60114caaad89a8c",
        )
        val headerArray: Map<String, Any> = mapOf(
            "Cookie" to cookie,
            "User-Agent" to user_agent,
        )
        return onReturn(url, args, headerArray, METHOD_GET, false)
    }

    @Beta
    fun getRefreshTokenRequest(refreshToken: String): Call {
        val url = "https://passport.bilibili.com/api/oauth2/refreshToken"
        val args: Map<String, Any> = mapOf(
            "access_token" to accessToken,
            "appkey" to android_key,
            "refresh_token" to refreshToken,
            "ts" to TS,
        )
        return onReturn(url, args, null, METHOD_POST, true)
    }

    @Deprecated("use qrcode instead.")
    fun getLoginConfirmRequest(
        url: String,
        cookie: String,
        userAgent: String = "Mozilla/5.0 (bbcallen@gmail.com)"
    ): Call {
        val headerArray: Map<String, Any> = mapOf(
            "Connection" to "keep-alive",
            "Upgrade-Insecure-Requests" to 1,
            "User-Agent" to userAgent,
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*,q=0.8",
            "Accept-Encoding" to "gzip, deflate",
            "Accept-Language" to "zh-CH,en-US;q=0.8",
            "Cookie" to cookie,
            "X-Requested-With" to BuildConfig.APPLICATION_ID,
        )
        return onReturn(url, null, headerArray, METHOD_GET, false)
    }

    fun getUserInfoRequest(mid: String): Call {
        val url = "https://api.bilibili.com/x/space/acc/info"
        val args: Map<String, Any> = mapOf(
            "mid" to mid,
        )
        return onReturn(url, args, null, METHOD_GET, false)
    }

    fun getFollowsRequest(mid: Long, pageIndex: Int, status: Int): Call {
        val url = "https://api.bilibili.com/pgc/app/follow/v2/bangumi"
        val args: Map<String, Any> = mapOf(
            "access_key" to accessToken,
            "appkey" to android_key,
            "build" to build,
            "mid" to mid,
            "pn" to pageIndex,
            "ps" to 18,
            "status" to status,
            "ts" to TS,
        )
        return onReturn(url, args, null, METHOD_GET, true)
    }

    fun getHotWordRequest(): Call {
        val url = "https://s.search.bilibili.com/main/hotword"
        return onReturn(url, null, null, METHOD_GET, false)
    }

    fun getSearchResultRequest(keyword: String): Call {
        val url = "https://api.bilibili.com/x/web-interface/search/type"
        val args: Map<String, Any> = mapOf(
            "search_type" to "media_bangumi",
            "keyword" to keyword,
        )
        val headerArray: Map<String, Any> = mapOf(
            "Referer" to "https://search.bilibili.com",
        )
        return onReturn(url, args, headerArray, METHOD_GET, false)
    }

    fun getSearchSuggestRequest(keyword: String): Call {
        val url = "https://s.search.bilibili.com/main/suggest"
        val args: Map<String, Any> = mapOf(
            "main_ver" to "v1",
            "special_acc_num" to 1,
            "topic_acc_num" to 1,
            "upuser_acc_num" to 3,
            "tag_num" to 10,
            "special_num" to 10,
            "bangumi_num" to 10,
            "upuser_num" to 3,
            "term" to keyword,
        )
        val headerArray: Map<String, Any> = mapOf(
            "Referer" to "https://search.bilibili.com",
        )
        return onReturn(url, args, headerArray, METHOD_GET, false)
    }

    fun getSeasonInfoAppRequest(sid: Long, proxy: String = "api.bilibili.com"): Call {
        val url = "https://$proxy/pgc/view/app/season"
        val args: Map<String, Any> = mapOf(
            "access_key" to accessToken,
            "appkey" to android_key,
            "build" to build,
            "platform" to platform,
            "season_id" to sid,
            "ts" to TS,
        )
        return onReturn(url, args, null, METHOD_GET, true)
    }

    fun getSeasonInfoWebRequest(sid: Long): Call {
        val url = "https://bangumi.bilibili.com/view/web_api/season"
        val args: Map<String, Any> = mapOf(
            "access_key" to accessToken,
            "appkey" to android_key,
            "build" to build,
            "c_locale" to "hk_CN",
            "platform" to platform,
            "s_locale" to "hk_CN",
            "season_id" to sid,
            "ts" to TS,
        )
        return onReturn(url, args, null, METHOD_GET, true)
    }

    fun getSeasonInfoBiliplusRequest(sid: Long): Call {
        val url = "https://www.biliplus.com/api/bangumi"
        val args: Map<String, Any> = mapOf(
            "access_key" to accessToken,
            "season" to sid
        )
        return onReturn(url, args, null, METHOD_GET, false)
    }

    fun getSeasonRecommendRequest(sid: Long): Call {
        val url = "https://api.bilibili.com/pgc/season/web/related/recommend"
        val args: Map<String, Any> = mapOf(
            "season_id" to sid
        )
        return onReturn(url, args, null, METHOD_GET, false)
    }

    fun getEpisodeRequest(cid: Long, epId: Long, qn: Int = ConfigManager.DEFAULT_QUALITY,
                          proxy: String? = null, locale: PlayModule.Locale? = null): Call {
        val host = proxy ?: "api.bilibili.com"
        val url = "https://$host/pgc/player/api/playurl"
        val args: Map<String, Any> = filterMapOf(
            "access_key" to accessToken,
            "appkey" to android_key,
            locale.toKeyPair(),
            "build" to build,
            "cid" to cid,
            "device" to platform,
            "ep_id" to epId,
            "fnval" to 976,
            "fnver" to 0,
            "force_host" to 0,
            "fourk" to 0,
            "mobi_app" to platform,
            "platform" to platform,
            "qn" to qn,
            "ts" to TS,
        )
        return onReturn(url, args, null, METHOD_GET, true)
    }

    @Beta
    fun getDanmakuRequest(cid: Long): Call {
        val url = "https://api.bilibili.com/x/v1/dm/list.so"
        val args: Map<String, Any> = mapOf(
            "oid" to cid,
        )
        return onReturn(url, args, null, METHOD_GET, false)
    }

    fun getSubtitlesRequest(cid: Long, bvid: String): Call {
        val url = "https://api.bilibili.com/x/player/v2"
        val args: Map<String, Any> = mapOf(
            "cid" to cid,
            "bvid" to bvid,
        )
        return onReturn(url, args, null, METHOD_GET, false)
    }

    fun getGithubReleaseRequest(): Call {
        val url = "https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases"
        val args: Map<String, Any> = mapOf(
            "per_page" to 1,
            "page" to 1
        )
        return onReturn(url, args, null, METHOD_GET, false)
    }

    private fun onReturn(
        url: String, args: Map<String, Any>?, headerArray: Map<String, Any>?,
        method: Int = METHOD_POST, withSign: Boolean = true
    ): Call {
        val client: OkHttpClient = OkHttpClient.Builder().run{
            readTimeout(5, TimeUnit.SECONDS)
            writeTimeout(5, TimeUnit.SECONDS)
            connectTimeout(10, TimeUnit.SECONDS)
            callTimeout(5, TimeUnit.MINUTES)
            followRedirects(false)
            followSslRedirects(false)
            build()
        }
        val request: Request.Builder = Request.Builder()
        val body = GetArgs(args)
        val bodyString = body.getString(withSign)
        if (method == METHOD_POST) {
            LogCat.v("HTTP请求\nPOST $url\n[Body] $bodyString", 2)
            request.url(url)
            request.post(body.getForm(withSign))
        } else {
            val urlFinal = if (bodyString == "") url else
                "$url?$bodyString"
            LogCat.v("HTTP请求\nGET $urlFinal", 2)
            request.url(urlFinal)
        }
        if (headerArray != null) {
            for ((key, value) in headerArray) {
                request.addHeader(key, value.toString())
            }
        }
        return client.newCall(request.build())
    }

    private class GetArgs(val args: Map<String, Any>?){
        private var string: String
        init {
            string = StringBuilder().run {
                args?.let{
                    for ((argName, argValue) in it){
                        val argValueDecoded = argValue.toString()
                        append("&$argName=$argValueDecoded")
                    }
                }
                toString()
            }
            if (string.length > 1){
                string = string.substring(1)
            }
        }

        fun getString(outSign: Boolean): String {
            return StringBuilder(string).run {
                if (outSign){
                    append("&sign=" + getSign())
                }
                toString()
            }
        }

        fun getForm(outSign: Boolean): FormBody {
            return FormBody.Builder().run {
                args?.let {
                    for ((argName, argValue) in args){
                        val argValueDecoded: String = try {
                            URLDecoder.decode(argValue.toString(), "UTF-8")
                        } catch (e: UnsupportedEncodingException) {
                            argValue.toString()
                        }
                        add(argName, argValueDecoded)
                    }
                }
                if (outSign){
                    add("sign", getSign())
                }
                build()
            }
        }

        private fun getSign(): String {
            val content = string + "59b43e04ad6965f34319062b478f83dd"
            try {
                val instance:MessageDigest = MessageDigest.getInstance("MD5")
                val digest:ByteArray = instance.digest(content.toByteArray())
                return StringBuffer().run {
                    for (b in digest) {
                        val i :Int = b.toInt() and 0xff
                        var hexString = Integer.toHexString(i)
                        if (hexString.length < 2) {
                            hexString = "0$hexString"
                        }
                        append(hexString)
                    }
                    toString()
                }
            } catch (e: NoSuchAlgorithmException) {
                return ""
            }
        }
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Throwable?)

        companion object {
            const val CODE_LOGIN_FAILED = -100
            const val CODE_NETWORK_ERROR = -101
            const val CODE_NETWORK_UNKNOWN = -102
            const val CODE_JSON_ERROR = -103
            const val CODE_SERVER_ERROR = -104
            const val CODE_SERVER_INTERNAL_ERROR = -105
        }
    }

    abstract class BaseOkHttpCallback(
        private val callback: Callback? = null
    ): okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
            LogCat.e("网络请求出错", e)
            if (e is UnknownHostException) {
                callback?.postFailure(Callback.CODE_NETWORK_ERROR, Application.getString(R.string.error_network), e)
            } else {
                callback?.postFailure(Callback.CODE_NETWORK_UNKNOWN, e.message, e)
            }
        }

        final override fun onResponse(call: Call, response: Response) {
            val result: String = response.body.string()
            try {
                val objects = JSONObject(result)
                when (objects.getInt("code")) {
                    0 -> {
                        onParseData(objects)
                    }
                    -403 -> {
                        LogCat.i("登录状态失效")
                        callback?.postFailure(Callback.CODE_LOGIN_FAILED, objects.getString("message"))
                        onWrongPassword()
                    }
                    else -> {
                        onFailedResponse(objects)
                    }
                }
            } catch (e: JSONException) {
                LogCat.e("数据解析失败", e)
                callback?.postFailure(Callback.CODE_JSON_ERROR, e.message, e)
            }
        }

        abstract fun onParseData(data: JSONObject)

        open fun onFailedResponse(data: JSONObject) {
            LogCat.e("服务器处理出错")
            callback?.postFailure(data.getInt("code"), data.getString("message"))
        }

        open fun onWrongPassword() {
            LogCat.i("跳转登录页面")
            try {
                Login.startActivity(Application.APPLICATION_CONTEXT)
            } catch (_: NullPointerException) { }
        }
    }
}

fun ApiModule.Callback.postFailure(code: Int, message: String?, e: Throwable? = null) {
    CrashHandler.saveExplosion(e, code, message)
    onFailure(code, message, e)
}

fun <K, V> filterMapOf(vararg pairs: Pair<K, V>?): Map<K, V> {
    return mapOf(*pairs.filterNotNull().toTypedArray())
}