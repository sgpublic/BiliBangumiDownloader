package io.github.sgpublic.bilidownload.app.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.util.TimerCompat

class LoadingImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private var timer: TimerCompat? = null

    fun startLoad() {
        visibility = View.VISIBLE
        var imageIndex = 0
        timer = TimerCompat(context).also {
            it.schedule(500, 500) {
                imageIndex = R.drawable.pic_search_doing_2.takeIf {
                    imageIndex == R.drawable.pic_search_doing_1
                } ?: R.drawable.pic_search_doing_1
                setImageResource(imageIndex)
            }
        }
    }

    fun stopLoad(hasError: Boolean = false) {
        removeTimer()
        if (!hasError) {
            visibility = View.GONE
            return
        }
        visibility = View.VISIBLE
        setImageResource(R.drawable.pic_load_failed_h)
    }

    fun loadEmpty() {
        stopLoad(true)
        setImageResource(R.drawable.pic_null)
    }

    override fun onDetachedFromWindow() {
        removeTimer()
        super.onDetachedFromWindow()
    }

    private fun removeTimer() {
        timer?.cancel()
        timer = null
    }
}