package io.github.sgpublic.bilidownload.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.sgpublic.swipebacklayoutx.SwipeBackLayoutX
import com.sgpublic.swipebacklayoutx.app.SwipeBackActivity
import com.yanzhenjie.sofia.Sofia
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.util.ActivityCollector
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.*

abstract class BaseActivity<T : ViewBinding>: SwipeBackActivity() {
    protected val binding: T get() = _binding!!
    private var _binding: T? = null

    private var rootViewBottom: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCollector.addActivity(this)

        setupContentView()
        onViewSetup()
        onActivityCreated(savedInstanceState)
    }

    protected abstract fun onActivityCreated(savedInstanceState: Bundle?)

    @Suppress("UNCHECKED_CAST")
    private fun setupContentView() {
        var type: Class<*>? = javaClass
        var method: Method? = null
        while (type != null) {
            try {
                val clazz: Class<T> = (type.genericSuperclass as ParameterizedType)
                    .actualTypeArguments[0] as Class<T>
                method = clazz.getMethod("inflate", LayoutInflater::class.java)
                break
            } catch (_: NoSuchMethodException) {
            } catch (_:ClassCastException) {
            } finally {
                type = type.superclass
            }
        }
        if (method == null) {
            throw IllegalStateException("unable to create interface for ViewBinding")
        }

        _binding = method.invoke(null, layoutInflater) as T
        setContentView(binding.root)

        if (isActivityAtBottom()){
            setSwipeBackEnable(false)
        } else {
            setSwipeBackEnable(true)
            swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayoutX.EDGE_LEFT)
            swipeBackLayout.setEdgeSize(200)
        }

        Sofia.with(this)
                .statusBarBackgroundAlpha(0)
                .navigationBarBackgroundAlpha(0)
                .invasionNavigationBar()
                .invasionStatusBar()
                .statusBarDarkFont()
    }

    protected open fun initViewAtTop(view: View){
        var statusbarheight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbarheight = resources.getDimensionPixelSize(resourceId)
        }
        val params: LinearLayout.LayoutParams = view.layoutParams as LinearLayout.LayoutParams
        params.topMargin = statusbarheight
    }

    protected open fun initViewAtBottom(view: View) {
        rootViewBottom = view.layoutParams.height
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { v: View?, insets: WindowInsetsCompat? ->
            if (insets != null) {
                val b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                val params = view.layoutParams
                params.height = rootViewBottom + b
                view.layoutParams = params
                ViewCompat.onApplyWindowInsets(v!!, insets)
            }
            insets
        }
    }

    abstract fun onViewSetup()

    override fun onPause() {
        super.onPause()
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment?.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment?.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        ActivityCollector.removeActivity(this)
    }

    protected open fun setAnimateState(isVisible: Boolean, duration: Int, view: View, callback: Runnable? = null) {
        runOnUiThread {
            if (isVisible) {
                view.visibility = View.VISIBLE
                view.animate().alphaBy(0f).alpha(1f).setDuration(duration.toLong())
                    .setListener(null)
                callback?.run()
                return@runOnUiThread
            }
            view.animate().alphaBy(1f).alpha(0f).setDuration(duration.toLong())
                .setListener(null)
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        view.visibility = View.GONE
                        callback?.run()
                    }
                }
            }, duration.toLong())
        }
    }

    protected open fun isActivityAtBottom(): Boolean = false

    private var last: Long = -1
    override fun onBackPressed() {
        if (!isActivityAtBottom()){
            super.onBackPressed()
            return
        }
        val now = System.currentTimeMillis()
        if (last == -1L) {
            onToast(R.string.text_back_exit)
            last = now
        } else {
            if (now - last < 2000) {
                ActivityCollector.finishAll()
            } else {
                last = now
                onToast(R.string.text_back_exit_notice)
            }
        }
    }

    protected fun onToast(content: String?) {
        runOnUiThread {
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }
    }
    protected fun onToast(@StringRes content: Int) {
        onToast(resources.getText(content).toString())
    }
    protected fun onToast(@StringRes content: Int, code: Int) {
        val contentShow = (resources.getText(content).toString() + "($code)")
        onToast(contentShow)
    }
    protected fun onToast(@StringRes content: Int, message: String?, code: Int) {
        if (message != null) {
            val contentShow = resources.getText(content).toString() + "，$message($code)"
            onToast(contentShow)
        } else {
            onToast(content, code)
        }
    }
}