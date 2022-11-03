package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import io.github.sgpublic.bilidownload.base.ui.MultiSelectableAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.util.log
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding

class SeasonEpisodeDialogAdapter: SeasonEpisodeAdapter(), MultiSelectableAdapter<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem> {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemSeasonEpisodeBinding.inflate(inflater, parent, false).also {
        val param = it.root.layoutParams
        param.height = RecyclerView.LayoutParams.WRAP_CONTENT
        param.width = RecyclerView.LayoutParams.MATCH_PARENT
        it.root.layoutParams = param
    }

    private var onChangeSelectMode: (Boolean) -> Unit = { }
    fun setOnChangeSelectModeListener(listener: (Boolean) -> Unit) {
        onChangeSelectMode = listener
    }

    private var selectable = true
    fun setSelectable(selectable: Boolean) {
        if (!selectable) {
            cancelSelectMode()
        }
        this.selectable = selectable
    }

    init {
        setOnItemLongClickListener longClick@{
            if (!selectable) {
                return@longClick false
            }
            if (isSelectMode()) {
                return@longClick false
            }
            entrySelectMode(getPosition(it.id))
            return@longClick true
        }
    }

    @CallSuper
    override fun onBindViewHolder(
        context: Context, ViewBinding: ItemSeasonEpisodeBinding,
        data: SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem
    ) {
        super.onBindViewHolder(context, ViewBinding, data)
        if (selectMode) {
            ViewBinding.episodeSelected.visibility = selection.contains(getPosition(data.id))
                .take(View.VISIBLE, View.GONE)
        }
    }

    override fun getLongClickableView(ViewBinding: ItemSeasonEpisodeBinding) = ViewBinding.itemEpisodeBase

    override fun setData(list: Collection<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>) {
        selection.clear()
        selectMode = false
        super.setData(list)
    }

    private val selection: HashSet<Int> = HashSet()

    override fun addSelection(pos: Int) {
        if (!selectMode || !selectable) {
            return
        }
        selection.add(pos)
        notifyItemChanged(pos)
    }

    private var selectMode: Boolean = false
    fun isSelectMode(): Boolean = selectMode
    override fun removeSelection(pos: Int) {
        if (!selectMode || !selectable) {
            return
        }
        selection.remove(pos)
        notifyItemChanged(pos)
    }

    override fun toggleSelection(pos: Int) {
        if (selection.contains(pos)) {
            removeSelection(pos)
        } else {
            addSelection(pos)
        }
    }

    override fun entrySelectMode(initPos: Int) {
        if (selectMode || !selectable) {
            return
        }
        selectMode = true
        addSelection(initPos.takeIf { it >= 0 } ?: return)
        onChangeSelectMode.invoke(true)
    }

    override fun exitSelectMode(): List<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem> {
        if (!selectMode) {
            log.warn("not in select mode")
            return emptyList()
        }
        selectMode = false
        val selected = ArrayList<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>(selection.size)
        val tmpSelect = ArrayList(selection)
        for (select in tmpSelect) {
            selected.add(getItem(select))
            selection.remove(select)
            notifyItemChanged(select)
        }
        tmpSelect.clear()
        onChangeSelectMode.invoke(false)
        return selected
    }

    override fun cancelSelectMode() {
        if (!selectMode) {
            return
        }
        selectMode = false
        val tmpSelect = ArrayList(selection)
        for (select in tmpSelect) {
            selection.remove(select)
            notifyItemChanged(select)
        }
        tmpSelect.clear()
        onChangeSelectMode.invoke(false)
    }

    override fun selectAll() {
        if (!selectMode || !selectable) {
            return
        }
        for (i in 0 until itemCount) {
            if (!selection.contains(i)) {
                selection.add(i)
                notifyItemChanged(i)
            }
        }
    }

    override fun unselectAll() {
        if (!selectMode || !selectable) {
            return
        }
        for (i in 0 until itemCount) {
            if (selection.contains(i)) {
                selection.remove(i)
                notifyItemChanged(i)
            }
        }
    }
}