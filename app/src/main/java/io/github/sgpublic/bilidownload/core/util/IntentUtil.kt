package io.github.sgpublic.bilidownload.core.util

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.github.sgpublic.bilidownload.Application

object IntentUtil {
    private fun startActivity(
        url: String? = null, action: String = Intent.ACTION_VIEW,
        extra: Bundle? = null
    ) {
        Intent().apply {
            this.action = action
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            url?.let { this.data = Uri.parse(it) }
            extra?.let { this.putExtras(it) }
        }.let {
            Application.APPLICATION_CONTEXT.startActivity(it)
        }
    }

    fun openUrl(url: String) {
        startActivity(url)
    }

    fun openBiliComic(id: Long) {
        openUrl("bilicomic://detail/$id")
    }
}