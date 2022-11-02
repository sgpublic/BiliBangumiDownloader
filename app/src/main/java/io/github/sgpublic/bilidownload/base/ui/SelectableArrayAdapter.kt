package io.github.sgpublic.bilidownload.base.ui

import androidx.annotation.CallSuper
import androidx.annotation.ColorRes
import androidx.viewbinding.ViewBinding
import io.github.sgpublic.bilidownload.R

abstract class SelectableArrayAdapter<VB: ViewBinding, ItemT: Any> : ArrayRecyclerAdapter<VB, ItemT>(), SingleSelectableAdapter {
    protected var position: Int = 0
        private set

    @CallSuper
    override fun setSelection(pos: Int) {
        val pre = position
        position = pos
        notifyItemChanged(pre)
        notifyItemChanged(pos)
    }

    @CallSuper
    override fun getSelection(): Int = position

    override fun getSelectedItem() = getItem(getSelection())
}

interface SingleSelectableAdapter {
    fun setSelection(pos: Int)
    fun getSelection(): Int
    fun getSelectedItem(): Any
    @ColorRes
    fun getSelectedColor(): Int = R.color.colorPrimary
    @ColorRes
    fun getNormalColor(): Int = R.color.color_player_controller
}

interface MultiSelectableAdapter<T> {
    fun addSelection(pos: Int)
    fun removeSelection(pos: Int)
    fun toggleSelection(pos: Int)
    fun entrySelectMode(initPos: Int = -1)
    fun exitSelectMode(): List<T>
    fun cancelSelectMode()
    fun selectAll()
    fun unselectAll()
}