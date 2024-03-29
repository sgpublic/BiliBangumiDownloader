package io.github.sgpublic.bilidownload.core.forest

import io.github.sgpublic.bilidownload.core.forest.annotations.ModuleStyle
import io.github.sgpublic.bilidownload.core.forest.data.common.Modules
import io.github.sgpublic.bilidownload.core.util.GsonException
import io.github.sgpublic.bilidownload.core.util.fromGson
import io.github.sgpublic.bilidownload.core.util.log
import java.util.*

object ApiModule {
    val TS: Long get() = System.currentTimeMillis() / 1000
    val TS_FULL: Long get() = System.currentTimeMillis()
}

inline fun <reified T: Any> Modules.find(): List<T> {
    val moduleStyle = T::class.java.getAnnotation(ModuleStyle::class.java)
        ?: throw GsonException("To invoke this function, you must add annotation ApiStyle to target type!")
    val result = LinkedList<T>()
    for (element in modules) {
        val obj = element.asJsonObject
        if (!obj.has("style") || obj.get("style").asString == moduleStyle.value) {
            result.add(T::class.java.fromGson(obj.toString()))
        }
    }
    if (result.isEmpty()) {
        log.debug("${T::class.java} find() empty")
    }
    return result
}

