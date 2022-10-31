package io.github.sgpublic.bilidownload

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.arialyy.aria.core.Aria
import com.dtflys.forest.Forest
import com.dtflys.forest.converter.json.ForestGsonConverter
import io.github.sgpublic.bilidownload.base.forest.GsonConverter
import io.github.sgpublic.bilidownload.core.forest.core.BiliApiInterceptor
import io.github.sgpublic.bilidownload.core.forest.core.UrlEncodedInterceptor
import io.github.sgpublic.bilidownload.core.grpc.GrpcModule
import io.github.sgpublic.bilidownload.core.room.AppDatabase
import io.github.sgpublic.bilidownload.core.util.BASE_64
import io.github.sgpublic.bilidownload.core.util.finishAll
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.exsp.ExPreference
import okio.IOException
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

@Suppress("unused")
class Application : Application() {
    private var onBoot = true
    override fun onCreate() {
        super.onCreate()
        application = this
        loadLogbackConfig()
        startListenException()
        log.info("APP启动")
        room = Room.databaseBuilder(this, AppDatabase::class.java, BuildConfig.PROJECT_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
        Aria.init(this)
        ExPreference.init(this)
        configForest()
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

    private fun configForest() {
        Forest.config().let {
            it.interceptors = listOf(UrlEncodedInterceptor::class.java, BiliApiInterceptor::class.java)
            it.jsonConverter = GsonConverter
            it.isLogResponseContent = BuildConfig.DEBUG
        }
    }

    private fun loadLogbackConfig() {
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
        log.info("APP结束")
        if (DATABASE.isOpen) {
            DATABASE.close()
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
