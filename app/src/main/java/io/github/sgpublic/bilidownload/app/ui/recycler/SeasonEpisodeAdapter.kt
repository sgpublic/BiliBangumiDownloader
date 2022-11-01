package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.ui.ArrayRecyclerAdapter
import io.github.sgpublic.bilidownload.base.ui.MultiSelectableAdapter
import io.github.sgpublic.bilidownload.base.ui.SelectableArrayAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

open class SeasonEpisodeAdapter: SelectableArrayAdapter<ItemSeasonEpisodeBinding, SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemSeasonEpisodeBinding.inflate(inflater, parent, false).also {
        val param = it.root.layoutParams
        param.height = RecyclerView.LayoutParams.MATCH_PARENT
        param.width = RecyclerView.LayoutParams.WRAP_CONTENT
        it.root.layoutParams = param
    }

    /** epid */
    final override fun getItemId(position: Int) = getItem(position).id

    private val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.CHINA)

    @CallSuper
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
        val indexTitle = try {
            Application.getString(R.string.text_episode_index, data.title.apply { toFloat() })
        } catch (_: NumberFormatException) { data.title }
        if (data.longTitle.isBlank()) {
            ViewBinding.episodeIndexTitle.visibility = View.GONE
            ViewBinding.episodeTitle.text = indexTitle
        } else {
            ViewBinding.episodeIndexTitle.text = indexTitle
            ViewBinding.episodeTitle.text = data.longTitle
        }
        if (getSelectedItem().id == data.id) {
            ViewBinding.episodeState.visibility = View.VISIBLE
            ViewBinding.episodeState.setImageResource(R.drawable.ic_episode_playing)
        } else {
            // TODO 显示下载状态
            ViewBinding.episodeState.visibility = View.GONE
        }
    }

    final override fun getClickableView(ViewBinding: ItemSeasonEpisodeBinding) = ViewBinding.itemEpisodeBase

    private var recycler: RecyclerView? = null
    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recycler = recyclerView
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        recycler = null
    }

    private val indexTmp: HashMap<Long, Int> = HashMap()
    fun setSelectedEpid(epid: Long) {
        setSelection(indexTmp[epid] ?: -1)
    }
    fun getPosition(epid: Long): Int = indexTmp[epid] ?: 0

    @CallSuper
    override fun setData(list: Collection<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>) {
        indexTmp.clear()
        super.setData(list)
        list.forEachIndexed { index, data ->
            indexTmp[data.id] = index
        }
    }
}