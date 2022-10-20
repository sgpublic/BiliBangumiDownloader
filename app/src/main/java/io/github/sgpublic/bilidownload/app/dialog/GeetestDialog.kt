package io.github.sgpublic.bilidownload.app.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.JsonObject
import com.lxj.xpopup.core.CenterPopupView
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.util.dp
import io.github.sgpublic.bilidownload.core.util.fromGson
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.takeOr
import io.github.sgpublic.bilidownload.databinding.DialogWebviewBinding
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 *
 * @author Madray Haven
 * @date 2022/10/20 14:11
 */
@SuppressLint("ViewConstructor")
class GeetestDialog(
    context: Context,
    private val url: String,
    private val onCancel: () -> Unit,
    private val onConfirm: (String) -> Unit,
): CenterPopupView(context) {
    override fun onShow() {
        WebView.webViewClient = Interceptor {
            smartDismiss()
            onConfirm.invoke(it)
        }
        WebView.loadUrl(url)
    }

    override fun onBackPressed(): Boolean {
        onCancel.invoke()
        return super.onBackPressed()
    }

    class Interceptor(
        private val onConfirm: (String) -> Unit
    ) : WebViewClient() {
        private val client: OkHttpClient = OkHttpClient.Builder()
            .build()

        override fun shouldInterceptRequest(
            view: WebView, request: WebResourceRequest
        ): WebResourceResponse? {
            if (request.url.host == "api.geetest.com" &&
                request.url.path == "/ajax.php" &&
                request.method.uppercase() == "GET") {
                val resp = Request.Builder().let {
                    it.url(request.url.toString())
                    for ((key, value) in request.requestHeaders) {
                        it.addHeader(key, value)
                    }
                    client.newCall(it.build()).execute()
                }

                resp.body?.run {
                    val body = string()
                    val obj = JsonObject::class.java.fromGson(body.let {
                        return@let it.substring(it.indexOf("(") + 1, it.indexOf(")"))
                    })
                    if (obj.has("validate")) {
                        onConfirm.invoke(obj.get("validate").asString)
                    }
                    return WebResourceResponse(
                        contentType()?.type.takeOr(""),
                        (contentType()?.charset() ?: Charsets.UTF_8).name(),
                        resp.code, resp.message.takeOr("OK"),
                        resp.headers.toMap(), body.byteInputStream()
                    )
                }
            }
            return super.shouldInterceptRequest(view, request)
        }
    }

    private val WebView: WebView by lazy {
        return@lazy DialogWebviewBinding.bind(contentView).root
    }
    override fun getImplLayoutId(): Int = R.layout.dialog_webview
    override fun getPopupWidth(): Int = 300.dp
    override fun getPopupHeight(): Int = 300.dp

    override fun onDestroy() {
        WebView.destroy()
        super.onDestroy()
    }
}