package io.github.sgpublic.bilidownload.core.forest.core

import com.dtflys.forest.http.ForestRequest
import com.dtflys.forest.http.ForestRequestType
import com.dtflys.forest.lifecycles.MethodAnnotationLifeCycle
import com.dtflys.forest.reflection.ForestMethod
import io.github.sgpublic.bilidownload.core.forest.ApiModule
import io.github.sgpublic.bilidownload.core.forest.annotations.BiliSign
import io.github.sgpublic.bilidownload.core.util.MD5_FULL
import io.github.sgpublic.bilidownload.core.util.encodeUrl
import java.util.*

class SignLifeCycle: MethodAnnotationLifeCycle<BiliSign, Any> {
    private lateinit var appKey: String
    private lateinit var appSecret: String
    private lateinit var mobiApp: String
    private lateinit var platform: String
    private lateinit var build: String
    override fun onMethodInitialized(method: ForestMethod<*>, annotation: BiliSign) {
        appKey = annotation.appKey
        appSecret = annotation.appSecret
        mobiApp = annotation.mobiApp
        platform = annotation.platform
        build = annotation.build
    }

    override fun beforeExecute(request: ForestRequest<*>): Boolean {
        val paramKeys = PriorityQueue<String>()
        val addField: (String, Any) -> Unit
        val field: MutableMap<String, Any>
        when (request.type) {
            ForestRequestType.GET -> {
                field = request.query
                addField = { key: String, value: Any ->
                    request.addQuery(key, value)
                    field[key] = value
                }
            }
            else -> {
                field = request.body.nameValuesMap()
                addField = { key: String, value: Any ->
                    request.addBody(key, value)
                    field[key] = value
                }
            }
        }

        addField("appkey", appKey)
        addField("mobi_app", mobiApp)
        addField("platform", platform)
        addField("ts", ApiModule.TS)
        addField("build", build)

        for ((key, _) in field) {
            paramKeys.add(key)
        }

        val sign = StringJoiner("&")
        while (paramKeys.isNotEmpty()) {
            val key = paramKeys.poll()!!
            sign.add("$key=${field[key].toString().encodeUrl()}")
        }

        val body = sign.toString() + appSecret
        addField("sign", body.MD5_FULL)

        return true
    }
}