package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import io.github.sgpublic.bilidownload.base.ui.MultiSelectable
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding
import java.util.HashSet

class SeasonEpisodeDialogAdapter: SeasonEpisodeAdapter(),
    MultiSelectable<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>{
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemSeasonEpisodeBinding.inflate(inflater, parent, false).also {
        val param = it.root.layoutParams
        param.height = RecyclerView.LayoutParams.WRAP_CONTENT
        param.width = RecyclerView.LayoutParams.MATCH_PARENT
        it.root.layoutParams = param
    }

    private var selectable: Boolean = false
    fun setIsSelectable(selectable: Boolean) {
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
            entrySelectMode(getItemPosition(it.id))
            return@longClick true
        }
    }

    @CallSuper
    override fun onBindViewHolder(
        context: Context, ViewBinding: ItemSeasonEpisodeBinding,
        data: SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem
    ) {
        super.onBindViewHolder(context, ViewBinding, data)
        ViewBinding.episodeSelected.visibility = (selectMode && multiSelection.contains(getItemPosition(data.id)))
            .take(View.VISIBLE, View.GONE)
    }

    override fun getLongClickableView(ViewBinding: ItemSeasonEpisodeBinding) = ViewBinding.itemEpisodeBase

    override var selectMode: Boolean = false
    override val multiSelection: HashSet<Int> = HashSet()

    private var onChangeSelectMode: (Boolean) -> Unit = { }
    fun setOnChangeSelectModeListener(listener: (Boolean) -> Unit) {
        onChangeSelectMode = listener
    }
    override fun invokeOnChangeSelectMode(mode: Boolean) {
        onChangeSelectMode.invoke(mode)
    }
}