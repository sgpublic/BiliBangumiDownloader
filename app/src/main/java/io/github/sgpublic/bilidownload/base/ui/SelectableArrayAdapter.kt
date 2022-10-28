package io.github.sgpublic.bilidownload.base.ui

import androidx.annotation.ColorRes
import androidx.viewbinding.ViewBinding
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding

abstract class SelectableArrayAdapter<VB: ViewBinding, ItemT> : ArrayRecyclerAdapter<VB, ItemT>() {
    protected open var position: Int = 0
    fun setSelection(pos: Int) {
        val pre = position
        position = pos
        notifyItemChanged(pre)
        notifyItemChanged(pos)
    }
    fun getSelection(): Int = position

    protected var selectedColor = R.color.colorPrimary
    protected var normalColor = R.color.color_player_controller
    fun setColor(@ColorRes selectedColor: Int, @ColorRes normalColor: Int) {
        this.selectedColor = selectedColor
        this.normalColor = normalColor
    }
}