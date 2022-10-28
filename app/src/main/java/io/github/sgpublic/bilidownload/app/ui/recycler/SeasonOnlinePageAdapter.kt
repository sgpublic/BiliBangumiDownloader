package io.github.sgpublic.bilidownload.app.ui.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geetest.sdk.utils.l
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp.SeasonInfoData
import io.github.sgpublic.bilidownload.core.forest.data.SeasonRecommendResp
import io.github.sgpublic.bilidownload.core.forest.find
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.databinding.*
import kotlin.math.max

/**
 *
 * @author Madray Haven
 * @date 2022/10/28 12:58
 */
class SeasonOnlinePageAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val seriesList: ArrayList<SeasonInfoData.Seasons.SeasonData.SeasonItem> = ArrayList()
    fun setSeries(list: Collection<SeasonInfoData.Seasons.SeasonData.SeasonItem>) {
        seriesList.clear()
        seriesList.addAll(list)
    }
    private val episodeAdapter: SeasonEpisodeAdapter by lazy {
        SeasonEpisodeAdapter()
    }
    fun setEpisode(list: Collection<SeasonInfoData.Episodes.EpisodesData.EpisodesItem>) {
        episodeAdapter.setData(list)
    }

    private var onResourceClick: (String) -> Unit = { }
    fun setOnResourceItemClickListener(onClick: (String) -> Unit) {
        this.onResourceClick = onClick
    }
    private var onEpisodeClick: (Long, Long) -> Unit = { _, _ -> }
    fun setOnEpisodeItemClickListener(onClick: (Long, Long) -> Unit) {
        this.onEpisodeClick = onClick
    }
    private val recommendList: ArrayList<SeasonRecommendResp.SeasonRecommend.Card> = ArrayList()
    fun setRecommend(list: Collection<SeasonRecommendResp.SeasonRecommend.Card>) {
        var size = max(list.size, recommendList.size)
        recommendList.clear()
        recommendList.addAll(list)
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TypeTitleHolder
        }
        var startIndex = position - 1
        if (episodeAdapter.itemCount > 0) {
            startIndex -= 1
        }
        if (seriesList.isNotEmpty()) {
            startIndex -= 1
        }
        if (recommendList.isEmpty()) {
            throw IllegalStateException("recommend list is empty, but recyclerview try to load it.")
        }
        return if (startIndex == 0) {
            TypeRecommendTitleHolder
        } else {
            when (recommendList[startIndex - 1].type) {
                1 -> TypeRecommendSeasonHolder
                2 -> TypeRecommendResourceHolder
                else -> throw IllegalArgumentException("unknown recommend type")
            }
        }
    }

    private lateinit var titleHolder: TitleHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TypeTitleHolder -> TitleHolder(RecyclerSeasonTitleBinding.inflate(
                inflater, parent, false
            )).also { titleHolder = it }
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        var size = recommendList.size
        if (size > 0) {
            size += 1
        }
        if (seriesList.isNotEmpty()) {
            size += 1
        }
        if (episodeAdapter.itemCount > 0) {
            size += 1
        }
        return size + 1
    }


    class TitleHolder(val binding: RecyclerSeasonTitleBinding): RecyclerView.ViewHolder(binding.root)
    class RecommendTitleHolder(val binding: RecyclerSeasonRecommendBinding): RecyclerView.ViewHolder(binding.root)
    class RecommendResourceHolder(val binding: ItemSearchEpisodeBinding): RecyclerView.ViewHolder(binding.root)
    class RecommendSeasonHolder(val binding: ItemSearchSeasonBinding): RecyclerView.ViewHolder(binding.root)


    companion object {
        const val TypeTitleHolder = 1
        const val TypeRecommendTitleHolder = 4
        const val TypeRecommendResourceHolder = 5
        const val TypeRecommendSeasonHolder = 6
    }
}