package io.github.sgpublic.bilidownload.ui.list

import android.widget.BaseAdapter
import androidx.annotation.ColorRes
import io.github.sgpublic.bilidownload.R

abstract class SelectableBaseAdapter : BaseAdapter() {
    protected open var position: Int = 0
    fun setSelection(pos: Int) {
        position = pos
        notifyDataSetChanged()
    }
    fun getSelection(): Int = position

    protected var selectedColor = R.color.colorPrimary
    protected var normalColor = R.color.color_player_controller
    fun setColor(@ColorRes selectedColor: Int, @ColorRes normalColor: Int) {
        this.selectedColor = selectedColor
        this.normalColor = normalColor
    }

    protected var onItemClick: (Int, String) -> Unit = { _, _ -> }
    fun setOnItemClickListener(onItemClickListener: (Int, String) -> Unit) {
        onItemClick = onItemClickListener
    }
}