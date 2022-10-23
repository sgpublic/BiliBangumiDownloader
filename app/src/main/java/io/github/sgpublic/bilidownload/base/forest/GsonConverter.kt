package io.github.sgpublic.bilidownload.base.forest

import com.dtflys.forest.converter.json.ForestJsonConverter
import com.dtflys.forest.exceptions.ForestConvertException
import com.dtflys.forest.utils.ForestDataType
import com.dtflys.forest.utils.StringUtils
import io.github.sgpublic.bilidownload.core.util.fromGson
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.toGson
import java.lang.reflect.Type
import java.nio.charset.Charset

/**
 *
 * @author Madray Haven
 * @date 2022/10/20 16:28
 */
object GsonConverter: ForestJsonConverter {
    /** 日期格式  */
    private var dateFormat: String? = null
    override fun getDateFormat(): String {
        return dateFormat!!
    }

    override fun setDateFormat(dateFormat: String) {
        this.dateFormat = dateFormat
    }

    override fun <T> convertToJavaObject(source: String, targetType: Type): T {
        try {
            return targetType.fromGson(source)
        } catch (e: Exception) {
            log.debug("targetType: $targetType, source: $source")
            throw ForestConvertException(this, e)
        }
    }

    override fun <T> convertToJavaObject(
        source: ByteArray,
        targetType: Class<T>,
        charset: Charset
    ): T {
        val str = StringUtils.fromBytes(source, charset)
        return convertToJavaObject(str, targetType)
    }

    override fun <T> convertToJavaObject(source: ByteArray, targetType: Type, charset: Charset): T? {
        val str = StringUtils.fromBytes(source, charset)
        return convertToJavaObject(str, targetType)
    }

    override fun encodeToString(obj: Any): String {
        return obj.toGson()
    }

    private val clazz = HashMap<String, Any>().javaClass
    override fun convertObjectToMap(obj: Any?): Map<String, Any>? {
        if (obj == null) {
            return null
        }
        if (obj is Map<*, *>) {
            val newMap: MutableMap<String, Any> = HashMap(obj.size)
            for (key in obj.keys) {
                val `val` = obj[key]
                if (`val` != null) {
                    newMap[key.toString()] = `val`
                }
            }
            return newMap
        }
        if (obj is CharSequence) {
            return convertToJavaObject(obj.toString(), LinkedHashMap<String, Any>().javaClass)
        }
        return clazz.fromGson(obj.toGson())
    }

    override fun getDataType(): ForestDataType {
        return ForestDataType.JSON
    }
}
