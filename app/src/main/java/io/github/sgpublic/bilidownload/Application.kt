package io.github.sgpublic.bilidownload

import android.app.Application
import android.content.*
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import com.arialyy.aria.core.Aria
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.core.util.LogCat
import io.github.sgpublic.bilidownload.core.util.finishAll
import io.github.sgpublic.bilidownload.room.AppDatabase
import io.github.sgpublic.bilidownload.room.entity.TaskEntity
import io.github.sgpublic.bilidownload.service.DownloadService

@Suppress("unused")
class Application : Application() {
    private var onBoot = true
    override fun onCreate() {
        super.onCreate()
        LogCat.i("APP启动")
        application = this
        room = Room.databaseBuilder(applicationContext, AppDatabase::class.java, BuildConfig.PROJECT_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
        room.TasksDao().let {
            it.resetPreparingTasks()
            it.resetProcessingTasks()
        }
        Aria.init(applicationContext)
    }

    private fun startListenException() {
        Handler(mainLooper).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable){
                    CrashHandler.saveExplosion(e, -100, "应用意外停止")
                    if (!BuildConfig.DEBUG) {
                        finishAll()
                        break
                    }
                }
            }
        }
    }

    private fun showExceptionDialog(exc: String?) {
        // TODO 应用意外退出弹窗
        if (exc == null) {
            finishAll()
            return
        }
        XPopup.Builder(APPLICATION_CONTEXT).asConfirm(
            getString(R.string.title_function_crash),
            getString(R.string.text_function_crash),
            getString(R.string.text_function_crash_exit),
            getString(R.string.text_function_crash_copy), {
                val cs = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cs.setPrimaryClip(ClipData.newPlainText("exception", exc))
                finishAll()
            }, {
                finishAll()
            }, false
        ).show()
    }

    override fun onTerminate() {
        LogCat.i("APP结束")
        if (DATABASE.isOpen) {
            DATABASE.close()
        }
        tasksObserver?.let {
            processingTasks?.removeObserver(it)
            waitingTasks?.removeObserver(it)
        }
        super.onTerminate()
    }


    companion object {
        private lateinit var application: Application
        private lateinit var room: AppDatabase

        val APPLICATION_CONTEXT: Context get() = application.applicationContext
        val CONTENT_RESOLVER: ContentResolver get() = APPLICATION_CONTEXT.contentResolver
        val DATABASE: AppDatabase get() = room

        val IS_NIGHT_MODE: Boolean get() = APPLICATION_CONTEXT.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        private var tasksObserver: Observer<List<TaskEntity>>? = null
        private var processingTasks: LiveData<List<TaskEntity>>? = null
        private var waitingTasks: LiveData<List<TaskEntity>>? = null
        fun startListenTask() {
            synchronized(this) {
                if (tasksObserver != null) {
                    return
                }
                tasksObserver = Observer<List<TaskEntity>> { tasks ->
                    if (!ConfigManager.TASK_AUTO_START) {
                        return@Observer
                    }
                    if (tasks.isNotEmpty()) {
                        val intent = Intent(APPLICATION_CONTEXT, DownloadService::class.java)
                        APPLICATION_CONTEXT.startForegroundService(intent)
                    }
                }.also { observer ->
                    DATABASE.TasksDao().run {
                        processingTasks = listenByTaskStatus(TaskEntity.STATUS_PROCESSING).also {
                            it.observeForever(observer)
                        }
                        waitingTasks = listenByTaskStatus(TaskEntity.STATUS_WAITING).also {
                            it.observeForever(observer)
                        }
                    }
                }
            }
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

        fun getColor(@ColorRes colorId: Int): Int {
            return APPLICATION_CONTEXT.getColor(colorId)
        }
    }
}
