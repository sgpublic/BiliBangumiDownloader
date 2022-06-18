package io.github.sgpublic.bilidownload.core.manager

import java.io.IOException
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL

class DownloadTaskManager {
    companion object {
        private const val STATUS_WAITING = 1 shl 5
        private const val USER_AGENT = "Bilibili Freedoooooom/MarkII"

        fun getSizeLong(url_string: String): Long {
            return try {
                val url = URL(url_string)
                val urlCon = url.openConnection() as HttpURLConnection
                urlCon.setRequestProperty("accept", "*/*")
                urlCon.setRequestProperty("connection", "Keep-Alive")
                urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                urlCon.setRequestProperty("User-Agent", USER_AGENT)
                val size = urlCon.contentLength.toLong()
                urlCon.disconnect()
                size
            } catch (e: IOException) { 0 }
        }

        fun getSizeString(url_string: String): String {
            val size = getSizeLong(url_string)
            val sizeString: String
            val fileSize = BigDecimal(size)
            val megabyte = BigDecimal(1024 * 1024)
            var returnValue = fileSize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                    .toFloat()
            if (returnValue > 1) {
                sizeString = "$returnValue MB"
            } else {
                val kilobyte = BigDecimal(1024)
                returnValue = fileSize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                        .toFloat()
                sizeString = "$returnValue KB"
            }
            return sizeString
        }
    }
}