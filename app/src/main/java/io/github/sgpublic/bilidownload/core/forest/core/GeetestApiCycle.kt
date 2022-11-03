package io.github.sgpublic.bilidownload.core.forest.core

import com.dtflys.forest.http.ForestRequest
import com.dtflys.forest.http.ForestResponse
import com.dtflys.forest.interceptor.Interceptor
import com.dtflys.forest.lifecycles.MethodAnnotationLifeCycle
import com.dtflys.forest.reflection.ForestMethod
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import io.github.sgpublic.bilidownload.core.forest.annotations.GeetestApi
import io.github.sgpublic.bilidownload.core.util.fromGson
import io.github.sgpublic.bilidownload.core.util.take
import org.json.JSONObject

class GeetestApiCycle: MethodAnnotationLifeCycle<GeetestApi, Any> {
    override fun onMethodInitialized(method: ForestMethod<*>?, annotation: GeetestApi?) {

    }

    override fun onSuccess(data: Any, request: ForestRequest<*>, response: ForestResponse<*>) {
        val content = response.content
        if (content.startsWith("(") && content.endsWith(")")) {
            response.content = content.substring(1, content.length - 1)
        }
        val obj = try {
            JsonObject::class.java.fromGson(response.content)
        } catch (e: JsonSyntaxException) {
            return
        }
        if (obj.get("status").asString == "success") {
            return
        }
        val message = when {
            obj.has("user_error") -> obj.get("user_error").asString
            obj.has("error") -> obj.get("error").asString
            else -> "Unknown error"
        }
        throw GeetestApiException(message)
    }
}