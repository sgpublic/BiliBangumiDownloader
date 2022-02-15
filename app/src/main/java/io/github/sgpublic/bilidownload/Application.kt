package io.github.sgpublic.bilidownload

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.room.AppDatabase
import io.github.sgpublic.bilidownload.util.ActivityCollector
import io.github.sgpublic.bilidownload.util.MyLog
import java.lang.ref.WeakReference

@Suppress("unused")
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        MyLog.i("APP启动")
        application = WeakReference(this)
        room = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "bilidl.db")
            .allowMainThreadQueries()
            .build()

        startListenException()
    }

    private fun startListenException() {
        Handler(mainLooper).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable){
                    CrashHandler.saveExplosion(e, -100, "应用意外停止")
                    if (!BuildConfig.DEBUG) {
                        ActivityCollector.finishAll()
                        break
                    }
                }
            }
        }
    }

    private fun showExceptionDialog(exc: String?) {
        // TODO 应用意外退出弹窗
        if (exc == null) {
            ActivityCollector.finishAll()
            return
        }
        XPopup.Builder(APPLICATION_CONTEXT).asConfirm(
            getString(R.string.title_function_crash),
            getString(R.string.text_function_crash),
            getString(R.string.text_function_crash_exit),
            getString(R.string.text_function_crash_copy), {
                val cs = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cs.setPrimaryClip(ClipData.newPlainText("exception", exc))
                ActivityCollector.finishAll()
            }, {
                ActivityCollector.finishAll()
            }, false
        ).show()
    }

    override fun onTerminate() {
        MyLog.i("APP结束")
        application?.clear()
        application = null
        if (DATABASE.isOpen) {
            DATABASE.close()
        }
        room = null
        super.onTerminate()
    }

    companion object {
        private var application: WeakReference<Application>? = null
        @Volatile
        private var room: AppDatabase? = null

        val APPLICATION: Application get() = application?.get() ?: throw NullPointerException()
        val APPLICATION_CONTEXT: Context get() = APPLICATION.applicationContext
        val CONTENT_RESOLVER: ContentResolver get() = APPLICATION_CONTEXT.contentResolver
        val DATABASE: AppDatabase get() = room ?: throw NullPointerException()

        fun dip2px(dip: Float): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                APPLICATION_CONTEXT.resources.displayMetrics).toInt()
        }

        fun sp2px(sp: Float): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                APPLICATION_CONTEXT.resources.displayMetrics).toInt()
        }

        fun onToast(context: AppCompatActivity, content: String?) {
            context.runOnUiThread {
                Toast.makeText(APPLICATION_CONTEXT, content, Toast.LENGTH_SHORT).show()
            }
        }
        fun onToast(context: AppCompatActivity, @StringRes content: Int) {
            onToast(context, APPLICATION_CONTEXT.resources.getText(content).toString())
        }
        fun onToast(context: AppCompatActivity, @StringRes content: Int, code: Int) {
            val contentShow = (APPLICATION_CONTEXT.resources.getText(content).toString() + "($code)")
            onToast(context, contentShow)
        }
        fun onToast(context: AppCompatActivity, @StringRes content: Int, message: String?, code: Int) {
            if (message != null) {
                val contentShow = APPLICATION_CONTEXT.resources.getText(content).toString() + "，$message($code)"
                onToast(context, contentShow)
            } else {
                onToast(context, content, code)
            }
        }

        fun getString(@StringRes textId: Int, vararg arg: Any): String {
            return APPLICATION_CONTEXT.resources.getString(textId, *arg)
        }
    }
}
