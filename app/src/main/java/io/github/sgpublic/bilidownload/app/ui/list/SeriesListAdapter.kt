package io.github.sgpublic.bilidownload.app.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.base.ui.ArrayListAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.util.constraintInfo
import io.github.sgpublic.bilidownload.core.util.customLoad
import io.github.sgpublic.bilidownload.core.util.withCrossFade
import io.github.sgpublic.bilidownload.core.util.withVerticalPlaceholder
import io.github.sgpublic.bilidownload.databinding.ItemBangumiFollowBinding
import kotlinx.coroutines.NonDisposableHandle.parent

class SeriesListAdapter : ArrayListAdapter<SeasonInfoResp.SeasonInfoData.Seasons.SeasonData.SeasonItem, ItemBangumiFollowBinding>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemBangumiFollowBinding.inflate(inflater, parent, false)

    override fun onBindView(
        context: Context,
        ViewBinding: ItemBangumiFollowBinding,
        data: SeasonInfoResp.SeasonInfoData.Seasons.SeasonData.SeasonItem
    ) {
        ViewBinding.followContent.text = data.seasonTitle
        if (data.badgeInfo.img != null) {
            Glide.with(context)
                .customLoad(data.badgeInfo.img)
                .withCrossFade()
                .constraintInfo(ViewBinding.followBadge)
        }
        Glide.with(context)
            .customLoad(data.cover)
            .withVerticalPlaceholder()
            .withCrossFade()
            .into(ViewBinding.followImage)
    }

    override fun getClickableView(ViewBinding: ItemBangumiFollowBinding): View {
        return ViewBinding.root
    }
}