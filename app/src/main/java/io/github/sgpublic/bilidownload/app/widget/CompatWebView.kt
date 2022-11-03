package io.github.sgpublic.bilidownload.app.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebSettings
import android.webkit.WebView

/**
 *
 * @author Madray Haven
 * @date 2022/11/3 13:30
 */
class CompatWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs) {
    init {
        settings.let { setting ->
            @SuppressLint("SetJavaScriptEnabled")
            setting.javaScriptEnabled = true
            setting.domStorageEnabled = true
            setting.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }
}