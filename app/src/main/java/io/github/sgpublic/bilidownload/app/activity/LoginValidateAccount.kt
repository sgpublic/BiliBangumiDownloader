package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContract
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.app.BaseActivity
import io.github.sgpublic.bilidownload.core.forest.ApiModule
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ActivityWebviewBinding
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
        ViewBinding.webviewTarget.loadUrl(url)
    }

    override fun onViewSetup() {
        setSupportActionBar(ViewBinding.webviewToolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.text_login_phone_validate)
        }
        ViewBinding.webviewTarget.webViewClient = PhoneValidateClient({
            Application.onToast(this, R.string.text_login_phone_validate_failed)
            setResult(RESULT_CANCELED)
            finish()
        }, { code ->
            setResult(RESULT_OK, Intent().also {
                it.putExtra(CODE, code)
            })
            finish()
        })
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

    private class PhoneValidateClient(
        private val onFailed: () -> Unit,
        private val onConfirm: (String) -> Unit,
    ): WebViewClient() {
        private val flag: AtomicBoolean = AtomicBoolean(false)

        override fun shouldOverrideUrlLoading(
            view: WebView, request: WebResourceRequest
        ): Boolean {
            if ("m.bilibili.com" == request.url.host && !flag.get()) {
                onFailed()
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
            log.debug(request.method.uppercase() + " " + request.url.toString())
            if (request.url.host == "passport.bilibili.com" &&
                request.url.path == "/web/sso/exchange_cookie" &&
                request.method.uppercase() == "GET") {
                val code = Pattern.compile("code=(.*?)&")
                    .matchString("${request.url}&", "code=&").let {
                        return@let it.substring(5, it.length - 1)
                    }
                flag.set(true)
                if (code.isBlank()) {
                    onFailed.invoke()
                } else {
                    onConfirm.invoke(code)
                }
                // code 只能用一次，所以这里直接构造一个假的响应返回避免 WebView 自行处理请求后把 code 用掉了
                return WebResourceResponse(
                    "application/json",
                    "UTF-8",
                    exchangeCookie.toGson().byteInputStream()
                )
            }
            return super.shouldInterceptRequest(view, request)
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