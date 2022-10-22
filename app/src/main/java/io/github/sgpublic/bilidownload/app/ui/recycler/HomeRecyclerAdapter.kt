package io.github.sgpublic.bilidownload.app.ui.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.sgpublic.bilidownload.app.activity.SeasonPlayer
import io.github.sgpublic.bilidownload.app.ui.SeasonBannerAdapter
import io.github.sgpublic.bilidownload.core.forest.data.BannerResp
import io.github.sgpublic.bilidownload.core.util.dp
import io.github.sgpublic.bilidownload.databinding.ItemBangumiFollowBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerFooterBinding
import io.github.sgpublic.bilidownload.databinding.RecyclerHomeBannerBinding

/**
 *
 * @author Madray Haven
 * @date 2022/10/22 17:20
 */
class HomeRecyclerAdapter(
    private val context: AppCompatActivity,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_BANNER
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            TYPE_BANNER -> BannerViewHolder(RecyclerHomeBannerBinding.inflate(
                inflater, parent, false
            ))
            TYPE_FOOTER -> FooterViewHolder(RecyclerFooterBinding.inflate(
                inflater, parent, false
            ))
            else -> SeasonViewHolder(ItemBangumiFollowBinding.inflate(
                inflater, parent, false
            ))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BannerViewHolder) {
            onBindBannerViewHolder(holder)
            return
        }
        if (holder is FooterViewHolder) {
            val lp = holder.binding.root.layoutParams as GridLayoutManager.LayoutParams
            lp.bottomMargin = 56.dp
        }
    }

    private val adapter = SeasonBannerAdapter(this@HomeRecyclerAdapter.context)
    private fun onBindBannerViewHolder(holder: BannerViewHolder) {
        if (bannerData.isEmpty()) {
            return
        }
        (holder.binding.bangumiBanner.layoutParams
                as GridLayoutManager.LayoutParams).topMargin = 110.dp
        val viewHolder = holder.binding.bangumiBanner
            .setAdapter(adapter)
            .setOnPageClickListener { _, position ->
                val data = bannerData[position]
                SeasonPlayer.startActivity(context, data.seasonId)
            }
        viewHolder.create(bannerData)
        if (canLoop) {
            viewHolder.startLoop()
        } else {
            viewHolder.stopLoop()
        }
    }

    override fun getItemCount(): Int = 0

    private val bannerData = arrayListOf<BannerResp.BannerData.BannerItem.Item>()
    fun setBannerData(list: Collection<BannerResp.BannerData.BannerItem.Item>) {
        bannerData.clear()
        bannerData.addAll(list)
        notifyItemChanged(0)
    }

    private var canLoop = true
    fun setBannerLoop(canLoop: Boolean) {
        this.canLoop = canLoop
        notifyItemChanged(0)
    }

    class BannerViewHolder(val binding: RecyclerHomeBannerBinding)
        : RecyclerView.ViewHolder(binding.root)

    class SeasonViewHolder(val binding: ItemBangumiFollowBinding)
        : RecyclerView.ViewHolder(binding.root)

    class FooterViewHolder(val binding: RecyclerFooterBinding)
        : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TYPE_BANNER = 1
        const val TYPE_BILLING_SEASON = 2
        const val TYPE_BILLING_VIDEO = 3
        const val TYPE_SEASON = 4
        const val TYPE_FOOTER = 5
    }
}