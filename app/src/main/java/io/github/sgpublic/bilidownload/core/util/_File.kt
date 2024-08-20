package io.github.sgpublic.bilidownload.core.util

import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.nio.charset.Charset

fun File.writeAndClose(content: String, charset: Charset = Charsets.UTF_8) {
    delete()
    parentFile?.mkdirs()
    createNewFile()
    writer(charset).use {
        it.write(content)
    }
}

fun File.writeAndClose(content: ByteArray, charset: Charset = Charsets.UTF_8) {
    delete()
    parentFile?.mkdirs()
    createNewFile()
    writeBytes(content)
}

fun File.child(name: String) = File(this, name)
