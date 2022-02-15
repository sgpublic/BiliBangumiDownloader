package io.github.sgpublic.bilidownload

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.statement.UiThreadStatement

abstract class ApplicationText {
    protected val applicationContext: Context = ApplicationProvider.getApplicationContext()

    protected fun runOnUiThread(runnable: Runnable) {
        UiThreadStatement.runOnUiThread(runnable)
    }
}