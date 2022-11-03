package io.github.sgpublic.bilidownload.core.util

import android.animation.Animator
import android.content.IntentSender.OnFinished
import android.view.View
import com.lxj.xpopup.XPopup
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R

fun XPopup.Builder.showAsOutsideConfirm(confirm: () -> Unit) {
    asConfirm(
        Application.getString(R.string.title_open_other),
        Application.getString(R.string.text_open_other),
    ) {
        confirm.invoke()
    }.show()
}

fun View.animate(isVisible: Boolean, duration: Long, onFinished: (() -> Unit)? = null) {
    post {
        animate().apply {
            if (isVisible) {
                alphaBy(0f).alpha(1f)
            } else {
                alphaBy(1f).alpha(0f)
            }
        }.setDuration(duration).also {
            if (onFinished != null) {
                it.setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator) {}
                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationRepeat(p0: Animator) {}
                    override fun onAnimationEnd(p0: Animator) {
                        onFinished.invoke()
                    }
                })
            }
        }.start()
    }
}