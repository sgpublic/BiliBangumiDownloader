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
        register()
        beforeCreate()
        super.onCreate(savedInstanceState)

        setupContentView()
        if (savedInstanceState != null) {
            STATE.putAll(savedInstanceState)
        }
        onViewSetup()
        onActivityCreated(savedInstanceState != null)
    }

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

    override fun onDestroy() {
        STATE.clear()
        super.onDestroy()
        unregister()
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
            @Suppress("DEPRECATION")
            super.onBackPressed()
            return
        }
        val now = System.currentTimeMillis()
        if (last < 0) {
            Application.onToast(this, R.string.text_back_exit)
            last = now
        } else {
            if (now - last < 2000) {
                Application.finishAll()
            } else {
                last = -1
                Application.onToast(this, R.string.text_back_exit_notice)
            }
        }
    }

    protected inline fun <reified VB: ViewBinding> viewBinding(): Lazy<VB> = lazy {
        VB::class.java.getMethod("inflate", LayoutInflater::class.java)
            .invoke(null, layoutInflater) as VB
    }
}