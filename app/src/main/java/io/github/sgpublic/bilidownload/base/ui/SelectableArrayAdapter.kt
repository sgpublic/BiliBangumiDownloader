package io.github.sgpublic.bilidownload.base.ui

import androidx.annotation.CallSuper
import androidx.annotation.ColorRes
import androidx.viewbinding.ViewBinding
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.util.log

abstract class SelectableArrayAdapter<VB: ViewBinding, ItemT: Any> : ArrayRecyclerAdapter<VB, ItemT>() {
    open fun getItemPosition(id: Long) = 0
}

interface SingleSelection<ItemT: Any> {
    var position: Int

    @CallSuper
    fun setSelection(pos: Int) {
        val pre = position
        position = pos
        Adapter.notifyItemChanged(pre)
        Adapter.notifyItemChanged(pos)
    }

    @CallSuper
    fun getSelection(): Int = position

    fun getSelectedItem() = Adapter.getItem(getSelection())

    @ColorRes
    fun getSelectedColor(): Int = R.color.colorPrimary
    @ColorRes
    fun getNormalColor(): Int = R.color.color_player_controller
    
    val Adapter: SelectableArrayAdapter<*, ItemT>
}

interface MultiSelectable<ItemT: Any> {
    val multiSelection: HashSet<Int>
    var selectMode: Boolean
    val Adapter: SelectableArrayAdapter<*, ItemT>

//    private var onChangeSelectMode: (Boolean) -> Unit = { }
//    fun setOnChangeSelectModeListener(listener: (Boolean) -> Unit) {
//        onChangeSelectMode = listener
//    }
//    override fun invokeOnChangeSelectMode(mode: Boolean) {
//        onChangeSelectMode.invoke(mode)
//    }
    
    fun invokeOnChangeSelectMode(mode: Boolean)

    fun addSelection(pos: Int) {
        if (!selectMode) {
            return
        }
        multiSelection.add(pos)
        Adapter.notifyItemChanged(pos)
    }

    fun isSelectMode(): Boolean = selectMode
    fun removeSelection(pos: Int) {
        if (!selectMode) {
            return
        }
        multiSelection.remove(pos)
        Adapter.notifyItemChanged(pos)
    }

    fun toggleSelection(pos: Int) {
        if (multiSelection.contains(pos)) {
            removeSelection(pos)
        } else {
            addSelection(pos)
        }
    }

    fun entrySelectMode(initPos: Int = -1) {
        if (selectMode) {
            return
        }
        selectMode = true
        addSelection(initPos.takeIf { it >= 0 } ?: return)
        invokeOnChangeSelectMode(true)
    }

    fun exitSelectMode(): List<ItemT> {
        if (!selectMode) {
            log.warn("not in select mode")
            return emptyList()
        }
        selectMode = false
        val selected = ArrayList<ItemT>(multiSelection.size)
        val tmpSelect = ArrayList(multiSelection)
        for (select in tmpSelect) {
            selected.add(Adapter.getItem(select))
            multiSelection.remove(select)
            Adapter.notifyItemChanged(select)
        }
        tmpSelect.clear()
        invokeOnChangeSelectMode(false)
        return selected
    }

    fun cancelSelectMode() {
        if (!selectMode) {
            return
        }
        selectMode = false
        val tmpSelect = ArrayList(multiSelection)
        for (select in tmpSelect) {
            multiSelection.remove(select)
            Adapter.notifyItemChanged(select)
        }
        tmpSelect.clear()
        invokeOnChangeSelectMode(false)
    }

    fun selectAll() {
        if (!selectMode) {
            return
        }
        for (i in 0 until Adapter.itemCount) {
            if (!multiSelection.contains(i)) {
                multiSelection.add(i)
                Adapter.notifyItemChanged(i)
            }
        }
    }

    fun unselectAll() {
        if (!selectMode) {
            return
        }
        for (i in 0 until Adapter.itemCount) {
            if (multiSelection.contains(i)) {
                multiSelection.remove(i)
                Adapter.notifyItemChanged(i)
            }
        }
    }
}