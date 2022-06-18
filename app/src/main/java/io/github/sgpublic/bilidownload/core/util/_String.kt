package io.github.sgpublic.bilidownload.core.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.IntRange
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.nio.charset.Charset
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