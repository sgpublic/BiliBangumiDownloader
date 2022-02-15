package io.github.sgpublic.bilidownload.ui.list

import android.widget.BaseAdapter

abstract class SelectableBaseAdapter : BaseAdapter() {
    protected open var position: Int = 0
    fun setSelection(pos: Int) {
        position = pos
        notifyDataSetChanged()
    }
    fun getSelection(): Int = position

    protected var onItemClick: (Int, String) -> Unit = { _, _ -> }
    fun setOnItemClickListener(onItemClickListener: (Int, String) -> Unit) {
        onItemClick = onItemClickListener
    }
}