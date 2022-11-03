package io.github.sgpublic.bilidownload.core.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import io.github.sgpublic.bilidownload.Application

private val activities = LinkedHashSet<AppCompatActivity>()
private val contexts = LinkedHashSet<Context>()

fun AppCompatActivity.register() {
    activities.add(this)
    (this as Context).register()
}

fun AppCompatActivity.unregister() {
    activities.remove(this)
    (this as Context).unregister()
}

fun Context.register() {
    contexts.add(this)
}

fun Context.unregister() {
    contexts.remove(this)
    if (activities.isEmpty()) {
        Application.onTerminate()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}

@Suppress("unused")
fun Application.Companion.finishAll() {
    val tmp = ArrayList(activities)
    for (activity in tmp){
        if (activity.lifecycle.currentState != Lifecycle.State.DESTROYED){
            activity.finish()
        }
    }
    tmp.clear()
}

val Context.isNightMode: Boolean get() = resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES