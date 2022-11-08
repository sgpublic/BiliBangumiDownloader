package io.github.sgpublic.bilidownload.core.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import io.github.sgpublic.bilidownload.Application

private val contexts = LinkedHashSet<Context>()

fun AppCompatActivity.register() {
    (this as Context).register()
}

fun AppCompatActivity.unregister() {
    (this as Context).unregister()
}

fun Context.register() {
    contexts.add(this)
}

fun Context.unregister() {
    contexts.remove(this)
    if (contexts.isEmpty()) {
        Application.onTerminate()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}

fun Application.Companion.finishAll() {
    val tmp = ArrayList(contexts)
    for (context in tmp){
        if (context !is AppCompatActivity) {
            continue
        }
        if (context.lifecycle.currentState != Lifecycle.State.DESTROYED){
            context.finish()
        }
    }
    tmp.clear()
}

val Context.isNightMode: Boolean get() = resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES