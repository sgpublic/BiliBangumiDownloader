package io.github.sgpublic.bilidownload.core.util

import android.app.Activity
import android.content.Context

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