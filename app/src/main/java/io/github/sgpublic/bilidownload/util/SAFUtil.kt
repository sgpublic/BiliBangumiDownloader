package io.github.sgpublic.bilidownload.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException

object SAFUtil {
    const val REQUEST_CODE_FOR_DIR = 233

    @JvmStatic
    fun getDocumentFile(context: Activity, path: String): DocumentFile? {
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A"
                + path.replace("/".toRegex(), "%2F"))
        return DocumentFile.fromTreeUri(context, uri)
    }

    @Throws(FileNotFoundException::class)
    fun getTreeUri(context: Activity, path: String): Uri {
        val file = getDocumentFile(context, path)
                ?: throw FileNotFoundException("目标目录不存在")
        return file.uri
    }

    @Throws(FileNotFoundException::class)
    fun checkSelfPermission(context: Activity, path: String): Boolean {
        val file = getDocumentFile(context, path)
                ?: throw FileNotFoundException("目标目录不存在")
        return file.canWrite() && file.canRead()
    }

    @Throws(FileNotFoundException::class)
    fun requestPermission(context: Activity, path: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getTreeUri(context, path))
        context.startActivityForResult(intent, REQUEST_CODE_FOR_DIR)
    }

    fun onActivityResult(context: Activity, requestCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_CODE_FOR_DIR) {
            return true
        }
        if (data == null) {
            return false
        }
        val treeUri = data.data
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(treeUri!!, takeFlags)
        context.getSharedPreferences("DirPermission", Context.MODE_PRIVATE).edit()
                .putString("treeUri", treeUri.toString())
                .apply()
        return true
    }
}