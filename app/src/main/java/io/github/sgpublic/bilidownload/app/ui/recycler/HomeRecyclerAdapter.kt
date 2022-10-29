package io.github.sgpublic.bilidownload.app.ui.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhpan.bannerview.BannerViewPager
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.ui.SeasonBannerAdapter
import io.github.sgpublic.bilidownload.base.ui.ViewBindingHolder
import io.github.sgpublic.bilidownload.core.forest.data.BangumiPageResp.BangumiPageData
import io.github.sgpublic.bilidownload.core.forest.data.BannerResp
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ItemBangumiEpisodeBinding
import io.github.sgpublic.bilidownload.databinding.ItemBangumiPgcBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerFooterBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerHomeBannerBinding
import java.lang.Double.max
import java.lang.ref.WeakReference
import kotlin.math.max

/**
 *
 * @author Madray Haven
 * @date 2022/10/22 17:20
 */
class HomeRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_BANNER
            bangumiData.size + 1 -> TYPE_FOOTER
            else -> when (bangumiData[position - 1].data) {
                is BangumiPageData.FallFeed.Item -> TYPE_PGC
                else -> TYPE_EPISODE
            }
        }
    }

    private lateinit var bannerHolder: BannerViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_BANNER -> BannerViewHolder(RecyclerHomeBannerBinding.inflate(
                inflater, parent, false
            )).also { holder ->
                bannerHolder = holder
                holder.ViewBinding.bangumiBanner.setAdapter(SeasonBannerAdapter().also {
                    it.setOnEpisodeClickListener { sid, epid ->
                        onEpisodeClickListener.invoke(sid, epid)
                    }
                })
            }
            TYPE_FOOTER -> FooterViewHolder(RecyclerFooterBinding.inflate(
                inflater, parent, false
            ))
            TYPE_PGC -> PgcViewHolder(ItemBangumiPgcBinding.inflate(
                inflater, parent, false
            ))
            else -> EpisodeViewHolder(ItemBangumiEpisodeBinding.inflate(
                inflater, parent, false
            ))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BannerViewHolder -> onBindBannerViewHolder(holder)
            is FooterViewHolder -> onBindFooterViewHolder(holder)
            is EpisodeViewHolder -> onBindEpisodeViewHolder(
                holder, bangumiData[position - 1].data as BangumiPageData.DoubleFeed.Item
            )
            is PgcViewHolder -> onBindPgcViewHolder(
                holder, bangumiData[position - 1].data as BangumiPageData.FallFeed.Item
            )
        }
    }

    private fun onBindBannerViewHolder(holder: BannerViewHolder) {
        holder.ViewBinding.bangumiBanner.let {
            val param = it.layoutParams as GridLayoutManager.LayoutParams
            param.topMargin = 110.dp
            if (canLoop) {
                it.startLoop()
            } else {
                it.stopLoop()
            }
        }
    }

    private var hasNext: Boolean = true
    private fun onBindFooterViewHolder(holder: FooterViewHolder) {
        val img = holder.ViewBinding.root
        img.visibility = (bangumiData.isEmpty()).take(View.INVISIBLE, View.VISIBLE)
        if (hasNext) {
            img.startLoad()
        } else {
            img.loadEmpty()
        }
    }

    private var onEpisodeClickListener: (Long, Long?) -> Unit = { _, _ -> }
    fun setOnEpisodeClickListener(onBannerClickListener: (Long, Long?) -> Unit) {
        this.onEpisodeClickListener = onBannerClickListener
    }
    private fun onBindEpisodeViewHolder(holder: EpisodeViewHolder, item: BangumiPageData.DoubleFeed.Item) {
        Glide.with(holder.context)
            .customLoad(item.cover)
            .withCrossFade()
            .into(holder.ViewBinding.itemBangumiEpisodeCover)
        item.badgeInfo?.let {
            Glide.with(holder.context)
                .customLoad(it.img)
                .withCrossFade()
                .constraintInfo(holder.ViewBinding.itemBangumiEpisodeBadge)
        }
        holder.ViewBinding.itemBangumiEpisodeTag.text = item.bottomLeftBadge.text
        holder.ViewBinding.itemBangumiEpisodeDesc.text = item.desc
        holder.ViewBinding.itemBangumiEpisodeTitle.text = item.title
        holder.ViewBinding.itemBangumiEpisodeStatus.text = item.stat.followView
        holder.ViewBinding.root.setOnClickListener {
            onEpisodeClickListener.invoke(item.seasonId ?: return@setOnClickListener, item.episodeId)
        }
    }

    @ColorRes
    private val Colors: Array<Int> = arrayOf(
        R.color.color_random_1,
        R.color.color_random_2,
        R.color.color_random_3,
        R.color.color_random_4,
        R.color.color_random_5,
    )
    private fun onBindPgcViewHolder(holder: PgcViewHolder, item: BangumiPageData.FallFeed.Item) {
        item.badgeInfo?.let {
            Glide.with(holder.context)
                .customLoad(it.img)
                .withCrossFade()
                .constraintInfo(holder.ViewBinding.itemBangumiPgcBadge)
        }
        Glide.with(holder.context)
            .customLoad(item.cover)
            .withCrossFade()
            .into(holder.ViewBinding.itemBangumiPgcCover)
        holder.ViewBinding.itemBangumiPcgDesc.text = item.desc
        holder.ViewBinding.itemBangumiPgcTitle.text = item.title
        holder.ViewBinding.root.setCardBackgroundColor(holder.context.resources.getColor(Colors.random(), holder.context.theme))
        holder.ViewBinding.root.setOnClickListener {
            onEpisodeClickListener.invoke(item.seasonId ?: return@setOnClickListener, item.episodeId)
        }
    }

    override fun getItemCount(): Int = bangumiData.size + 2

    private val bannerData = ArrayList<BannerResp.BannerData.BannerItem.Item>()
    fun setBannerData(list: Collection<BannerResp.BannerData.BannerItem.Item>) {
//        val size = bannerData.size.coerceAtLeast(list.size)
//        adapter.setData(list)
//        adapter.notifyItemRangeChanged(0, size)
        if (list.isEmpty()) {
            log.warn("bannerData is empty!")
            return
        }
        bannerData.clear()
        bannerData.addIf(list) {
            it.seasonId != null
        }
        bannerHolder.ViewBinding.bangumiBanner.create(bannerData)
    }

    private val bangumiData = ArrayList<BangumiPageData.AbstractFeed<*>>()
    fun setBangumiData(list: Collection<BangumiPageData.AbstractFeed<*>>, hasNext: Boolean) {
        val size = bangumiData.size
        bangumiData.clear()
        bangumiData.addAll(list)
        if (bannerData.isEmpty()) {
            log.warn("bangumiData is empty!")
        }
        this.hasNext = hasNext
        if (size < list.size && size != 0) {
            notifyItemRangeInserted(size + 1, list.size - size)
        } else {
            notifyItemRangeChanged(1, size + 1)
            if (size > list.size) {
                notifyItemRangeRemoved(size + 2, size - list.size)
            }
        }
    }

    private var onScrollToEndCallback: () -> Unit = { }
    fun setOnScrollToEndListener(callback: () -> Unit) {
        this.onScrollToEndCallback = callback
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        val manager = recyclerView.layoutManager as GridLayoutManager
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position == 0 || position == bangumiData.size + 1) {
                    return 2
                }
                return bangumiData[position - 1].span
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var isSlidingUpward = false

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    return
                }
                val lastItemPosition = manager.findLastCompletelyVisibleItemPosition()
                val itemCount = manager.itemCount

                if (lastItemPosition == itemCount - 1 && isSlidingUpward && hasNext) {
                    onScrollToEndCallback.invoke()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isSlidingUpward = dy > 0
            }
        })
    }

    private var canLoop = false
    fun setBannerLoop(canLoop: Boolean) {
        if (canLoop == this.canLoop) {
            return
        }
        this.canLoop = canLoop
        notifyItemChanged(0)
    }

    class BannerViewHolder(binding: RecyclerHomeBannerBinding)
        : ViewBindingHolder<RecyclerHomeBannerBinding>(binding)

    class PgcViewHolder(binding: ItemBangumiPgcBinding)
        : ViewBindingHolder<ItemBangumiPgcBinding>(binding)

    class EpisodeViewHolder(binding: ItemBangumiEpisodeBinding)
        : ViewBindingHolder<ItemBangumiEpisodeBinding>(binding)

    class FooterViewHolder(binding: RecyclerFooterBinding)
        : ViewBindingHolder<RecyclerFooterBinding>(binding)

    companion object {
        const val TYPE_BANNER = 1
        const val TYPE_PGC = 2
        const val TYPE_EPISODE = 3
        const val TYPE_FOOTER = 4
    }
}