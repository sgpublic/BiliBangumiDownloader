package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.base.ui.ArrayRecyclerAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.util.customLoad
import io.github.sgpublic.bilidownload.core.util.withCrossFade
import io.github.sgpublic.bilidownload.core.util.withVerticalPlaceholder
import io.github.sgpublic.bilidownload.databinding.ItemSeasonCelebrityBinding

/**
 *
 * @author Madray Haven
 * @date 2022/10/28 9:58
 */
class SeasonCelebrityAdapter(
    data: List<SeasonInfoResp.SeasonInfoData.Celebrity>
): ArrayRecyclerAdapter<ItemSeasonCelebrityBinding, SeasonInfoResp.SeasonInfoData.Celebrity>(data) {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemSeasonCelebrityBinding.inflate(inflater, parent, false)

    override fun onBindViewHolder(
        context: Context,
        ViewBinding: ItemSeasonCelebrityBinding,
        data: SeasonInfoResp.SeasonInfoData.Celebrity
    ) {
        Glide.with(context)
            .customLoad(data.avatar)
            .withCrossFade()
            .withVerticalPlaceholder()
            .into(ViewBinding.itemSeasonCelebrityAvatar)
        ViewBinding.itemSeasonCelebrityName.text = data.name
        ViewBinding.itemSeasonCelebrityDesc.text = data.shortDesc
    }
}