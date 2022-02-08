package io.github.sgpublic.bilidownload

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import com.kongzue.dialog.util.DialogSettings
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.util.MyLog
import java.lang.ref.WeakReference

@Suppress("unused")
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        MyLog.v("APP启动")
        application = WeakReference(this)
        startListenException()
        DialogSettings.style = DialogSettings.STYLE.STYLE_MIUI
        DialogSettings.theme = DialogSettings.THEME.LIGHT
    }

    private fun startListenException() {
        Handler(mainLooper).post {
            while (true){
                try {
                    Looper.loop()
                } catch (e: Throwable){
                    MyLog.e("应用意外停止", e)
                    CrashHandler.saveExplosion(e, -100)
                    break
                }
            }
        }
    }

    override fun onTerminate() {
        application.clear()
        super.onTerminate()
    }

    companion object {
        private lateinit var application: WeakReference<Application>

        val APPLICATION: Application get() = application.get()!!
        val APPLICATION_CONTEXT: Context get() = application.get()!!.applicationContext
        val CONTENT_RESOLVER: ContentResolver get() = APPLICATION_CONTEXT.contentResolver

        fun dip2px(dip: Float): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                APPLICATION_CONTEXT.resources.displayMetrics).toInt()
        }

        fun sp2px(dip: Float): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dip,
                APPLICATION_CONTEXT.resources.displayMetrics).toInt()
        }
    }
}