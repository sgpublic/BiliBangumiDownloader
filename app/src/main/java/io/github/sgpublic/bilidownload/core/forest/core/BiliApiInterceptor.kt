package io.github.sgpublic.bilidownload.core.forest.core

import com.dtflys.forest.http.ForestRequest
import com.dtflys.forest.http.ForestResponse
import com.dtflys.forest.interceptor.Interceptor
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import io.github.sgpublic.bilidownload.core.util.fromGson

class BiliApiInterceptor: Interceptor<Any> {
    override fun onSuccess(data: Any, request: ForestRequest<*>, response: ForestResponse<*>) {
        val obj = try {
            JsonObject::class.fromGson(response.content)
        } catch (e: JsonSyntaxException) {
            return
        }
        if (!obj.has("code")) {
            return
        }
        val code = obj.get("code").asInt
        if (code == 0) {
            return
        }
        val message = when {
            obj.has("msg") -> obj.get("msg").asString
            obj.has("message") -> obj.get("message").asString
            else -> "Unknown error"
        }
        throw BiliApiException(code, message, obj)
    }
}