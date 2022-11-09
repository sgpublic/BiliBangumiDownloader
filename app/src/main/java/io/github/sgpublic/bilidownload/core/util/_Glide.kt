package io.github.sgpublic.bilidownload.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import io.github.sgpublic.bilidownload.R
import jp.wasabeef.glide.transformations.internal.FastBlur
import java.security.MessageDigest

private val option = RequestOptions()
    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
    .centerCrop()
fun RequestManager.customLoad(url: String): RequestBuilder<Drawable> {
    return load(url).apply(option)
}
fun RequestManager.customLoad(url: Uri): RequestBuilder<Drawable> {
    return load(url).apply(option)
}

fun RequestBuilder<Drawable>.constraintInfo(view: ImageView) {
    into(object : CustomTarget<Drawable>() {
        override fun onLoadFailed(errorDrawable: Drawable?) {

        }

        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            if (view.scaleType != ImageView.ScaleType.FIT_XY) {
                view.scaleType = ImageView.ScaleType.FIT_XY
            }
            val param = view.layoutParams as ConstraintLayout.LayoutParams
            param.dimensionRatio = "${resource.intrinsicWidth}:${resource.intrinsicHeight}"
            view.layoutParams = param
            view.setImageDrawable(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }
    })
}

fun RequestBuilder<Drawable>.withRound(radius: Int = 8.dp): RequestBuilder<Drawable> {
    return apply(RequestOptions.bitmapTransform(RoundedCorners(radius)))
}

fun RequestBuilder<Drawable>.withVerticalPlaceholder(): RequestBuilder<Drawable> {
    return withPlaceholder(R.drawable.pic_doing_v)
        .withError(R.drawable.pic_load_failed_v)
}

fun RequestBuilder<Drawable>.withHorizontalPlaceholder(): RequestBuilder<Drawable> {
    return withPlaceholder(R.drawable.pic_doing_h)
        .withError(R.drawable.pic_load_failed_h)
}

fun RequestBuilder<Drawable>.withPlaceholder(@DrawableRes placeholder: Int): RequestBuilder<Drawable> {
    return apply(RequestOptions.placeholderOf(placeholder))
}

fun RequestBuilder<Drawable>.withError(@DrawableRes err: Int): RequestBuilder<Drawable> {
    return apply(RequestOptions.errorOf(err))
}

fun RequestBuilder<Drawable>.withCrossFade(): RequestBuilder<Drawable> {
    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    return transition(DrawableTransitionOptions.withCrossFade(factory))
}

fun RequestBuilder<Drawable>.withBlur(radius: Int = 50): RequestBuilder<Drawable> {
    return apply(RequestOptions.bitmapTransform(BlurHelper(radius)))
}

class BlurHelper(private val radius: Int) : BitmapTransformation() {
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        var bitmap = pool[width, height, Bitmap.Config.ARGB_8888]
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        canvas.drawBitmap(toTransform, 0f, 0f, paint)
        bitmap = FastBlur.blur(bitmap, radius, true)
        return bitmap
    }

    override fun toString(): String {
        return "BlurTransformation(radius=$radius, sampling=1)"
    }

    override fun equals(other: Any?): Boolean {
        return other is BlurHelper && other.radius == radius
    }

    override fun hashCode(): Int {
        return ID.hashCode() + radius * 1000 + 10
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + radius).toByteArray(CHARSET))
    }

    companion object {
        private const val VERSION = 1
        private const val ID = "BlurTransformation.$VERSION"
    }
}
