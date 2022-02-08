package io.github.sgpublic.bilidownload.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import jp.wasabeef.glide.transformations.internal.FastBlur
import java.security.MessageDigest

//BlurHelper
class BlurHelper : BitmapTransformation() {
    private val radius = 21
    private val sampling = 1
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val scaledWidth = width / sampling
        val scaledHeight = height / sampling
        var bitmap = pool[scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888]
        val canvas = Canvas(bitmap)
        canvas.scale(1 / sampling.toFloat(), 1 / sampling.toFloat())
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        canvas.drawBitmap(toTransform, 0f, 0f, paint)
        bitmap = FastBlur.blur(bitmap, radius, true)
        return bitmap
    }

    override fun toString(): String {
        return "BlurTransformation(radius=$radius, sampling=$sampling)"
    }

    override fun equals(other: Any?): Boolean {
        return other is BlurHelper && other.radius == radius && other.sampling == sampling
    }

    override fun hashCode(): Int {
        return ID.hashCode() + radius * 1000 + sampling * 10
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + radius + sampling).toByteArray(CHARSET))
    }

    companion object {
        private const val VERSION = 1
        private const val ID = "BlurTransformation.$VERSION"
    }
}