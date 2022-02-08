package io.github.sgpublic.bilidownload.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.IntRange
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*

object QRCodeUtil {
    fun createQRCodeBitmap(
        content: String,
        @IntRange(from = 1) width: Int,
        @IntRange(from = 1) height: Int,
    ): Bitmap {
        val hints: Hashtable<EncodeHintType, String> = Hashtable()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.ERROR_CORRECTION] = "H"
        hints[EncodeHintType.MARGIN] = "2"
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)

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
}