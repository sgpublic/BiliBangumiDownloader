package io.github.sgpublic.bilidownload.ui.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.activity.SeasonPlayer
import io.github.sgpublic.bilidownload.databinding.RecyclerHomeBannerBinding
import io.github.sgpublic.bilidownload.ui.SeasonBannerAdapter

class HomeRecyclerAdapter(private val context: AppCompatActivity) : FollowsRecyclerAdapter(context) {
    val TYPE_BANNER = 1

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_BANNER
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_BANNER) {
            return BannerViewHolder(RecyclerHomeBannerBinding.inflate(
                LayoutInflater.from(context), parent, false
            ))
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BannerViewHolder) {
            onBindBannerViewHolder(holder)
            return
        }
        if (holder is FooterViewHolder) {
            val lp = holder.binding.root.layoutParams as GridLayoutManager.LayoutParams
            lp.bottomMargin = Application.dip2px(56f)
        }
        super.onBindViewHolder(holder, position - 1)
    }

    private fun onBindBannerViewHolder(holder: BannerViewHolder) {
        if (bannerData.isEmpty() || holder.hasLoad) {
            return
        }
        (holder.binding.bangumiBanner.layoutParams as GridLayoutManager.LayoutParams)
            .topMargin = Application.dip2px(110f)
        val viewHolder = holder.binding.bangumiBanner
            .setHolderCreator {
                SeasonBannerAdapter(this@HomeRecyclerAdapter.context)
            }
            .setOnPageClickListener {
                val data = bannerData[it]
                SeasonPlayer.startActivity(context, data.seasonId)
            }
        viewHolder.create(bannerData)
        if (canLoop) {
            viewHolder.startLoop()
        } else {
            viewHolder.stopLoop()
        }
    }

    override fun removeAllFollows() {
        val size = follows.size
        follows.clear()
        pages = 0
        notifyItemRangeRemoved(1, size)
    }

    override fun getItemCount(): Int = follows.size + 2

    override fun getSpanSize(position: Int): Int {
        if (position == 0) {
            return 3
        }
        return super.getSpanSize(position)
    }

    private val bannerData = arrayListOf<SeasonBannerAdapter.BannerItem>()
    fun setBannerData(list: ArrayList<SeasonBannerAdapter.BannerItem>) {
        bannerData.clear()
        bannerData.addAll(list)
        notifyItemChanged(0)
    }

    private var canLoop = true
    fun setBannerLoop(canLoop: Boolean) {
        this.canLoop = canLoop
        notifyItemChanged(0)
    }

    class BannerViewHolder(val binding: RecyclerHomeBannerBinding, var hasLoad: Boolean = false)
        : RecyclerView.ViewHolder(binding.root)
}