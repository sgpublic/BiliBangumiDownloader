package io.github.sgpublic.bilidownload.core.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration

private val activities = ArrayList<Activity>()

fun Activity.register() {
    activities.add(this)
}

fun Activity.unregister() {
    activities.remove(this)
}

fun Context.finishAll() {
    for (activity in activities){
        if (!activity.isFinishing){
            activity.finish()
        }
    }
    activities.clear()
}

val Context.isNightMode: Boolean get() = resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES