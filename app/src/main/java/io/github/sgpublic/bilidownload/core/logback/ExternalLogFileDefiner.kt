package io.github.sgpublic.bilidownload.core.logback

import ch.qos.logback.core.PropertyDefinerBase
import io.github.sgpublic.bilidownload.Application
import java.io.File
import java.io.IOException

class ExternalLogFileDefiner: PropertyDefinerBase() {
    override fun getPropertyValue(): String {
        val logDir = File(Application.ApplicationContext.externalCacheDir, "log")
        if (!logDir.exists() && !logDir.mkdirs()) {
            throw RuntimeException("日志目录创建失败")
        }
        return try {
            logDir.canonicalPath
        } catch (e: IOException) {
            logDir.path
        }
    }
}