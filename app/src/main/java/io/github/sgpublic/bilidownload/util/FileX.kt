package io.github.sgpublic.bilidownload.util

import android.app.Activity
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException

class FileX {
    private val context: Activity
    private val file: DocumentFile

    constructor(context: Activity, path: String) {
        this.context = context
        val file = SAFUtil.getDocumentFile(context, path)
                ?: throw FileNotFoundException("目标文件不存在")
        this.file = file
    }

    private constructor(context: Activity, file: DocumentFile) {
        this.context = context
        this.file = file
    }

    fun createDirectory(displayName: String): FileX? {
        var file = file.findFile(displayName)
        if (file != null) {
            return if (file.isDirectory) {
                FileX(context, file)
            } else {
                null
            }
        }
        file = this.file.createDirectory(displayName)
        return file?.let { FileX(context, it) }
    }

    fun createFile(displayName: String): FileX? {
        var file = file.findFile(displayName)
        if (file != null) {
            return if (file.isFile) {
                FileX(context, file)
            } else {
                null
            }
        }
        file = this.file.createFile("*/*", displayName)
        return file?.let { FileX(context, it) }
    }

    val name: String?
        get() = file.name
    val parentFile: FileX?
        get() {
            val file = file.parentFile
            return file?.let { FileX(context, it) }
        }

    fun isDirectory(): Boolean {
        return file.isDirectory
    }

    fun isFile(): Boolean {
        return file.isFile
    }

    fun delete(): Boolean {
        return file.delete()
    }

    fun listFiles(): ArrayList<FileX>? {
        if (!file.isDirectory) {
            return null
        }
        val files = ArrayList<FileX>()
        for (file in file.listFiles()) {
            files.add(FileX(context, file))
        }
        return files
    }

    fun findFile(displayName: String): FileX? {
        val file = file.findFile(displayName)
        return file?.let { FileX(context, it) }
    }

    fun renameTo(displayName: String): Boolean {
        return file.renameTo(displayName)
    }

    val uri: Uri get() = file.uri
}