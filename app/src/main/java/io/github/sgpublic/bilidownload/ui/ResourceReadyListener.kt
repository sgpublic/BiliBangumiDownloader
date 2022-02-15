package io.github.sgpublic.bilidownload.ui

import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun <T> RequestBuilder<T>.addOnReadyListener(listener: () -> Unit): RequestBuilder<T> {
    return addListener(object : RequestListener<T> {
        override fun onLoadFailed(
            e: GlideException?, model: Any, target: Target<T>,
            isFirstResource: Boolean
        ): Boolean {
            return false
        }

        override fun onResourceReady(
            resource: T, model: Any, target: Target<T>,
            dataSource: DataSource, isFirstResource: Boolean
        ): Boolean {
            listener()
            return false
        }
    })
}