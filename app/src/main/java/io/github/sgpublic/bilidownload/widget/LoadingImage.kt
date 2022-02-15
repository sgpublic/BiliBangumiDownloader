package io.github.sgpublic.bilidownload.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import io.github.sgpublic.bilidownload.R
import java.util.*

class LoadingImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private var timer: Timer? = null

    fun startLoad() {
        visibility = View.VISIBLE
        timer = Timer()
        var imageIndex = 0
        timer?.schedule(object : TimerTask() {
            override fun run() {
                try {
                    imageIndex = R.drawable.pic_search_doing_2.takeIf {
                        imageIndex == R.drawable.pic_search_doing_1
                    } ?: R.drawable.pic_search_doing_1
                    setImageResource(imageIndex)
                } catch (_: NullPointerException) { }
            }
        }, 0, 500)
    }

    fun stopLoad(hasError: Boolean = false) {
        removeTimer()
        if (!hasError) {
            visibility = View.GONE
            return
        }
        visibility = View.VISIBLE
        setImageResource(R.drawable.pic_load_failed)
    }

    override fun onDetachedFromWindow() {
        removeTimer()
        super.onDetachedFromWindow()
    }

    private fun removeTimer() {
        timer?.let {
            it.cancel()
            timer = null
        }
    }
}