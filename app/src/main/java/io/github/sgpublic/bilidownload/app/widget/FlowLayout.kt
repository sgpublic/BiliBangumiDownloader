package io.github.sgpublic.bilidownload.app.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IntDef
import io.github.sgpublic.bilidownload.core.util.dp

/**
 * desc:
 * Created by huangxy on 2018/8/15.
 */
class FlowLayout @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr) {
    private var mLine: Line? = null
    private var mViews: SparseArray<View?>? = null
    private var mHorizontalSpacing = DEFAULT_SPACING
    private var mVerticalSpacing = DEFAULT_SPACING
    private var mUsedWidth = 0
    private val mLines: MutableList<Line?> = ArrayList()
    private var isAlignByCenter = 1

    interface AlienState {
        @IntDef(value = [RIGHT, LEFT, CENTER])
        annotation class Val
        companion object {
            const val RIGHT = 0
            const val LEFT = 1
            const val CENTER = 2
        }
    }

    fun setAlignByCenter(@AlienState.Val isAlignByCenter: Int) {
        this.isAlignByCenter = isAlignByCenter
        requestLayoutInner()
    }

    private fun requestLayoutInner() {
        Handler(Looper.getMainLooper()).post { requestLayout() }
    }

    fun setAdapter(list: List<*>?, res: Int, mItemView: ItemView<Any?>) {
        if (list == null) {
            return
        }
        removeAllViews()
        val layoutPadding = 8.dp
        setHorizontalSpacing(layoutPadding)
        setVerticalSpacing(layoutPadding)
        val size = list.size
        for (i in 0 until size) {
            val item = list[i]!!
            val inflate = LayoutInflater.from(context).inflate(res, null)
            mItemView.getCover(item, ViewHolder(inflate), inflate, i)
            addView(inflate)
        }
    }

    abstract class ItemView<T> {
        abstract fun getCover(item: T, holder: ViewHolder?, inflate: View?, position: Int)
    }

