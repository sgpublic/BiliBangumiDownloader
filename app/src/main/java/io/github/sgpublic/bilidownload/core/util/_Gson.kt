package io.github.sgpublic.bilidownload.core.util

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type
import kotlin.reflect.KClass

private val GSON: Gson = GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .disableHtmlEscaping()
    .create()

fun <T: Any> KClass<T>.fromGson(src: String): T {
    return GSON.fromJson(src, this.java)
        ?: throw GsonException()
}

fun <T: Any> Class<T>.fromGson(src: String): T {
    return GSON.fromJson(src, this)
        ?: throw GsonException()
}

fun <T: Any> Type.fromGson(src: String): T {
    return GSON.fromJson(src, this)
        ?: throw GsonException()
}

fun Any?.toGson(): String {
    return GSON.toJson(this)
}

class GsonException(message: String? = null): Exception(message ?: "对象序列化失败")