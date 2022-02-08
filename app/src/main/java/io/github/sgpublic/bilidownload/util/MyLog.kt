package io.github.sgpublic.bilidownload.util

import android.util.Log
import io.github.sgpublic.bilidownload.BuildConfig

object MyLog {
    fun v(msg: Any) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg) { tag, message ->
            Log.v(tag, message)
        }
    }

    fun v(msg: Any, e: Throwable) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg, e) { tag, message ->
            Log.v(tag, message, e)
        }
    }

    fun d(msg: Any) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg) { tag, message ->
            Log.w(tag, message)
        }
    }

    fun d(msg: Any, e: Throwable) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg, e) { tag, message ->
            Log.d(tag, message, e)
        }
    }

    fun i(msg: Any) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg) { tag, message ->
            Log.i(tag, message)
        }
    }

    fun i(msg: Any, e: Throwable) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg, e) { tag, message ->
            Log.i(tag, message, e)
        }
    }

    fun w(msg: Any) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg) { tag, message ->
            Log.w(tag, message)
        }
    }

    fun w(msg: Any, e: Throwable) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg, e) { tag, message ->
            Log.w(tag, message, e)
        }
    }

    fun e(msg: Any) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg) { tag, message ->
            Log.e(tag, message)
        }
    }

    fun e(msg: Any, e: Throwable) {
        if (!BuildConfig.DEBUG) {
            return
        }
        doLog(msg) { tag, message ->
            Log.e(tag, message, e)
        }
    }

    private fun doLog(message: Any, stack: Int = 3, log: (String, String) -> Unit) {
        val ste = Throwable().stackTrace[stack]
        val tag = "MyLog (" + ste.fileName + ":" + ste.lineNumber + ")"
        val msgStr = message.toString()
        if (msgStr.length <= 1024) {
            log(tag, msgStr)
            return
        }
        var index = 0
        while (index < msgStr.length - 1024) {
            val out = msgStr.substring(index, index + 1024)
            log(tag, out)
            index += 1024
        }
        log(tag, msgStr.substring(index))
    }

    private fun doLog(message: Any, e: Throwable, log: (String, String) -> Unit) {
        doLog("$message，[" + e.javaClass.canonicalName + "] "
                + e.localizedMessage, 4, log)
    }
}