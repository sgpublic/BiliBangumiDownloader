package io.github.sgpublic.bilidownload.base.app

import android.app.Service
import androidx.annotation.CallSuper
import io.github.sgpublic.bilidownload.core.util.register
import io.github.sgpublic.bilidownload.core.util.unregister

/**
 *
 * @author Madray Haven
 * @date 2022/11/3 17:17
 */
abstract class BaseService: Service() {
    @CallSuper
    override fun onCreate() {
        register()
    }

    @CallSuper
    override fun onDestroy() {
        unregister()
    }
}