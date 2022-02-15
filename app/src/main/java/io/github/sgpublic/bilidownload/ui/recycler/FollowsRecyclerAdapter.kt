package io.github.sgpublic.bilidownload.ui.recycler

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.activity.SeasonPlayer
import io.github.sgpublic.bilidownload.data.FollowData
import io.github.sgpublic.bilidownload.databinding.ItemBangumiFollowBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerFooterBinding
import io.github.sgpublic.bilidownload.ui.addOnReadyListener
import java.util.*

open class FollowsRecyclerAdapter(private val context: AppCompatActivity)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val nightMode: Boolean = context.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    protected val follows: ArrayList<FollowData> = arrayListOf()
    private var hasNext: Boolean = true

    override fun getItemViewType(position: Int): Int {
        return when(position + 1) {
            itemCount -> TYPE_FOOTER
            else -> TYPE_SEASON
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_FOOTER -> {
                FooterViewHolder(RecyclerFooterBinding.inflate(
                    LayoutInflater.from(context), parent, false
                ))
            }
            TYPE_SEASON -> {
                SeasonViewHolder(ItemBangumiFollowBinding.inflate(
                    LayoutInflater.from(context), parent, false
                ))
            }
            else -> throw IllegalStateException()
        }
    }

    private var isGettingMore = false
    private var timer: Timer? = null
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FooterViewHolder) {
            onBindFooterViewHolder(holder)
            return
        }
        if (holder is SeasonViewHolder) {
            onBindSeasonViewHolder(holder, position)
        }
    }

    private fun onBindFooterViewHolder(holder: FooterViewHolder) {
        if (!hasNext) {
            if(pages <= 1) {
                holder.binding.recyclerEnd.visibility = View.INVISIBLE
            }
            holder.binding.recyclerEnd.setImageResource(R.drawable.pic_nomore)
            return
        }
        holder.binding.recyclerEnd.visibility = View.VISIBLE
        if (!isGettingMore) {
            holder.binding.recyclerEnd.setImageResource(R.drawable.pic_search_doing_1)
            return
        }
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            private var imageIndex = 0
            override fun run() {
                imageIndex = if (imageIndex == R.drawable.pic_search_doing_1)
                    R.drawable.pic_search_doing_2 else R.drawable.pic_search_doing_1
                context.runOnUiThread {
                    holder.binding.recyclerEnd.setImageResource(imageIndex)
                }
            }
        }, 0, 500)
    }

    private fun onBindSeasonViewHolder(holder: SeasonViewHolder, position: Int) {
        val data = follows[position]
        holder.binding.followContent.text = data.title
        if (data.badge == "") {
            holder.binding.itemFollowBadgesBackground.visibility = View.GONE
        } else {
            holder.binding.itemFollowBadgesBackground.visibility = View.VISIBLE
            if (nightMode) {
                holder.binding.itemFollowBadgesBackground.setCardBackgroundColor(
                    data.badgeColorNight
                )
            } else {
                holder.binding.itemFollowBadgesBackground.setCardBackgroundColor(data.badgeColor)
            }
            holder.binding.itemFollowBadges.text = data.badge
        }
        holder.binding.root.setOnClickListener {
            SeasonPlayer.startActivity(context, data.seasonId)
        }
        if (holder.hasLoad) {
            holder.binding.followImage.visibility = View.VISIBLE
            return
        }
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.pic_doing_v)
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(context)
            .load(data.cover)
            .apply(requestOptions)
            .addOnReadyListener {
                holder.binding.followImage.visibility = View.VISIBLE
                holder.binding.followImage.animate().alpha(1f)
                    .setDuration(400).setListener(null)
            }
            .into(holder.binding.followImage)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager as GridLayoutManager
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return this@FollowsRecyclerAdapter.getSpanSize(position)
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
                    isGettingMore = true
                    onScrollToEndCallback(pages + 1)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isSlidingUpward = dy > 0
            }
        })
    }

    protected open fun getSpanSize(position: Int): Int {
        return when(position + 1) {
            itemCount -> 3
            else -> 1
        }
    }

    private var onScrollToEndCallback: (Int) -> Unit? = { }
    fun setOnScrollToEndListener(callback: (Int) -> Unit) {
        this.onScrollToEndCallback = callback
    }

    protected var pages: Int = 0
    fun insertFollowData(newData: List<FollowData>, hasNext: Boolean) {
        this.hasNext = hasNext
        pages++
        val preSize = follows.size
        follows.addAll(newData)
        isGettingMore = false
        timer?.cancel()
        timer = null
        notifyItemRangeChanged(preSize, newData.size + 1)
    }

    open fun removeAllFollows() {
        val size = follows.size
        follows.clear()
        pages = 0
        notifyItemRangeRemoved(0, size)
    }

    override fun getItemCount(): Int = follows.size + 1

    class SeasonViewHolder(val binding: ItemBangumiFollowBinding, var hasLoad: Boolean = false)
        : RecyclerView.ViewHolder(binding.root)

    class FooterViewHolder(val binding: RecyclerFooterBinding)
        : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TYPE_SEASON = 2
        const val TYPE_FOOTER = 3
    }
}