package io.github.sgpublic.bilidownload.app.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.ui.SelectableArrayAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.databinding.ItemEpisodeListBinding
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding
import kotlin.collections.ArrayList

class EpisodeListAdapter : SelectableArrayAdapter<ItemEpisodeListBinding, SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>(){
    override fun onBindViewHolder(
        context: Context, ViewBinding: ItemEpisodeListBinding,
        data: SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem
    ) {
        ViewBinding.itemEpisodeTitle.apply {
            if (position == this@EpisodeListAdapter.getSelection()) {
                setTextColor(ContextCompat.getColor(context, selectedColor))
                setBackgroundResource(R.drawable.shape_episode_list_border_current)
            } else {
                setTextColor(ContextCompat.getColor(context, normalColor))
                setBackgroundResource(R.drawable.shape_episode_list_border_list)
            }
            text = data.longTitle
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemEpisodeListBinding.inflate(inflater, parent, false)

    override fun getClickableView(ViewBinding: ItemEpisodeListBinding) = ViewBinding.itemEpisodeTitle
}