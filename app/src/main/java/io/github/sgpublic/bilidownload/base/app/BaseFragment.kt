package io.github.sgpublic.bilidownload.base.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.viewbinding.ViewBinding
import com.badlogic.gdx.utils.reflect.ClassReflection.getConstructor
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding

abstract class BaseFragment<VB: ViewBinding>(private val contest: AppCompatActivity) : Fragment() {
    private var _binding: VB? = null
    @Suppress("PropertyName")
    protected val ViewBinding: VB get() = _binding!!

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = onCreateViewBinding(container)
        return ViewBinding.root
    }

    override fun getContext(): AppCompatActivity = contest

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onViewSetup()
        if (savedInstanceState != null) {
            STATE.putAll(savedInstanceState)
        }
        beforeFragmentCreated()
        super.onViewCreated(view, savedInstanceState)
        onFragmentCreated(savedInstanceState != null)
    }

    protected open fun beforeFragmentCreated() { }

    protected abstract fun onFragmentCreated(hasSavedInstanceState: Boolean)

    protected fun runOnUiThread(runnable: () -> Unit){
        context.runOnUiThread(runnable)
    }

    protected fun finish(){
        context.finish()
    }

    protected fun <T: View?> findViewById(@IdRes res: Int): T? {
        return view?.findViewById<T>(res)
    }

    open fun getTitle(): CharSequence = ""

    protected open fun initViewAtTop(view: View){
        var statusbarheight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbarheight = resources.getDimensionPixelSize(resourceId)
        }
        val params = try {
            view.layoutParams as LinearLayout.LayoutParams
        } catch (e: ClassCastException) {
            view.layoutParams as FrameLayout.LayoutParams
        }
        params.topMargin = statusbarheight
    }

    protected abstract fun onCreateViewBinding(container: ViewGroup?): VB

    protected val STATE: Bundle = Bundle()
    override fun onSaveInstanceState(outState: Bundle) {
        STATE.takeIf { !STATE.isEmpty }?.let {
            outState.putAll(STATE)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        _binding = null
        STATE.clear()
        super.onDestroyView()
    }

    open fun onViewSetup() { }

    open fun onBackPressed(): Boolean {
        return false
    }

    val mainLooper: Looper get() = contest.mainLooper

    protected fun postDelayed(delay: Long, block: () -> Unit) {
        Handler(mainLooper).postDelayed(block, delay)
    }
    protected fun post(block: () -> Unit) {
        Handler(mainLooper).post(block)
    }

    class Factory(private val context: AppCompatActivity): FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
            val clazz = loadFragmentClass(classLoader, className)
            return if (BaseFragment::class.java.isAssignableFrom(clazz)) {
                clazz.getConstructor(AppCompatActivity::class.java)
                    .newInstance(context)
            } else {
                super.instantiate(classLoader, className)
            }
        }
    }
}