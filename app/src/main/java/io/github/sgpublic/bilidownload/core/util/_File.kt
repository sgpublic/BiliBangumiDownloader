package io.github.sgpublic.bilidownload.core.util

import okhttp3.internal.closeQuietly
import java.io.File
import java.nio.charset.Charset

fun File.writeAndClose(content: String, charset: Charset = Charsets.UTF_8) {
    delete()
    parentFile?.mkdirs()
    createNewFile()
    writer(charset).also {
        it.write(content)
        it.closeQuietly()
    }
}