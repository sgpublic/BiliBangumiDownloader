package io.github.sgpublic.bilidownload.core.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Response
import okhttp3.internal.closeQuietly
import kotlin.reflect.KClass

private val GSON: Gson = GsonBuilder().disableHtmlEscaping().create()

fun <T: Any> KClass<T>.fromGson(src: String): T {
    return GSON.fromJson(src, this.java)
        ?: throw GsonException()
}

fun <T: Any> Class<T>.fromGson(src: String): T {
    return GSON.fromJson(src, this)
        ?: throw GsonException()
}

fun Any?.toGson(): String {
    return GSON.toJson(this)
}

fun <T: Any> Response.jsonBody(clazz: KClass<T>): T {
    return clazz.fromGson(textBody())
}

fun Response.textBody(): String {
    val body = this.body.string()
    this.body.closeQuietly()
    return body
}

class GsonException: Exception("对象序列化失败")