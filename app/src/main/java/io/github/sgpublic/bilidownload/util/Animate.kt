package io.github.sgpublic.bilidownload.util

import android.animation.Animator
import android.view.View
import io.github.sgpublic.bilidownload.ui.ViewState

interface Animate {
    val animate: MutableMap<View, ViewState>
    fun startAnimate(isVisible: Boolean, duration: Long, view: View?, callback: () -> Unit = { }) {
        if (view == null) {
            return
        }
        if (animate[view]?.visible == isVisible) {
            return
        }
        if (!animate.containsKey(view)) {
            animate[view] = ViewState()
        }
        val state = animate[view]!!
        if (state.visible == isVisible) {
            return
        }
        state.visible = isVisible
        state.animate?.cancel()
        state.animate = view.animate().apply {
            if (isVisible) {
                alphaBy(0f).alpha(1f)
            } else {
                alphaBy(1f).alpha(0f)
            }
        }.setDuration(duration).setListener(object : ViewState.AnimateListener {
            override fun onAnimationEnd(p0: Animator?) {
                try {
                    state.animate = null
                    callback()
                } catch (_: NullPointerException) { }
            }
        })
    }
    fun clearAnimate() {
        animate.forEach { (_, state) ->
            state.animate?.cancel()
        }
        animate.clear()
    }
}