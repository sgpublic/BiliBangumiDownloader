package io.github.sgpublic.bilidownload.app.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.forest.data.SeasonRecommendResp
import io.github.sgpublic.bilidownload.core.util.customLoad
import io.github.sgpublic.bilidownload.core.util.withCrossFade
import io.github.sgpublic.bilidownload.core.util.withHorizontalPlaceholder
import io.github.sgpublic.bilidownload.core.util.withVerticalPlaceholder
import io.github.sgpublic.bilidownload.databinding.ItemSearchEpisodeBinding
import io.github.sgpublic.bilidownload.databinding.ItemSearchSeasonBinding
import kotlin.math.roundToInt

class RecommendAdapter(private val context: AppCompatActivity)
    : ArrayAdapter<SeasonRecommendResp.SeasonRecommend.Card>(context, R.layout.item_search_episode, ArrayList()) {
    private var onResourceClick: (String) -> Unit = { }
    fun setOnResourceItemClickListener(onClick: (String) -> Unit) {
        this.onResourceClick = onClick
    }
    private var onEpisodeClick: (Long, Long?) -> Unit = { _, _ -> }
    fun setOnEpisodeItemClickListener(onClick: (Long, Long?) -> Unit) {
        this.onEpisodeClick = onClick
    }

    private val list: ArrayList<SeasonRecommendResp.SeasonRecommend.Card> = ArrayList()
    override fun getItem(position: Int): SeasonRecommendResp.SeasonRecommend.Card {
        return list[position]
    }

    override fun getCount(): Int {
        return list.size
    }

    fun setData(list: Collection<SeasonRecommendResp.SeasonRecommend.Card>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val data = getItem(position)
        return when (data.type) {
            1 -> getSeasonView(parent, data.season ?: throw IllegalStateException("推荐类型为番剧，但并未返回数据"))
            2 -> getResourceView(parent, data.resource ?: throw IllegalStateException("推荐类型为资源，但并未返回数据"))
            else -> throw IllegalArgumentException("未知推荐 type：${data.type}")
        }
    }

    private fun getSeasonView(parent: ViewGroup, data: SeasonRecommendResp.SeasonRecommend.Card.Season): View {
        val binding = ItemSearchSeasonBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.itemSearchSeasonTitle.text = data.title
        if ((data.badgeInfo?.text ?: "") == "") {
            binding.itemSeasonBadges.visibility = View.GONE
        } else {
            binding.itemSeasonBadges.visibility = View.VISIBLE
            binding.itemSeasonBadges.text = data.badgeInfo!!.text
        }
        if ((data.rating?.score ?: 0f) == 0f) {
            binding.itemSearchRatingNull.visibility = View.VISIBLE
            binding.itemSearchRatingString.visibility = View.INVISIBLE
            binding.itemSearchRatingStar.progress = 0
        } else {
            binding.itemSearchRatingString.text = String.format("%.1f", data.rating!!.score)
            binding.itemSearchRatingStar.progress = data.rating!!.score.roundToInt()
            binding.itemSearchRatingNull.visibility = View.INVISIBLE
            binding.itemSearchRatingString.visibility = View.VISIBLE
        }
        binding.itemSearchSeasonContent.text = data.newEp.indexShow
        binding.root.setOnClickListener {
            onEpisodeClick.invoke(data.seasonId ?: return@setOnClickListener, data.episodeId)
        }
        Glide.with(context)
            .customLoad(data.cover)
            .withVerticalPlaceholder()
            .withCrossFade()
            .centerCrop()
            .into(binding.itemSearchSeasonCover)
        return binding.root
    }

    private fun getResourceView(parent: ViewGroup, data: SeasonRecommendResp.SeasonRecommend.Card.Resource): View {
        val binding = ItemSearchEpisodeBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.itemEpisodeBadges.text = data.label
        binding.itemSearchEpisodeTitle.text = data.title
        binding.itemSearchEpisodeFrom.text = data.desc
        Glide.with(context)
            .customLoad(data.cover)
            .withHorizontalPlaceholder()
            .withCrossFade()
            .into(binding.itemSearchEpisodeCover)
        binding.root.setOnClickListener {
            onResourceClick.invoke(data.reValue)
        }
        return binding.root
    }
}