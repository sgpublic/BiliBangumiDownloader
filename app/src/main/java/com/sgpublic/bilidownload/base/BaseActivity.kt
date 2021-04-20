package com.sgpublic.bilidownload.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.sgpublic.bilidownload.util.ActivityCollector
import com.sgpublic.bilidownload.databinding.ActivityAboutBinding
import com.sgpublic.bilidownload.util.ConfigManager
import com.yanzhenjie.sofia.Sofia
import me.imid.swipebacklayout.lib.SwipeBackLayout
import me.imid.swipebacklayout.lib.app.SwipeBackActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*

abstract class BaseActivity<T: ViewBinding>: SwipeBackActivity() {
    protected lateinit var binding: T

    private val edgeSize: Int = 200
    protected var rootViewBottom: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        PushAgent.getInstance(this).onAppStart()

        ActivityCollector.addActivity(this)

        setSwipeBackEnable(onSetSwipeBackEnable())
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)
        swipeBackLayout.setEdgeSize(edgeSize)

        binding = getContentView()
        setContentView(binding.root)
        onViewSetup()
        onActivityCreated(savedInstanceState)
    }

    protected abstract fun onActivityCreated(savedInstanceState: Bundle?)

    protected abstract fun getContentView(): T

    protected abstract fun onSetSwipeBackEnable(): Boolean

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
        rootViewBottom = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { v, insets ->
            var b = 0
            if (insets != null) {
                b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            }
            view.setPadding(
                view.paddingLeft, view.paddingTop, view.paddingRight, rootViewBottom + b
            )
            ViewCompat.onApplyWindowInsets(v, insets)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    protected open fun onViewSetup(){
        Sofia.with(this)
            .statusBarBackgroundAlpha(0)
            .navigationBarBackgroundAlpha(0)
            .invasionNavigationBar()
            .invasionStatusBar()
            .statusBarDarkFont()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
//        MobclickAgent.onResume(this)
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            fragment?.onResume()
        }
    }
//
//    override fun onPause() {
//        super.onPause()
//        MobclickAgent.onPause(this)
//    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }

    protected open fun setAnimateState(
        is_visible: Boolean,
        duration: Int,
        view: View,
        callback: Runnable? = null
    ) {
        runOnUiThread {
            if (is_visible) {
                view.visibility = View.VISIBLE
                view.animate().alphaBy(0f).alpha(1f).setDuration(duration.toLong())
                    .setListener(null)
                callback?.run()
            } else {
                view.animate().alphaBy(1f).alpha(0f).setDuration(duration.toLong())
                    .setListener(null)
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        view.visibility = View.GONE
                        callback?.run()
                    }
                }, duration.toLong())
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

    protected fun dip2px(dpValue: Float): Int {
        val scales = resources.displayMetrics.density
        return (dpValue * scales + 0.5f).toInt()
    }

    protected open fun saveExplosion(e: Throwable?, code: Int) {
        try {
            e?.let {
                val exceptionLog: JSONObject
                var exceptionLogContent = JSONArray()
                val exception = File(
                    applicationContext.getExternalFilesDir("log")?.path,
                    "exception.json"
                )
                var log_content: String
                try {
                    val fileInputStream =
                        FileInputStream(exception)
                    val bufferedReader =
                        BufferedReader(InputStreamReader(fileInputStream))
                    var line: String?
                    val stringBuilder = StringBuilder()
                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    log_content = stringBuilder.toString()
                } catch (e1: IOException) {
                    log_content = ""
                }
                if (log_content != "") {
                    exceptionLog = JSONObject(log_content)
                    if (!exceptionLog.isNull("logs")) {
                        exceptionLogContent = exceptionLog.getJSONArray("logs")
                    }
                }
                val elements = e.stackTrace
                var elementIndex: StackTraceElement
                val crashMsgJson = JSONObject()
                val crashMsgArray = JSONArray()
                val crashMsgArrayIndex = JSONObject()
                val crashStackTrace = JSONArray()
                var crashMsgIndex = 0
                while (crashMsgIndex < elements.size && crashMsgIndex < 10) {
                    elementIndex = e.stackTrace[crashMsgIndex]
                    val crashStackTraceIndex = JSONObject()
                    crashStackTraceIndex.put("class", elementIndex.className)
                    crashStackTraceIndex.put("line", elementIndex.lineNumber)
                    crashStackTraceIndex.put("method", elementIndex.methodName)
                    crashStackTrace.put(crashStackTraceIndex)
                    crashMsgIndex++
                }
                val configString = StringBuilder(e.toString())
                for (config_index in 0..2) {
                    elementIndex = elements[config_index]
                    configString.append("\nat ").append(elementIndex.toString())
                }
                ConfigManager(this).putString("last_exception", configString.toString())
                crashMsgArrayIndex.put("code", code)
                crashMsgArrayIndex.put("message", e.toString())
                crashMsgArrayIndex.put("stack_trace", crashStackTrace)
                crashMsgArray.put(crashMsgArrayIndex)
                var exceptionLogIndex = 0
                while (exceptionLogIndex < exceptionLogContent.length() && exceptionLogIndex < 2) {
                    val msg_index =
                        exceptionLogContent.getJSONObject(exceptionLogIndex)
                    if (crashMsgArrayIndex.toString() != msg_index.toString()) {
                        crashMsgArray.put(msg_index)
                    }
                    exceptionLogIndex++
                }
                crashMsgJson.put("logs", crashMsgArray)
                val fileOutputStream = FileOutputStream(exception)
                fileOutputStream.write(crashMsgJson.toString().toByteArray())
                fileOutputStream.close()
            }
        } catch (ignore: JSONException) {
        } catch (ignore: IOException) {
        } catch (ignore: IllegalArgumentException) {}
    }
}