    inner class ViewHolder(var mConvertView: View) {
        @Suppress("UNCHECKED_CAST")
        fun <T: View?> getView(viewId: Int): T? {
            var view = mViews!![viewId]
            if (view == null) {
                view = mConvertView.findViewById(viewId)
                mViews!!.put(viewId, view)
            }
            try {
                return view as T?
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
            return null
        }

        fun setText(viewId: Int, text: String?) {
            val view = getView<TextView>(viewId)!!
            view.text = text
        }

        init {
            mViews = SparseArray()
        }
    }

    fun setHorizontalSpacing(spacing: Int) {
        if (mHorizontalSpacing != spacing) {
            mHorizontalSpacing = spacing
            requestLayoutInner()
        }
    }

    fun setVerticalSpacing(spacing: Int) {
        if (mVerticalSpacing != spacing) {
            mVerticalSpacing = spacing
            requestLayoutInner()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingRight - paddingLeft
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        restoreLine() // 还原数据，以便重新记录
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == GONE) {
                continue
            }
            val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                sizeWidth,
                if (modeWidth == MeasureSpec.EXACTLY) MeasureSpec.AT_MOST else modeWidth
            )
            val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                sizeHeight,
                if (modeHeight == MeasureSpec.EXACTLY) MeasureSpec.AT_MOST else modeHeight
            )
            // 测量child
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            if (mLine == null) {
                mLine = Line()
            }
            val childWidth = child.measuredWidth
            mUsedWidth += childWidth // 增加使用的宽度
            if (mUsedWidth <= sizeWidth) { // 使用宽度小于总宽度，该child属于这一行。
                mLine!!.addView(child) // 添加child
                mUsedWidth += mHorizontalSpacing // 加上间隔
                if (mUsedWidth >= sizeWidth) { // 加上间隔后如果大于等于总宽度，需要换行
                    if (!newLine()) {
                        break
                    }
                }
            } else { // 使用宽度大于总宽度。需要换行
                if (mLine!!.viewCount == 0) { // 如果这行一个child都没有，即使占用长度超过了总长度，也要加上去，保证每行都有至少有一个child
                    mLine!!.addView(child) // 添加child
                    if (!newLine()) { // 换行
                        break
                    }
                } else { // 如果该行有数据了，就直接换行
                    if (!newLine()) { // 换行
                        break
                    }
                    // 在新的一行，不管是否超过长度，先加上去，因为这一行一个child都没有，所以必须满足每行至少有一个child
                    mLine!!.addView(child)
                    mUsedWidth += childWidth + mHorizontalSpacing
                }
            }
        }
        if (mLine != null && mLine!!.viewCount > 0 && !mLines.contains(mLine)) {
            // 由于前面采用判断长度是否超过最大宽度来决定是否换行，则最后一行可能因为还没达到最大宽度，所以需要验证后加入集合中
            mLines.add(mLine)
        }
        val totalWidth = MeasureSpec.getSize(widthMeasureSpec)
        var totalHeight = 0
        val linesCount = mLines.size
        for (i in 0 until linesCount) { // 加上所有行的高度
            totalHeight += mLines[i]!!.mHeight
        }
        totalHeight += mVerticalSpacing * (linesCount - 1) // 加上所有间隔的高度
        totalHeight += paddingTop + paddingBottom // 加上padding
        // 设置布局的宽高，宽度直接采用父view传递过来的最大宽度，而不用考虑子view是否填满宽度，因为该布局的特性就是填满一行后，再换行
        // 高度根据设置的模式来决定采用所有子View的高度之和还是采用父view传递过来的高度
        setMeasuredDimension(totalWidth, resolveSize(totalHeight, heightMeasureSpec))
    }

    private fun restoreLine() {
        mLines.clear()
        mLine = Line()
        mUsedWidth = 0
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed) {
            val left = paddingLeft //获取最初的左上点
            var top = paddingTop
            val count = mLines.size
            for (i in 0 until count) {
                val line = mLines[i]
                line!!.LayoutView(left, top) //摆放每一行中子View的位置
                top += line.mHeight + mVerticalSpacing //为下一行的top赋值
            }
        }
    }

    /**
     * 新增加一行
     */
    private fun newLine(): Boolean {
        mLines.add(mLine)
        val mMaxLinesCount = Int.MAX_VALUE
        if (mLines.size < mMaxLinesCount) {
            mLine = Line()
            mUsedWidth = 0
            return true
        }
        return false
    }

    internal inner class Line {
        var mWidth = 0 // 该行中所有的子View累加的宽度
        var mHeight = 0 // 该行中所有的子View中高度的那个子View的高度
        var views: MutableList<View> = ArrayList()
        fun addView(view: View) { // 往该行中添加一个
            views.add(view)
            mWidth += view.measuredWidth
            val childHeight = view.measuredHeight
            mHeight = if (mHeight < childHeight) childHeight else mHeight //高度等于一行中最高的View
        }

        val viewCount: Int
            get() = views.size

        //摆放行中子View的位置
        fun LayoutView(l: Int, t: Int) {
            var left = l
            val count = viewCount
            val layoutWidth = measuredWidth - paddingLeft - paddingRight //行的总宽度
            //剩余的宽度，是除了View和间隙的剩余空间
            val surplusWidth = layoutWidth - mWidth - mHorizontalSpacing * (count - 1)
            if (surplusWidth >= 0) {
                for (i in 0 until count) {
                    val view = views[i]
                    val childWidth = view.measuredWidth
                    val childHeight = view.measuredHeight
                    //计算出每个View的顶点，是由最高的View和该View高度的差值除以2
                    var topOffset = ((mHeight - childHeight) / 2.0 + 0.5).toInt()
                    if (topOffset < 0) {
                        topOffset = 0
                    }

                    //布局View
                    if (i == 0) {
                        when (isAlignByCenter) {
                            AlienState.CENTER -> left += surplusWidth / 2
                            AlienState.RIGHT -> left += surplusWidth
                            else -> left = 0
                        }
                    }
                    view.layout(left, t + topOffset, left + childWidth, t + topOffset + childHeight)
                    left += childWidth + mVerticalSpacing //为下一个View的left赋值
                }
            }
        }
    }

    companion object {
        const val DEFAULT_SPACING = 20
    }
}