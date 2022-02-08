package io.github.sgpublic.bilidownload.base

import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.manager.ConfigManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

object CrashHandler {
    fun saveExplosion(e: Throwable?, code: Int): String? {
        try {
            e ?: return null
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
            for (element_index in elements) {
                val crashStackTraceIndex = JSONObject()
                crashStackTraceIndex.put("class", element_index.className)
                crashStackTraceIndex.put("line", element_index.lineNumber)
                crashStackTraceIndex.put("method", element_index.methodName)
                crashStackTrace.put(crashStackTraceIndex)
            }
            val configString = StringBuilder(e.toString())
            for (config_index in 0..2) {
                configString.append("\nat ").append(elements[config_index].toString())
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
            val logs = crashMsgJson.toString()
            fileOutputStream.write(logs.toByteArray())
            fileOutputStream.close()
            return logs
        } catch (ignore: Exception) { }
        return null
    }
}