package io.github.sgpublic.bilidownload.app.ui

import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.forest.data.BannerResp.BannerData.BannerItem
import io.github.sgpublic.bilidownload.core.util.customLoad
import io.github.sgpublic.bilidownload.core.util.fittedInfo
import io.github.sgpublic.bilidownload.core.util.withCrossFade
import io.github.sgpublic.bilidownload.core.util.withHorizontalPlaceholder
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
            .fittedInfo(binding.bannerBadge)
        Glide.with(context)
            .customLoad(data.cover)
            .withHorizontalPlaceholder()
            .withCrossFade()
            .into(binding.bannerImage)
        binding.bannerContent.text = data.title
    }
}