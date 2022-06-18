package io.github.sgpublic.bilidownload.core.util

import android.content.Context
import android.os.Handler

class TimerCompat(private val context: Context) {
    private var init = false
    private var doing = false
    private var lock = Object()

    fun schedule(delay: Long, period: Long, unit: Runnable) {
        val runnable = object : Runnable {
            override fun run() {
                if (!doing) return
                unit.run()
                Handler(context.mainLooper).postDelayed(this, delay)
            }
        }
        synchronized(lock) {
            if (init) return
            init = true; doing = true
            Handler(context.mainLooper).postDelayed(runnable, period)
        }
    }

    fun cancel() {
        synchronized(lock) {
            doing = false
        }
    }
}