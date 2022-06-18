package io.github.sgpublic.bilidownload

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.rule.ServiceTestRule

abstract class ApplicationText {
    protected val applicationContext: Context = ApplicationProvider.getApplicationContext()

    protected fun runOnUiThread(runnable: Runnable) {
        UiThreadStatement.runOnUiThread(runnable)
    }

    protected fun startService(intent: Intent) {
        val serviceRule = ServiceTestRule()
        serviceRule.startService(intent)
    }

    protected fun getSharedPreferences(): SharedPreferences {
        return applicationContext.getSharedPreferences("user", Context.MODE_PRIVATE)
    }
}