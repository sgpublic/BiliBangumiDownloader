package io.github.sgpublic.bilidownload.ui

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.databinding.ItemBangumiBannerBinding

class SeasonBannerAdapter(private val context: AppCompatActivity) :
    BaseBannerAdapter<SeasonBannerAdapter.BannerItem>() {
    override fun getLayoutId(viewType: Int): Int =
        R.layout.item_bangumi_banner

    override fun bindData(holder: BaseViewHolder<BannerItem>, data: BannerItem, position: Int, pageSize: Int) {
        val binding = ItemBangumiBannerBinding.bind(holder.itemView)
        if (data.badge == "") {
            binding.itemBannerBadgeBackground.visibility = View.GONE
        } else {
            binding.itemBannerBadgeBackground.visibility = View.VISIBLE
            binding.itemBannerBadgeBackground.setCardBackgroundColor(data.badgeColor)
            binding.itemBannerBadge.text = data.badge
        }
        if (data.bannerPath == "") {
            Glide.with(context)
                .customLoad(data.seasonCover)
                .withVerticalPlaceholder()
                .withCrossFade()
                .into(binding.bannerImageForeground)
            Glide.with(context)
                .customLoad(data.seasonCover)
                .withCrossFade()
                .withBlur()
                .into(binding.bannerImage)
        } else {
            Glide.with(context)
                .customLoad(data.bannerPath)
                .withHorizontalPlaceholder()
                .withCrossFade()
                .into(binding.bannerImage)
        }
        binding.bannerContent.text = data.indicatorText
    }

    class BannerItem(
        val bannerPath: String,
        val seasonCover: String,
        val seasonId: Long,
        val title: String,
        private val indicator: String,
        val badge: String,
        val badgeColor: Int
    ) {
        val indicatorText: String get() = "$title：$indicator"

        override fun equals(other: Any?): Boolean {
            if (other is BannerItem) {
                return other.seasonId == seasonId
            }
            return false
        }

        override fun hashCode(): Int {
            return seasonId.hashCode()
        }
    }
}