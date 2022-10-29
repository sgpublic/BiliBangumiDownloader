package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.graphics.Color
import android.util.Half.toFloat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.ui.ArrayRecyclerAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding
import java.text.SimpleDateFormat
import java.util.*

class SeasonEpisodeAdapter(
    private val selectable: Boolean = false
) : ArrayRecyclerAdapter<ItemSeasonEpisodeBinding, SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemSeasonEpisodeBinding.inflate(inflater, parent, false).also {
        val param = it.root.layoutParams
        param.height = RecyclerView.LayoutParams.MATCH_PARENT
        it.root.layoutParams = param
    }

    private val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.CHINA)
    override fun onBindViewHolder(
        context: Context, ViewBinding: ItemSeasonEpisodeBinding,
        data: SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem
    ) {
        Glide.with(context)
            .customLoad(data.cover)
            .withHorizontalPlaceholder()
            .withCrossFade()
            .centerCrop()
            .into(ViewBinding.episodeImage)
        if ((data.badgeInfo?.text ?: "") != "") {
            ViewBinding.episodeVipBackground.setCardBackgroundColor(
                Color.parseColor(context.isNightMode.take(
                    data.badgeInfo!!.bgColorNight, data.badgeInfo!!.bgColor
                ))
            )
            ViewBinding.episodeVip.text = data.badgeInfo!!.text
        } else {
            ViewBinding.episodeVipBackground.visibility = View.GONE
        }
        ViewBinding.episodePublicTime.text = Application.getString(
            R.string.text_episode_public_time, sdf.format(Date(data.pubTime * 1000))
        )
        ViewBinding.episodeIndexTitle.text = try {
            Application.getString(R.string.text_episode_index, data.title.apply { toFloat() })
        } catch (ignore: NumberFormatException) { data.title }
        ViewBinding.episodeTitle.text = data.longTitle
    }

    override fun getClickableView(ViewBinding: ItemSeasonEpisodeBinding) = ViewBinding.itemEpisodeBase
}