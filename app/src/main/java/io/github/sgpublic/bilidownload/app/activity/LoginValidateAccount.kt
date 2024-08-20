package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import com.dtflys.forest.Forest
import com.dtflys.forest.http.ForestCookie
import com.dtflys.forest.http.ForestRequestType
import com.google.gson.JsonObject
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.forest.ApiModule
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ActivityWebviewBinding
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

/**
 *
 * @author Madray Haven
 * @date 2022/10/19 16:37
 */
class LoginValidateAccount: BaseActivity<ActivityWebviewBinding>() {
    private val url: String by lazy { return@lazy intent.getStringExtra("url") ?: "" }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        if (url.isBlank()) {
            Application.onToast(this, R.string.text_login_phone_validate_invalid)
            finish()
            return
        }
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        ViewBinding.webviewTarget.loadUrl(url)
    }

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.webviewToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.text_login_phone_validate)
        }
        val callback = object : PhoneValidateClient.Callback {
            override fun onFailed() {
                Application.onToast(this@LoginValidateAccount, R.string.text_login_phone_validate_failed)
                setResult(RESULT_CANCELED)
                finish()
            }

            override fun onConfirm(code: String) {
                setResult(RESULT_OK, Intent().also {
                    it.putExtra(CODE, code)
                })
                finish()
            }
        }
        ViewBinding.webviewTarget.webViewClient = when {
            url.contains("/account/mobile/security/managephone/phone/verify") -> PhoneValidateClientA(callback)
            url.contains("/h5-app/passport/risk/verify") -> PhoneValidateClientB(callback)
            else -> {
                log.error("不支持的校验方式：$url")
                callback.onFailed()
                return
            }
        }.also {
            it.onAttachToWebView(ViewBinding.webviewTarget, url)
        }
        ViewBinding.webviewTarget.webChromeClient = object : WebChromeClient() {
            private var shown = false
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                ViewBinding.webviewProcess.progress = newProgress
                if (newProgress == 100) {
                    shown = false
                    ViewBinding.webviewProcess.animate(false, 100)
                } else if (!shown) {
                    shown = true
                    ViewBinding.webviewProcess.animate(true, 100)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    /** 适用于 /x/safecenter/login/tel/verify 的校验规则 */
    private class PhoneValidateClientA(callback: Callback): PhoneValidateClient(callback) {
        private val flag: AtomicBoolean = AtomicBoolean(false)

        override fun shouldOverrideUrlLoading(
            view: WebView, request: WebResourceRequest
        ): Boolean {
            if ("m.bilibili.com" == request.url.host && !flag.get()) {
                callback.onFailed()
            }
            return false
        }

        @Suppress("unused")
        private val exchangeCookie = object {
            val code: Int = 0
            val data = object {
                val redirectUrl: String = "https://m.bilibili.com/index.html"
                val refresh_token: String = BuildConfig.PROJECT_NAME
                val timestamp: Long = ApiModule.TS
            }
        }

        override fun shouldInterceptRequest(
            view: WebView, request: WebResourceRequest
        ): WebResourceResponse? {
            super.shouldInterceptRequest(view, request)
            if (request.url.host == "passport.bilibili.com" &&
                request.url.path == "/web/sso/exchange_cookie" &&
                request.method.uppercase() == "GET") {
                val code = Pattern.compile("code=(.*?)&")
                    .matchString("${request.url}&", "code=&").let {
                        return@let it.substring(5, it.length - 1)
                    }
                flag.set(true)
                if (code.isBlank()) {
                    log.error("找不到 code 用于交换 token")
                    callback.onFailed()
                } else {
                    callback.onConfirm(code)
                }
                // code 只能用一次，所以这里直接构造一个假的响应返回避免 WebView 自行处理请求后把 code 用掉了
                return WebResourceResponse(
                    "application/json",
                    "UTF-8",
                    exchangeCookie.toGson().byteInputStream()
                )
            }
            return null
        }
    }

    class Injection(private val onSave: (String) -> Unit) {
        @JavascriptInterface
        fun logOnOpen(url: String) {
            log.debug("logOnOpen: $url")
        }
        @JavascriptInterface
        fun logOnSend(body: String) {
            log.debug("logOnSend: $body")
        }
        @JavascriptInterface
        fun setVerifyBody(body: String) {
            log.debug("save verify body: $body")
            onSave.invoke(body)
        }
    }

    /** 适用于 /h5-app/passport/risk/verify 的校验规则 */
    private class PhoneValidateClientB(callback: Callback): PhoneValidateClient(callback) {
        private var verifyBody: String? = null

        override fun onAttachToWebView(webView: WebView, url: String) {
            webView.addJavascriptInterface(Injection {
                verifyBody = it
            }, "Injection")
        }

        override fun onPageFinished(view: WebView, url: String) {
            view.context.resources.assets.run {
                val implCode = StringJoiner("\n    ", "(function() {\n    ", "\n})();")
                open("js/ajax_interception.js").reader().readLines().forEach {
//                    implCode.add(it.replace("\"", "\\\""))
                    implCode.add(it)
                }
                val preCode = open("js/phone_validate.js").reader().readText()
                    .replace("%%SRC_CODE%%", implCode.toString())
                log.debug(preCode)
                view.loadUrl("javascript:$implCode")
            }
        }

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            super.shouldInterceptRequest(view, request)
            if (request.url.host == "passport.bilibili.com" &&
                request.url.path == "/x/safecenter/login/tel/verify" &&
                request.method.uppercase() == "POST") {
                if (verifyBody == null) {
                    log.error("未拦截到请求体")
                    callback.onFailed()
                    return null
                }
                val url = request.url.toString()
                try {
                    val resp = Forest.request(JsonObject::class.java)
                        .setType(ForestRequestType.POST)
                        .setUrl(request.url.toString()).addBody(verifyBody)
                        .addCookies(CookieManager.getInstance()
                            .getCookie(url).split("; ").map {
                                ForestCookie.parse(url, it)
                            }
                        )
                        .addHeader(mapOf(
                            "Origin" to "https://passport.bilibili.com",
                            "Referer" to request.url,
                        ))
                        .execute(JsonObject::class.java)
                    callback.onConfirm(resp.getAsJsonObject("data").get("code").asString)
                    resp.getAsJsonObject("data").addProperty("code", "-1")
                    return WebResourceResponse("application/json", "UTF-8", resp.toString().byteInputStream())
                } catch (e: Exception) {
                    log.error("校验请求提交错误", e)
                    callback.onFailed()
                }
            }
            return null
        }
    }

    private abstract class PhoneValidateClient(
        protected val callback: Callback
    ): WebViewClient() {
        interface Callback {
            fun onFailed()
            fun onConfirm(code: String)
        }

        open fun onAttachToWebView(webView: WebView, url: String) { }
        @CallSuper
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            log.debug(request.method.uppercase() + " " + request.url.toString())
            return null
        }
    }

    override fun onDestroy() {
        ViewBinding.webviewTarget.destroy()
        super.onDestroy()
    }

    override val ViewBinding: ActivityWebviewBinding by viewBinding()

    object LoginValidateContract: ActivityResultContract<String, String?>() {
        override fun createIntent(context: Context, input: String): Intent {
            return Intent(context, LoginValidateAccount::class.java).also {
                it.putExtra("url", input)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            return intent?.getStringExtra(CODE)
        }
    }

    companion object {
        const val CODE = "code"
    }
}