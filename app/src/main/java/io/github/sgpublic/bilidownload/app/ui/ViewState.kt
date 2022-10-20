package io.github.sgpublic.bilidownload.app.ui

import android.animation.Animator
import android.view.ViewPropertyAnimator

data class ViewState(
    var visible: Boolean = false,
    var animate: ViewPropertyAnimator? = null
) {
    interface AnimateListener : Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator) {}
        override fun onAnimationCancel(p0: Animator) {}
        override fun onAnimationRepeat(p0: Animator) {}
        override fun onAnimationEnd(p0: Animator)
    }
}
