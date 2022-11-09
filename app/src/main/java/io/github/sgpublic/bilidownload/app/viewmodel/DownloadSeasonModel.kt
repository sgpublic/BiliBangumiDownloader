package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.lifecycle.MutableLiveData
import io.github.sgpublic.bilidownload.app.ui.recycler.DownloadSeasonAdapter
import io.github.sgpublic.bilidownload.base.ui.BaseRecyclerActivity

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 11:07
 */
class DownloadSeasonModel: BaseRecyclerActivity.ArrayViewModel<DownloadSeasonAdapter.SeasonTaskGroup>() {
    override val Data: MutableLiveData<List<DownloadSeasonAdapter.SeasonTaskGroup>> by lazy {
        MutableLiveData()
    }
}