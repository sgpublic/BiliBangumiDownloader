package io.github.sgpublic.bilidownload.util

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.github.sgpublic.bilidownload.Application

object IntentUtil {
    private fun startActivity(action: String = Intent.ACTION_VIEW, url: String? = null, extra: Bundle? = null) {
        Intent().apply {
            this.action = action
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            url?.let { this.data = Uri.parse(it) }
            extra?.let { this.putExtras(it) }
        }.let {
            Application.APPLICATION_CONTEXT.startActivity(it)
        }
    }

    fun openBrowser(url: String) {
        startActivity(url = url)
    }

    fun openBiliComic(id: Long) {
        startActivity(url = "bilicomic://detail/$id")
    }
}