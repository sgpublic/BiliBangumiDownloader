package io.github.sgpublic.bilidownload

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.room.Room
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import com.arialyy.aria.core.Aria
import com.dtflys.forest.Forest
import com.google.android.gms.net.CronetProviderInstaller
import io.github.sgpublic.bilidownload.app.notifacation.NotifyChannel
import io.github.sgpublic.bilidownload.app.service.DownloadService
import io.github.sgpublic.bilidownload.base.forest.GsonConverter
import io.github.sgpublic.bilidownload.core.forest.core.BiliApiInterceptor
import io.github.sgpublic.bilidownload.core.forest.core.UrlEncodedInterceptor
import io.github.sgpublic.bilidownload.core.logback.PkgNameConverter
import io.github.sgpublic.bilidownload.core.logback.TraceConverter
import io.github.sgpublic.bilidownload.core.room.AppDatabase
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.exsp.ExPreference
import org.slf4j.LoggerFactory

@Suppress("unused")
class Application : Application() {
    private var onBoot = true
    override fun onCreate() {
        super.onCreate()
        application = this
        loadLogbackConfig()
        startListenException()
        log.info("APP启动：${BuildConfig.VERSION_NAME}")
        CronetProviderInstaller.installProvider(this)
        configAria()
        configRoom()
        ExPreference.init(this)
        configForest()
        NotifyChannel.init(this)
    }

    private fun startListenException() {
        Handler(mainLooper).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable){
                    log.error("应用意外退出", e)
                    throw e
                }
            }
        }
    }

    private fun configRoom() {
        val database = Room.databaseBuilder(this, AppDatabase::class.java, BuildConfig.PROJECT_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
        database.DownloadTaskDao().resetProcessing()
        if (database.DownloadTaskDao().oneWaiting != null) {
            DownloadService.startService(this)
        } else {
            log.debug("No waiting task, cancel auto start.")
        }
        room = database
    }

    private fun configAria() {
        Aria.init(this).run {
            appConfig.let {
                it.isUseBroadcast = true
                it.logLevel = 2
            }
            dGroupConfig.let {
                it.setConvertSpeed(true)
            }
        }
    }

    private fun configForest() {
        Forest.config().let {
            it.interceptors = listOf(UrlEncodedInterceptor::class.java, BiliApiInterceptor::class.java)
            it.jsonConverter = GsonConverter
            it.isLogResponseContent = BuildConfig.DEBUG
        }
    }

    private fun loadLogbackConfig() {
        PatternLayout.defaultConverterMap["trace"] = TraceConverter::class.java.name
        PatternLayout.defaultConverterMap["pkgName"] = PkgNameConverter::class.java.name
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val configurator = JoranConfigurator()
        configurator.context = context
        context.reset()
        try {
            configurator.doConfigure(resources.assets.open("logback-bilidl.xml"))
            if (BuildConfig.DEBUG) {
                StatusPrinter.printIfErrorsOccured(context)
            }
        } catch (_: Exception) { }
    }

    override fun onTerminate() {
        log.info("APP结束：${BuildConfig.VERSION_NAME}")
        room.close()
        super.onTerminate()
    }


    companion object {
        private lateinit var application: Application
        private lateinit var room: AppDatabase

        fun onTerminate() {
            application.onTerminate()
        }
        val ApplicationContext: Context get() = application
        val Database: AppDatabase get() = room

        val IsNightMode: Boolean get() = (ApplicationContext.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        fun onToast(context: Context, content: String?) {
            val toast = {
                Toast.makeText(ApplicationContext, content, Toast.LENGTH_SHORT).show()
            }
            if (context is Activity) {
                context.runOnUiThread(toast)
            } else {
                toast.invoke()
            }
        }
        fun onToast(context: Context, @StringRes content: Int) {
            onToast(context, ApplicationContext.resources.getText(content).toString())
        }
        fun onToast(context: Context, @StringRes content: Int, code: Int) {
            val contentShow = (ApplicationContext.resources.getText(content).toString() + "($code)")
            onToast(context, contentShow)
        }
        fun onToast(context: Context, @StringRes content: Int, message: String?, code: Int) {
            if (message != null) {
                val contentShow = ApplicationContext.resources.getText(content).toString() + "，$message($code)"
                onToast(context, contentShow)
            } else {
                onToast(context, content, code)
            }
        }

        fun getString(@StringRes textId: Int, vararg arg: Any): String {
            return ApplicationContext.getString(textId, *arg)
        }

        fun getColor(@ColorRes colorId: Int): Int {
            return ApplicationContext.getColor(colorId)
        }
    }
}
