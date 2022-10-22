package io.github.sgpublic.bilidownload.app.ui

import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.forest.data.BannerResp.BannerData.BannerItem
import io.github.sgpublic.bilidownload.databinding.ItemBangumiBannerBinding

class SeasonBannerAdapter(private val context: AppCompatActivity) :
    BaseBannerAdapter<BannerItem.Item>() {
    override fun getLayoutId(viewType: Int): Int =
        R.layout.item_bangumi_banner

    override fun bindData(holder: BaseViewHolder<BannerItem.Item>, data: BannerItem.Item, position: Int, pageSize: Int) {
        val binding = ItemBangumiBannerBinding.bind(holder.itemView)
        Glide.with(context)
            .customLoad(data.badgeInfo.img)
            .withCrossFade()
            .into(binding.bannerBadge)
        Glide.with(context)
            .customLoad(data.cover)
            .withHorizontalPlaceholder()
            .withCrossFade()
            .into(binding.bannerImage)
    }
}