package io.github.sgpublic.bilidownload.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class ObservableRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    init {
        overScrollMode = OVER_SCROLL_NEVER
    }

    private var x1 = 0f
    private var y1 = 0f
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val x2: Float = event.x
                val y2: Float = event.y
                val offsetX: Float = abs(x2 - x1)
                val offsetY: Float = abs(y2 - y1)
                parent.requestDisallowInterceptTouchEvent(offsetX <= offsetY)
            }
            MotionEvent.ACTION_UP -> {
                y1 = 0f
                x1 = y1
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(event)
    }
}