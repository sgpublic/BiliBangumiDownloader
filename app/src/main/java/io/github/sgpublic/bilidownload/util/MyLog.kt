package io.github.sgpublic.bilidownload.util

import android.util.Log
import io.github.sgpublic.bilidownload.BuildConfig

object MyLog {
    fun v(msg: Any, stack: Int = 0) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg, stack) { tag, message ->
            Log.v(tag, message)
        }
    }

    fun v(msg: Any, e: Throwable, stack: Int = 0) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg, e, stack) { tag, message ->
            Log.v(tag, message, e)
        }
    }

    fun d(msg: Any, stack: Int = 0) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg, stack) { tag, message ->
            Log.d(tag, message)
        }
    }

    fun d(msg: Any, e: Throwable, stack: Int = 0) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg, e, stack) { tag, message ->
            Log.d(tag, message, e)
        }
    }

    fun i(msg: Any, stack: Int = 0) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg, stack) { tag, message ->
            Log.i(tag, message)
        }
    }

    fun i(msg: Any, e: Throwable, stack: Int = 0) {
        doLog(msg, e, stack) { tag, message ->
            Log.i(tag, message, e)
        }
    }

    fun w(msg: Any, stack: Int = 0) {
        doLog(msg, stack) { tag, message ->
            Log.w(tag, message)
        }
    }

    fun w(msg: Any, e: Throwable, stack: Int = 0) {
        doLog(msg, e, stack) { tag, message ->
            Log.w(tag, message, e)
        }
    }

    fun e(msg: Any, stack: Int = 0) {
        doLog(msg, stack) { tag, message ->
            Log.e(tag, message)
        }
    }

    fun e(msg: Any, e: Throwable, stack: Int = 0) {
        doLog(msg, stack) { tag, message ->
            Log.e(tag, message, e)
        }
    }

    private const val maxLength = 2048
    private fun doLog(message: Any, stack: Int, log: (String, String) -> Unit) {
        val ste = Throwable().stackTrace[stack + 3]
        val tag = "MyLog (" + ste.fileName + ":" + ste.lineNumber + ")"
        val msgStr = message.toString()
        if (msgStr.length <= maxLength) {
            log(tag, msgStr)
            return
        }
        var index = 0
        while (index < msgStr.length - maxLength) {
            val out = msgStr.substring(index, index + maxLength)
            log(tag, out)
            index += maxLength
        }
        log(tag, msgStr.substring(index))
    }

    private fun doLog(message: Any, e: Throwable, stack: Int, log: (String, String) -> Unit) {
        doLog("$message，[" + e.javaClass.canonicalName + "] "
                + e.localizedMessage, stack + 1, log)
    }
}