package io.github.sgpublic.bilidownload.app.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.ui.SelectableArrayAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.databinding.ItemEpisodeListBinding
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding
import kotlin.collections.ArrayList

class EpisodeListAdapter : SelectableArrayAdapter<ItemEpisodeListBinding, SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>(){
    private val idTmp = HashMap<Long, Int>()
    override fun setData(list: Collection<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>) {
        super.setData(list)
        list.forEachIndexed { index, item ->
            idTmp[item.id] = index
        }
    }
    fun setSelectedEpid(epid: Long) {
        setSelection(idTmp[epid] ?: 0)
    }

    override fun setOnItemClickListener(onClick: (SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem) -> Unit) {
        super.setOnItemClickListener {
            if (getSelectedItem().id != it.id) {
                onClick.invoke(it)
            }
        }
    }

    override fun onBindViewHolder(
        context: Context, ViewBinding: ItemEpisodeListBinding,
        data: SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem
    ) {
        ViewBinding.itemEpisodeTitle.apply {
            if (getSelectedItem().id == data.id) {
                setTextColor(ContextCompat.getColor(context, getSelectedColor()))
                setBackgroundResource(R.drawable.shape_episode_list_border_current)
            } else {
                setTextColor(ContextCompat.getColor(context, getNormalColor()))
                setBackgroundResource(R.drawable.shape_episode_list_border_list)
            }
            text = data.longTitle.ifBlank {
                try {
                    Application.getString(
                        R.string.text_episode_index,
                        data.title.apply { toFloat() })
                } catch (_: NumberFormatException) {
                    data.title
                }
            }
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemEpisodeListBinding.inflate(inflater, parent, false)

    override fun getClickableView(ViewBinding: ItemEpisodeListBinding) = ViewBinding.itemEpisodeTitle
}