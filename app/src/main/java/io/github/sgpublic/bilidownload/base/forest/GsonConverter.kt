package io.github.sgpublic.bilidownload.base.forest

import com.dtflys.forest.converter.json.ForestJsonConverter
import com.dtflys.forest.exceptions.ForestConvertException
import com.dtflys.forest.utils.ForestDataType
import com.dtflys.forest.utils.ReflectUtils
import com.dtflys.forest.utils.StringUtils
import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.math.BigDecimal
import java.nio.charset.Charset

/**
 *
 * @author Madray Haven
 * @date 2022/10/20 16:28
 */


/**
 * 使用Gson实现的消息转换实现类
 *
 * @author Gongjun
 * @since 2016-06-04
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

    override fun <T> convertToJavaObject(source: String, targetType: Type): T? {
        return if (StringUtils.isBlank(source)) {
            null
        } else try {
            if (targetType is ParameterizedType
                || targetType.javaClass.name.startsWith("com.google.gson")
            ) {
                val gson = createGson()
                return gson.fromJson(source, targetType)
            }
            val clazz = ReflectUtils.toClass(targetType)
            try {
                if (MutableMap::class.java.isAssignableFrom(clazz)) {
                    val jsonObject = JsonParser.parseString(source).asJsonObject
                    return toMap(jsonObject, false) as T
                } else if (MutableList::class.java.isAssignableFrom(clazz)) {
                    val jsonArray = JsonParser.parseString(source).asJsonArray
                    return toList(jsonArray) as T
                }
                val gson = createGson()
                gson.fromJson<Any>(source, targetType) as T
            } catch (th: Throwable) {
                throw ForestConvertException(this, th)
            }
        } catch (ex: Exception) {
            throw ForestConvertException(this, ex)
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

    /**
     * 创建GSON对象
     * @return New instance of `com.google.gson.Gson`
     */
    private fun createGson(): Gson {
        val gsonBuilder = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .disableHtmlEscaping()
        if (StringUtils.isNotBlank(dateFormat)) {
            gsonBuilder.setDateFormat(dateFormat)
        }
        return gsonBuilder.create()
    }

    override fun encodeToString(obj: Any): String {
        val gson = createGson()
        return gson.toJson(obj)
    }

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
        val gson = createGson()
        val jsonElement = gson.toJsonTree(obj)
        return toMap(jsonElement.asJsonObject, true)
    }

    fun convertToJson(obj: Any?, type: Type?): String {
        val gson = createGson()
        return gson.toJson(obj, type)
    }

    override fun getDataType(): ForestDataType {
        return ForestDataType.JSON
    }

    private fun toMap(json: JsonObject, singleLevel: Boolean): Map<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        val entrySet = json.entrySet()
        val iter: Iterator<Map.Entry<String, JsonElement>> = entrySet.iterator()
        while (iter.hasNext()) {
            val (key, value) = iter.next()
            if (singleLevel) {
                when (value) {
                    is JsonArray -> {
                        map[key] = toList(value)
                    }
                    is JsonPrimitive -> {
                        map[key] = toObject(value)!!
                    }
                    else -> {
                        map[key] = value
                    }
                }
                continue
            }
            when (value) {
                is JsonArray -> {
                    map[key] = toList(value)
                }
                is JsonObject -> {
                    map[key] = toMap(value, false)
                }
                is JsonPrimitive -> {
                    map[key] = toObject(value)!!
                }
                else -> {
                    map[key] = value
                }
            }
        }
        return map
    }

    private fun toObject(jsonPrimitive: JsonPrimitive): Any? {
        if (jsonPrimitive.isBoolean) {
            return jsonPrimitive.asBoolean
        }
        if (jsonPrimitive.isString) {
            return jsonPrimitive.asString
        }
        if (jsonPrimitive.isNumber) {
            val num = jsonPrimitive.asBigDecimal
            val index = num.toString().indexOf('.')
            if (index == -1) {
                if (num.compareTo(BigDecimal(Long.MAX_VALUE)) == 1) {
                    return num
                }
                if (num.compareTo(BigDecimal(Long.MIN_VALUE)) == -1) {
                    return num
                }
                return if (num.compareTo(BigDecimal(Int.MAX_VALUE)) == 1
                    || num.compareTo(BigDecimal(Int.MIN_VALUE)) == -1
                ) {
                    jsonPrimitive.asLong
                } else jsonPrimitive.asInt
            }
            val dvalue = jsonPrimitive.asDouble
            val fvalue = jsonPrimitive.asFloat
            return if (dvalue.toString() == fvalue.toString()) {
                fvalue
            } else dvalue
        }
        if (jsonPrimitive.isJsonArray) {
            return toList(jsonPrimitive.asJsonArray)
        }
        return if (jsonPrimitive.isJsonObject) {
            toMap(jsonPrimitive.asJsonObject, false)
        } else null
    }

    private fun toList(json: JsonArray): List<Any?> {
        val list: MutableList<Any?> = ArrayList()
        for (i in 0 until json.size()) {
            when (val value: Any = json[i]) {
                is JsonArray -> {
                    list.add(toList(value))
                }
                is JsonObject -> {
                    list.add(toMap(value, false))
                }
                is JsonPrimitive -> {
                    list.add(toObject(value))
                }
                else -> {
                    list.add(value)
                }
            }
        }
        return list
    }
}
