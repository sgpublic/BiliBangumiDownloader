package io.github.sgpublic.bilidownload.core.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.IntRange
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.Serializable
import java.math.BigInteger
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import java.util.regex.Pattern

private val pattern = Pattern.compile("[\\u4E00-\\u9FA5]+")
private val GB2312 = Charset.forName("GB2312")

fun String.isChinese(): Boolean {
    return pattern.matcher(this).find()
}

fun String.isSimplifyChinese(): Boolean {
    return isChinese() && String(toByteArray(GB2312)) == this
}

fun String.createQRCodeBitmap(
    @IntRange(from = 1) width: Int,
    @IntRange(from = 1) height: Int,
): Bitmap {
    val hints: Hashtable<EncodeHintType, String> = Hashtable()
    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
    hints[EncodeHintType.ERROR_CORRECTION] = "H"
    hints[EncodeHintType.MARGIN] = "2"
    val bitMatrix = QRCodeWriter().encode(
        this, BarcodeFormat.QR_CODE,
        width, height, hints
    )

    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (bitMatrix[x, y]) {
                pixels[y * width + x] = Color.BLACK
            } else {
                pixels[y * width + x] = Color.WHITE
            }
        }
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}


val ByteArray.BASE_64: String get() {
    return Base64.getEncoder().encodeToString(this)
}

val String.ORIGIN_BASE64: ByteArray get() {
    return Base64.getDecoder().decode(this)
}

private val instance: MessageDigest get() = MessageDigest.getInstance("MD5")

/**
 * 16 位 MD5
 */
val String.MD5: String get() {
    return MD5_FULL.substring(5, 24)
}

/**
 * 32 位 MD5
 */
val String.MD5_FULL: String get() {
    val digest = instance.digest(toByteArray())
    return StringBuffer().run {
        for (b in digest) {
            val i :Int = b.toInt() and 0xff
            var hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                hexString = "0$hexString"
            }
            append(hexString)
        }
        toString()
    }
}

/**
 * 8 位 MD5，由 16 位 MD5 转换为 32 进制得来
 */
val String.MD5_COMPRESSED: String get() {
    return BigInteger(MD5, 16).toString(32)
}

/**
 * 8 位 MD5，由 16 位 MD5 转换为 32 进制得来
 */
val String.MD5_FULL_COMPRESSED: String get() {
    return BigInteger(MD5_FULL, 16).toString(32)
}

/**
 * 16 位 MD5
 */
val Serializable.MD5: String get() {
    return MD5_FULL.substring(5, 24)
}

/**
 * 32 位 MD5
 */
val Serializable.MD5_FULL: String get() {
    return toGson().MD5_FULL
}

/**
 * 8 位 MD5，由 16 位 MD5 转换为 32 进制得来
 */
val Serializable.MD5_COMPRESSED: String get() {
    return BigInteger(MD5, 16).toString(32)
}

/**
 * 8 位 MD5，由 16 位 MD5 转换为 32 进制得来
 */
val Serializable.MD5_FULL_COMPRESSED: String get() {
    return BigInteger(MD5_FULL, 16).toString(32)
}

fun String.encodeUrl(): String {
    return URLEncoder.encode(this, "UTF-8")
}
fun String.decodeUrl(): String {
    return URLDecoder.decode(this, "UTF-8")
}

fun Pattern.matchString(target: CharSequence, def: String): String {
    val matcher = matcher(target)
    if (!matcher.find()) {
        return def
    }
    return matcher.group();
}
