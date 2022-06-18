package io.github.sgpublic.bilidownload.base

import io.github.sgpublic.bilidownload.BuildConfig
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.util.LogCat
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

object CrashHandler {
    fun saveExplosion(e: Throwable?, code: Int, message: String? = "save exception"): String? {
        try {
            e ?: return null
            LogCat.w(message ?: e.localizedMessage, e, 2)
            val exceptionLog: JSONObject
            var exceptionLogContent = JSONArray()
            val logPath: String = Application.APPLICATION_CONTEXT
                .getExternalFilesDir("log")?.path
                ?: throw IllegalStateException()
            val exception = File(logPath, "exception.json")
            var logContent: String
            try {
                val fileInputStream = FileInputStream(exception)
                val bufferedReader = BufferedReader(InputStreamReader(fileInputStream))
                var line: String?
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                logContent = stringBuilder.toString()
            } catch (e1: IOException) {
                logContent = ""
            }
            if (logContent != "") {
                exceptionLog = JSONObject(logContent)
                if (!exceptionLog.isNull("logs")) {
                    exceptionLogContent = exceptionLog.getJSONArray("logs")
                }
            }
            val elements = e.stackTrace
            val crashMsgJson = JSONObject()
            val crashMsgArray = JSONArray()
            val crashMsgArrayIndex = JSONObject()
            val crashStackTrace = JSONArray()
            for (elementIndex in elements) {
                if (!elementIndex.className.startsWith(BuildConfig.APPLICATION_ID)) {
                    continue
                }
                val crashStackTraceIndex = JSONObject()
                crashStackTraceIndex.put("class", elementIndex.className)
                crashStackTraceIndex.put("line", elementIndex.lineNumber)
                crashStackTraceIndex.put("method", elementIndex.methodName)
                crashStackTrace.put(crashStackTraceIndex)
            }
            val configString = StringBuilder(e.toString())
            for (configIndex in 0..2) {
                configString.append("\nat ").append(elements[configIndex].toString())
            }
            ConfigManager.LAST_EXCEPTION = configString.toString()
            crashMsgArrayIndex.put("code", code)
            crashMsgArrayIndex.put("message", e.toString())
            crashMsgArrayIndex.put("stack_trace", crashStackTrace)
            crashMsgArray.put(crashMsgArrayIndex)
            var exceptionLogIndex = 0
            while (exceptionLogIndex < exceptionLogContent.length() && exceptionLogIndex < 2) {
                val msgIndex = exceptionLogContent.getJSONObject(exceptionLogIndex)
                if (crashMsgArrayIndex.toString() != msgIndex.toString()) {
                    crashMsgArray.put(msgIndex)
                }
                exceptionLogIndex++
            }
            crashMsgJson.put("logs", crashMsgArray)
            val fileOutputStream = FileOutputStream(exception)
            fileOutputStream.write(crashMsgJson.toString().toByteArray())
            fileOutputStream.close()
            return crashMsgArray.toString()
        } catch (_: Exception) { }
        return null
    }
}