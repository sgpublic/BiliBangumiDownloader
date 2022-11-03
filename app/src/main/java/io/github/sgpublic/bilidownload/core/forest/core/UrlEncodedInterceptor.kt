package io.github.sgpublic.bilidownload.core.forest.core

import com.dtflys.forest.http.ForestRequest
import com.dtflys.forest.http.ForestRequestType
import com.dtflys.forest.interceptor.Interceptor

class UrlEncodedInterceptor: Interceptor<Any> {
    override fun beforeExecute(request: ForestRequest<*>): Boolean {
        if (request.type == ForestRequestType.POST) {
            request.contentFormUrlEncoded()
        }
        request.addHeader("User-Agent", UserAgent)
        return true
    }

    companion object {
        const val UserAgent = "Mozilla/5.0 BiliDroid/7.1.1 (sgpublic2002@gmail.com)"
    }
}