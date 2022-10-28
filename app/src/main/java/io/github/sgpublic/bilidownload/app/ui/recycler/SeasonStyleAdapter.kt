package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.sgpublic.bilidownload.base.ui.ArrayRecyclerAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.databinding.ItemSeasonStyleBinding

/**
 *
 * @author Madray Haven
 * @date 2022/10/28 9:19
 */
class SeasonStyleAdapter(
    data: List<SeasonInfoResp.SeasonInfoData.Style>,
    private val onClick: (String) -> Unit,
): ArrayRecyclerAdapter<ItemSeasonStyleBinding, SeasonInfoResp.SeasonInfoData.Style>(data) {
    init {
        setOnItemClickListener {
            onClick.invoke(it.url)
        }
    }

    override fun onBindViewHolder(
        context: Context,
        ViewBinding: ItemSeasonStyleBinding,
        data: SeasonInfoResp.SeasonInfoData.Style
    ) {
        ViewBinding.itemSeasonStyleTitle.text = data.name
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup): ItemSeasonStyleBinding {
        return ItemSeasonStyleBinding.inflate(inflater, parent, false)
    }
}