package io.github.sgpublic.bilidownload.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

class ObservableScrollView : NestedScrollView {
    private var scrollViewListener: ScrollViewListener? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    fun setScrollViewListener(scrollViewListener: ScrollViewListener?) {
        this.scrollViewListener = scrollViewListener
    }

    override fun onScrollChanged(x: Int, y: Int, oldx: Int, oldy: Int) {
        super.onScrollChanged(x, y, oldx, oldy)
        if (scrollViewListener != null) {
            scrollViewListener!!.onScrollChanged(this, x, y, oldx, oldy)
        }
    }

    interface ScrollViewListener {
        fun onScrollChanged(scrollView: ObservableScrollView?, x: Int, y: Int, oldx: Int, oldy: Int)
    }
}