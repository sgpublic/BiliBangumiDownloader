package io.github.sgpublic.bilidownload.app.activity

import io.github.sgpublic.bilidownload.app.ui.recycler.DownloadSeasonAdapter
import io.github.sgpublic.bilidownload.base.ui.ArrayRecyclerAdapter
import io.github.sgpublic.bilidownload.base.ui.BaseRecyclerActivity
import io.github.sgpublic.bilidownload.databinding.ItemDownloadSeasonBinding

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 10:53
 */
class DownloadSeasonList: BaseRecyclerActivity<DownloadSeasonAdapter.SeasonTaskGroup, ItemDownloadSeasonBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override val Adapter: ArrayRecyclerAdapter<ItemDownloadSeasonBinding, DownloadSeasonAdapter.SeasonTaskGroup> =
        DownloadSeasonAdapter()
}