package io.github.sgpublic.bilidownload.core.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.sgpublic.bilidownload.Application
import okhttp3.internal.closeQuietly
import java.io.IOException
import java.nio.charset.Charset

class DocumentFileX internal constructor(private val doc: DocumentFile) {
    val name: String? get() = doc.name
    val parentFile: DocumentFileX? get() {
        val file = doc.parentFile
        return file?.let { DocumentFileX(it) }
    }
    val isDirectory get() = doc.isDirectory
    val isFile get() = doc.isFile
    val uri: Uri get() = doc.uri
    val path: String get() = doc.uri.path ?: "(invalid)"
    val canRead: Boolean get() = doc.canRead()
    val canWrite: Boolean get() = doc.canWrite()
    val accessibility: Boolean get() = doc.canRead() && doc.canWrite()

    fun readText(charset: Charset = Charsets.UTF_8): String {
        val input = Application.CONTENT_RESOLVER.openInputStream(uri)
            ?: throw IOException("Cannot open file -> $uri")
        val content: String
        input.reader(charset).let {
            content = it.readText()
            it.closeQuietly()
        }
        input.closeQuietly()
        return content
    }

    fun write(content: String, charset: Charset = Charsets.UTF_8) {
        val output = Application.CONTENT_RESOLVER.openOutputStream(uri, "wa")
            ?: throw IOException("Cannot open file -> $uri")
        output.writer(charset).let {
            it.write(content)
            it.closeQuietly()
        }
        output.closeQuietly()
    }

    fun createDirectory(displayName: String): DocumentFileX {
        val exist = findFile(displayName)
        if (exist != null) {
            if (doc.isDirectory) {
                return exist
            }
            throw IOException("A file with the same name already exists -> ${exist.uri.path}")
        }
        val newDir = doc.createDirectory(displayName)
            ?: throw IOException("Cannot create directory \"$displayName\" -> $path")
        return DocumentFileX(newDir)
    }

    fun createFile(displayName: String, mimeType: String = "*/*"): DocumentFileX {
        val exist = findFile(displayName)
        if (exist != null) {
            if (!doc.isDirectory) {
                return exist
            }
            doc.listFiles().forEach {
                LogCat.d(it.uri.path ?: return@forEach)
            }
            throw IOException("A directory with the same name already exists -> ${exist.uri.path}")
        }
        val newFile = doc.createFile(mimeType, displayName)
            ?: throw IOException("Cannot create file \"$displayName\" -> $path")
        return DocumentFileX(newFile)
    }

    fun delete(): Boolean {
        if (!canWrite) {
            throw IllegalAccessException("Cannot write current file -> $path")
        }
        return doc.delete()
    }

    fun listFiles(): ArrayList<DocumentFileX> {
        if (!canRead) {
            throw IllegalAccessException("Cannot read current file -> $path")
        }
        if (!isDirectory) {
            throw IOException("Current file is not a directory!")
        }
        val files = ArrayList<DocumentFileX>()
        for (file in doc.listFiles()) {
            files.add(DocumentFileX(file))
        }
        return files
    }

    fun findFile(displayName: String): DocumentFileX? {
        if (!canRead) {
            throw IllegalAccessException("Cannot read current file -> $path")
        }
        if (!isDirectory) {
            throw IOException("Current file is not a directory!")
        }
        val file = doc.findFile(displayName)
        return file?.let { DocumentFileX(it) }
    }

    fun renameTo(displayName: String): Boolean {
        if (!canWrite) {
            throw IllegalAccessException("Cannot write current file -> $path")
        }
        return doc.renameTo(displayName)
    }

    fun length(): Long = doc.length()

    companion object {
        fun fromSingleUri(context: Context, uri: Uri): DocumentFileX {
            return DocumentFileX(DocumentFile.fromSingleUri(context, uri)!!)
        }
        fun fromTreeUri(context: Context, uri: Uri): DocumentFileX {
            return DocumentFileX(DocumentFile.fromTreeUri(context, uri)!!)
        }
    }
}

fun Context.checkSafPermission(uri: Uri): Boolean {
    val file = DocumentFileX.fromTreeUri(this, uri)
    return file.canRead && file.canWrite
}