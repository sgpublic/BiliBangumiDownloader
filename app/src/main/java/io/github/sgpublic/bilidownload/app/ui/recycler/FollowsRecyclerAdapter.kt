package io.github.sgpublic.bilidownload.app.ui.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.core.forest.data.FollowsResp
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ItemBangumiFollowBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerFooterBinding
import kotlin.collections.ArrayList
import kotlin.math.max

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 9:48
 */
class FollowsRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val follows: ArrayList<FollowsResp.FollowsData.FollowItem> = ArrayList()
    fun setFollowsData(list: Collection<FollowsResp.FollowsData.FollowItem>, hasNext: Boolean) {
        val size = follows.size
        follows.clear()
        follows.addAll(list)
        this.hasNext = hasNext
        if (size < list.size && size != 0) {
            notifyItemRangeInserted(size, list.size - size)
        } else {
            notifyItemRangeChanged(0, size + 1)
            if (size > list.size) {
                notifyItemRangeRemoved(size + 1, size - list.size)
            }
        }
    }

    private var hasNext: Boolean = true

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            follows.size -> TYPE_FOOTER
            else -> TYPE_SEASON
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_FOOTER -> FooterViewHolder(RecyclerFooterBinding.inflate(
                inflater, parent, false
            ))
            else -> SeasonViewHolder(ItemBangumiFollowBinding.inflate(
                inflater, parent, false
            ))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FooterViewHolder -> onBindFooterViewHolder(holder)
            is SeasonViewHolder -> onBindSeasonViewHolder(holder, follows[position])
        }
    }

    private fun onBindFooterViewHolder(holder: FooterViewHolder) {
        val img = holder.binding.root
        img.visibility = (follows.isEmpty()).take(View.INVISIBLE, View.VISIBLE)
        if (hasNext) {
            img.startLoad()
        } else {
            img.loadEmpty()
        }
    }

    private var onEpisodeClickListener: (Long) -> Unit = { _ -> }
    fun setOnEpisodeClickListener(onBannerClickListener: (Long) -> Unit) {
        this.onEpisodeClickListener = onBannerClickListener
    }
    private fun onBindSeasonViewHolder(holder: SeasonViewHolder, data: FollowsResp.FollowsData.FollowItem) {
        holder.binding.followContent.text = data.title
        data.badgeInfo?.let {
            Glide.with(holder.binding.followBadge.context)
                .customLoad(it.img)
                .withCrossFade()
                .constraintInfo(holder.binding.followBadge)
        }
        Glide.with(holder.binding.followImage.context)
            .customLoad(data.cover)
            .withVerticalPlaceholder()
            .withCrossFade()
            .centerCrop()
            .into(holder.binding.followImage)
        holder.binding.root.setOnClickListener {
            onEpisodeClickListener.invoke(data.seasonId)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager as GridLayoutManager
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return (position == follows.size).take(3, 1)
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

    private var onScrollToEndCallback: () -> Unit = { }
    fun setOnScrollToEndListener(callback: () -> Unit) {
        this.onScrollToEndCallback = callback
    }

    override fun getItemCount(): Int = follows.size + 1

    class SeasonViewHolder(val binding: ItemBangumiFollowBinding)
        : RecyclerView.ViewHolder(binding.root)

    class FooterViewHolder(val binding: RecyclerFooterBinding)
        : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TYPE_SEASON = 1
        const val TYPE_FOOTER = 2
    }
}