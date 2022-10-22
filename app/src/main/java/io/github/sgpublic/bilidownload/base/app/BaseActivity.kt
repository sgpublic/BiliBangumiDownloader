package io.github.sgpublic.bilidownload.base.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.yanzhenjie.sofia.Sofia
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.util.finishAll
import io.github.sgpublic.bilidownload.core.util.register
import io.github.sgpublic.bilidownload.core.util.unregister
import java.util.*

abstract class BaseActivity<VB : ViewBinding>: AppCompatActivity() {
    private var _binding: VB? = null
    @Suppress("PropertyName")
    protected abstract val ViewBinding: VB

    private var rootViewBottom: Int = 0

    final override fun onCreate(savedInstanceState: Bundle?) {
        beforeCreate()
        super.onCreate(savedInstanceState)

        register()

        setupContentView()
        if (savedInstanceState != null) {
            STATE.putAll(savedInstanceState)
        }
        onViewSetup()
        onActivityCreated(savedInstanceState != null)
    }

//    protected abstract fun onCreateViewBinding(): VB

    protected open fun beforeCreate() { }

    protected abstract fun onActivityCreated(hasSavedInstanceState: Boolean)

    private fun setupContentView() {
        setContentView(ViewBinding.root)

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
        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = statusbarheight
    }

    protected open fun initViewAtBottom(view: View) {
        rootViewBottom = view.layoutParams.height
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) {
                v: View, insets: WindowInsetsCompat ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val params = view.layoutParams
            params.height = rootViewBottom + b
            view.layoutParams = params
            ViewCompat.onApplyWindowInsets(v, insets)
            return@setOnApplyWindowInsetsListener insets
        }
    }

    protected open fun onViewSetup() { }

    @Suppress("PropertyName")
    protected val STATE: Bundle = Bundle()
    override fun onSaveInstanceState(outState: Bundle) {
        STATE.takeIf { !STATE.isEmpty }?.let {
            outState.putAll(STATE)
        }
        super.onSaveInstanceState(outState)
    }

//    final override val animate: MutableMap<View, ViewState> = mutableMapOf()
    override fun onDestroy() {
        STATE.clear()
//        clearAnimate()
        unregister()
        super.onDestroy()
    }

    protected open fun setAnimateState(isVisible: Boolean, duration: Int, view: View?, callback: Runnable? = null) {
        val onEnd = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    view?.visibility = View.GONE
                    callback?.run()
                }
            }
        }
        if (view == null) {
            Timer().schedule(onEnd, duration.toLong())
            return
        }
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
            Timer().schedule(onEnd, duration.toLong())
        }
    }

    protected open fun isActivityAtBottom(): Boolean = false

    private var last: Long = -1
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        supportFragmentManager.fragments.forEach {
            if (it is BaseFragment<*> && it.onBackPressed()) {
                return
            }
        }
        if (!isActivityAtBottom()){
            super.onBackPressed()
            return
        }
        val now = System.currentTimeMillis()
        if (last == -1L) {
            Application.onToast(this, R.string.text_back_exit)
            last = now
        } else {
            if (now - last < 2000) {
                finishAll()
            } else {
                last = now
                Application.onToast(this, R.string.text_back_exit_notice)
            }
        }
    }

    inline fun <reified VB: ViewBinding> viewBinding(): Lazy<VB> = lazy {
        VB::class.java.getMethod("inflate", LayoutInflater::class.java)
            .invoke(null, layoutInflater) as VB
    }
}