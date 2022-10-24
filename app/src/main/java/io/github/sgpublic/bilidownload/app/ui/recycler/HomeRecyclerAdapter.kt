package io.github.sgpublic.bilidownload.app.ui.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhpan.bannerview.BannerViewPager
import io.github.sgpublic.bilidownload.app.activity.SeasonPlayer
import io.github.sgpublic.bilidownload.app.ui.SeasonBannerAdapter
import io.github.sgpublic.bilidownload.core.forest.data.BangumiPageResp
import io.github.sgpublic.bilidownload.core.forest.data.BangumiPageResp.BangumiPageData
import io.github.sgpublic.bilidownload.core.forest.data.BannerResp
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ItemBangumiEpisodeBinding
import io.github.sgpublic.bilidownload.databinding.ItemBangumiPgcBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerFooterBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerHomeBannerBinding
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 * @author Madray Haven
 * @date 2022/10/22 17:20
 */
class HomeRecyclerAdapter(
    private val context: AppCompatActivity,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    private var onBannerClickListener: (BannerResp.BannerData.BannerItem.Item) -> Unit = { }
    fun setOnBannerClickListener(onBannerClickListener: (BannerResp.BannerData.BannerItem.Item) -> Unit) {
        this.onBannerClickListener = onBannerClickListener
    }

    private var bannerView: WeakReference<BannerViewPager<in Any>>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            TYPE_BANNER -> BannerViewHolder(RecyclerHomeBannerBinding.inflate(
                inflater, parent, false
            )).also {
                val holder = it.binding.bangumiBanner
                    .setAdapter(adapter)
                    .setOnPageClickListener { _, position ->
                        val data = bannerData[position]
                        onBannerClickListener.invoke(data)
                    }
                bannerView = WeakReference(holder)
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

    private val adapter = SeasonBannerAdapter(context)
    private fun onBindBannerViewHolder(holder: BannerViewHolder) {
        val param = holder.binding.bangumiBanner.layoutParams as GridLayoutManager.LayoutParams
        param.topMargin = 110.dp
        val banner = bannerView?.get() ?: return
        banner.create(bannerData)
        if (canLoop.get()) {
            banner.startLoop()
        } else {
            banner.stopLoop()
        }
    }

    private var hasNext: Boolean = true
    private fun onBindFooterViewHolder(holder: FooterViewHolder) {
        val img = holder.binding.root
        img.visibility = (bangumiData.isEmpty()).take(View.INVISIBLE, View.VISIBLE)
        if (hasNext) {
            img.startLoad()
        } else {
            img.loadEmpty()
        }
    }

    private var onEpisodeClickListener: (BannerResp.BannerData.BannerItem.Item) -> Unit = { }
    fun setOnEpisodeClickListener(onBannerClickListener: (BannerResp.BannerData.BannerItem.Item) -> Unit) {
        this.onBannerClickListener = onBannerClickListener
    }
    private fun onBindEpisodeViewHolder(holder: EpisodeViewHolder, item: BangumiPageData.DoubleFeed.Item) {
        Glide.with(context)
            .customLoad(item.cover)
            .withCrossFade()
            .into(holder.binding.itemBangumiEpisodeCover)
        item.badgeInfo?.let {
            Glide.with(context)
                .customLoad(it.img)
                .withCrossFade()
                .fittedInfo(holder.binding.itemBangumiEpisodeBadge)
        }
        holder.binding.itemBangumiEpisodeTag.text = item.bottomLeftBadge.text
        holder.binding.itemBangumiEpisodeDesc.text = item.desc
        holder.binding.itemBangumiEpisodeTitle.text = item.title
        holder.binding.itemBangumiEpisodeStatus.text = item.stat.followView
    }

    private fun onBindPgcViewHolder(holder: PgcViewHolder, item: BangumiPageData.FallFeed.Item) {

    }

    override fun getItemCount(): Int = bangumiData.size + 2

    private val bannerData = ArrayList<BannerResp.BannerData.BannerItem.Item>()
    fun setBannerData(list: Collection<BannerResp.BannerData.BannerItem.Item>) {
//        val size = bannerData.size.coerceAtLeast(list.size)
        bannerData.clear()
        bannerData.addAll(list)
        if (bannerData.isEmpty()) {
            log.warn("bannerData is empty!")
        } else {
            log.debug("bannerData: ${bannerData.toGson()}")
        }
        notifyItemChanged(0)
    }

    private val bangumiData = ArrayList<BangumiPageData.AbstractFeed<*>>()
    fun setBangumiData(list: Collection<BangumiPageData.AbstractFeed<*>>, hasNext: Boolean) {
        val size = bangumiData.size.coerceAtLeast(list.size)
        bangumiData.clear()
        bangumiData.addAll(list)
        if (bannerData.isEmpty()) {
            log.warn("bangumiData is empty!")
        } else {
            log.debug("bangumiData: ${bangumiData.toGson()}")
        }
        this.hasNext = hasNext
        notifyItemChanged(1, size + 2)
    }

    private var onScrollToEndCallback: () -> Boolean = { false }
    fun setOnScrollToEndListener(callback: () -> Boolean) {
        this.onScrollToEndCallback = callback
    }

    private var recyclerView: WeakReference<RecyclerView>? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = WeakReference(recyclerView)
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

                if (lastItemPosition == itemCount - 1 &&
                    isSlidingUpward && hasNext) {
                    onScrollToEndCallback.invoke()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isSlidingUpward = dy > 0
            }
        })
    }

    private var canLoop = AtomicBoolean(true)
    fun setBannerLoop(canLoop: Boolean) {
        this.canLoop.set(canLoop)
        val banner = bannerView?.get() ?: return
        if (canLoop) {
            banner.startLoop()
        } else {
            banner.stopLoop()
        }
    }

    class BannerViewHolder(val binding: RecyclerHomeBannerBinding)
        : RecyclerView.ViewHolder(binding.root)

    class PgcViewHolder(val binding: ItemBangumiPgcBinding)
        : RecyclerView.ViewHolder(binding.root)

    class EpisodeViewHolder(val binding: ItemBangumiEpisodeBinding)
        : RecyclerView.ViewHolder(binding.root)

    class FooterViewHolder(val binding: RecyclerFooterBinding)
        : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TYPE_BANNER = 1
        const val TYPE_PGC = 2
        const val TYPE_EPISODE = 3
        const val TYPE_FOOTER = 4
    }
}