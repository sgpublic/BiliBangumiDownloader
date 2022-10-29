package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.app.ui.list.SeriesListAdapter
import io.github.sgpublic.bilidownload.base.ui.ViewBindingHolder
import io.github.sgpublic.bilidownload.base.ui.ViewBindingRecyclerAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp.SeasonInfoData
import io.github.sgpublic.bilidownload.core.forest.data.SeasonRecommendResp
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ItemSearchEpisodeBinding
import io.github.sgpublic.bilidownload.databinding.ItemSearchSeasonBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerSeasonRecommendBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerSeasonTitleBinding
import kotlin.math.max
import kotlin.math.roundToInt

/**
 *
 * @author Madray Haven
 * @date 2022/10/28 12:58
 */
class SeasonOnlinePageAdapter: ViewBindingRecyclerAdapter() {
    private val recommendList: ArrayList<SeasonRecommendResp.SeasonRecommend.Card> = ArrayList()
    fun setRecommend(list: Collection<SeasonRecommendResp.SeasonRecommend.Card>) {
        val size = max(list.size, recommendList.size)
        recommendList.clear()
        recommendList.addAll(list)
        if (size < list.size && size != 0) {
            notifyItemRangeInserted(size + 1, list.size - size)
        } else {
            notifyItemRangeChanged(1, size + 1)
            if (size > list.size) {
                notifyItemRangeRemoved(size + 1, size - list.size)
            }
        }
    }
    private var onResourceClick: (String) -> Unit = { }
    fun setOnResourceItemClickListener(onClick: (String) -> Unit) {
        this.onResourceClick = onClick
    }
    private var onEpisodeClick: (Long, Long?) -> Unit = { _, _ -> }
    fun setOnEpisodeItemClickListener(onClick: (Long, Long?) -> Unit) {
        this.onEpisodeClick = onClick
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TypeTitleHolder
        }
        if (recommendList.isEmpty()) {
            throw IllegalStateException("recommend list is empty, but recyclerview try to load it.")
        }
        if (position == 1) {
            return TypeRecommendTitleHolder
        }
        return when (recommendList[position - 2].type) {
            1 -> TypeRecommendSeasonHolder
            2 -> TypeRecommendResourceHolder
            else -> throw IllegalArgumentException("unknown recommend type")
        }
    }

    private var season: SeasonInfoData? = null
    fun setSeasonInfo(season: SeasonInfoData) {
        this.season = season
    }
    private val seriesAdapter: SeriesListAdapter by lazy { SeriesListAdapter() }
    fun setSeries(list: Collection<SeasonInfoData.Seasons.SeasonData.SeasonItem>) {
        seriesAdapter.setData(list)
    }
    private val episodeAdapter: SeasonEpisodeAdapter by lazy { SeasonEpisodeAdapter() }
    fun setEpisode(list: Collection<SeasonInfoData.Episodes.EpisodesData.EpisodesItem>) {
        episodeAdapter.setData(list)
    }
    private var seasonDetailClick: () -> Unit = { }
    fun setOnSeasonDetailClick(onClick: () -> Unit) {
        seasonDetailClick = onClick
    }
    private var choseEpisodeClick: () -> Unit = { }
    fun setOnChoseEpisodeClick(onClick: () -> Unit) {
        choseEpisodeClick = onClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TypeTitleHolder -> TitleHolder(RecyclerSeasonTitleBinding.inflate(
                inflater, parent, false
            ))
            TypeRecommendTitleHolder -> RecommendTitleHolder(RecyclerSeasonRecommendBinding.inflate(
                inflater, parent, false
            ))
            TypeRecommendResourceHolder -> RecommendResourceHolder(ItemSearchEpisodeBinding.inflate(
                inflater, parent, false
            ))
            TypeRecommendSeasonHolder -> RecommendSeasonHolder(ItemSearchSeasonBinding.inflate(
                inflater, parent, false
            ))
            else -> throw IllegalArgumentException("no viewType as $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewBindingHolder<*>, position: Int) {
        if (holder is TitleHolder) {
            onBindTitleBinding(holder.ViewBinding)
            return
        }
        if (position < 2) {
            return
        }
        val data = recommendList[position - 2]
        when (holder) {
            is RecommendSeasonHolder -> onBindRecommendSeasonHolder(holder.context, holder.ViewBinding, data.season!!)
            is RecommendResourceHolder -> onBindRecommendResourceHolder(holder.context, holder.ViewBinding, data.resource!!)
        }
    }

    private fun onBindTitleBinding(ViewBinding: RecyclerSeasonTitleBinding) {
        if (ViewBinding.seasonSeries.adapter == null) {
            ViewBinding.seasonSeries.adapter = seriesAdapter
        }
        seriesAdapter.isEmpty.take(View.GONE, View.VISIBLE).let { visibility ->
            ViewBinding.seasonSeriesTitle.visibility = visibility
            ViewBinding.seasonSeries.visibility = visibility
        }
        if (ViewBinding.seasonEpisodeList.adapter == null) {
            ViewBinding.seasonEpisodeList.adapter = episodeAdapter
        }
        episodeAdapter.isEmpty().take(View.GONE, View.VISIBLE).let { visibility ->
            ViewBinding.seasonChoseEpisode.visibility = visibility
            ViewBinding.seasonChoseEpisodeIc.visibility = visibility
            ViewBinding.seasonChoseEpisodeText.visibility = visibility
            ViewBinding.seasonEpisodeList.visibility = visibility
        }
        season?.let { season ->
            ViewBinding.seasonTitle.text = season.seasonTitle
            ViewBinding.seasonDetail.text = season.typeDesc
            ViewBinding.seasonChoseEpisodeText.text = season.newEp.desc
        }
        ViewBinding.seasonDetailCover.setOnClickListener {
            seasonDetailClick.invoke()
        }
        ViewBinding.seasonChoseEpisode.setOnClickListener {
            choseEpisodeClick.invoke()
        }
    }

    private fun onBindRecommendSeasonHolder(context: Context, ViewBinding: ItemSearchSeasonBinding, data: SeasonRecommendResp.SeasonRecommend.Card.Season) {
        Glide.with(context)
            .customLoad(data.cover)
            .withVerticalPlaceholder()
            .withCrossFade()
            .centerCrop()
            .into(ViewBinding.itemSearchSeasonCover)
        ViewBinding.itemSearchSeasonTitle.text = data.title
        ViewBinding.itemSearchSeasonContent.text = data.newEp.indexShow
        if (data.rating.score == 0f) {
            ViewBinding.itemSearchRatingNull.visibility = View.VISIBLE
            ViewBinding.itemSearchRatingString.visibility = View.INVISIBLE
        } else {
            ViewBinding.itemSearchRatingNull.visibility = View.INVISIBLE
            ViewBinding.itemSearchRatingString.visibility = View.VISIBLE
        }
        ViewBinding.itemSearchSeasonContent.text = data.newEp.indexShow
        ViewBinding.itemSearchRatingString.text = String.format("%.1f", data.rating.score)
        ViewBinding.itemSearchRatingStar.progress = data.rating.score.roundToInt()
        if ((data.badgeInfo?.text ?: "") == "") {
            ViewBinding.itemSeasonBadges.visibility = View.GONE
        } else {
            ViewBinding.itemSeasonBadges.visibility = View.VISIBLE
            ViewBinding.itemSeasonBadges.text = data.badgeInfo!!.text
        }
        ViewBinding.root.setOnClickListener {
            onEpisodeClick.invoke(data.seasonId ?: return@setOnClickListener, data.episodeId)
        }
    }

    private fun onBindRecommendResourceHolder(context: Context, ViewBinding: ItemSearchEpisodeBinding, data: SeasonRecommendResp.SeasonRecommend.Card.Resource) {
        ViewBinding.itemEpisodeBadges.text = data.label
        ViewBinding.itemSearchEpisodeTitle.text = data.title
        ViewBinding.itemSearchEpisodeFrom.text = data.desc
        Glide.with(context)
            .customLoad(data.cover)
            .withHorizontalPlaceholder()
            .withCrossFade()
            .into(ViewBinding.itemSearchEpisodeCover)
        ViewBinding.root.setOnClickListener {
            onResourceClick.invoke(data.reValue)
        }
    }

    override fun getItemCount(): Int {
        return recommendList.isEmpty().take(1, recommendList.size + 2)
    }


    class TitleHolder(binding: RecyclerSeasonTitleBinding): ViewBindingHolder<RecyclerSeasonTitleBinding>(binding)
    class RecommendTitleHolder(binding: RecyclerSeasonRecommendBinding): ViewBindingHolder<RecyclerSeasonRecommendBinding>(binding)
    class RecommendResourceHolder(binding: ItemSearchEpisodeBinding): ViewBindingHolder<ItemSearchEpisodeBinding>(binding)
    class RecommendSeasonHolder(binding: ItemSearchSeasonBinding): ViewBindingHolder<ItemSearchSeasonBinding>(binding)


    companion object {
        const val TypeTitleHolder = 1
        const val TypeRecommendTitleHolder = 2
        const val TypeRecommendResourceHolder = 3
        const val TypeRecommendSeasonHolder = 4
    }
}