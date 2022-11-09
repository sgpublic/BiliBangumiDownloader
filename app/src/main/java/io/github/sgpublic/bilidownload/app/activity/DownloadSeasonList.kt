package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.activity.viewModels
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.ui.recycler.DownloadSeasonAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.DownloadSeasonModel
import io.github.sgpublic.bilidownload.base.ui.BaseRecyclerActivity
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity
import io.github.sgpublic.bilidownload.databinding.ItemDownloadSeasonBinding

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 10:53
 */
class DownloadSeasonList: BaseRecyclerActivity<DownloadSeasonAdapter.SeasonTaskGroup, ItemDownloadSeasonBinding, DownloadSeasonModel>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        super.onViewSetup()
        setSupportActionBar(ViewBinding.recyclerToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.text_download)
        }
        Adapter.setOnItemClickListener {
            DownloadEpisodeList.startActivity(this, it.seasonId, it.seasonTitle)
        }
    }

    override fun onViewModelSetup() {
        ViewModel.SeasonTasks.observe(this) {
            val data = LinkedHashMap<Long, DownloadSeasonAdapter.SeasonTaskGroup>()
            for (entity in it) {
                if (!data.containsKey(entity.sid)) {
                    data[entity.sid] = DownloadSeasonAdapter.SeasonTaskGroup(
                        entity.seasonTitle, entity.seasonCover, entity.sid
                    )
                }
                data[entity.sid]!!.let { item ->
                    item.totalCount += 1
                    when (entity.status) {
                        DownloadTaskEntity.Status.Waiting,
                        DownloadTaskEntity.Status.Retry,
                        DownloadTaskEntity.Status.Processing -> {
                            item.runningCount += 1
                        }
                        DownloadTaskEntity.Status.Finished -> {
                            item.finishedCount += 1
                        }
                        else -> { }
                    }
                }
            }
            Adapter.setData(ArrayList(data.values))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    override val Adapter = DownloadSeasonAdapter()
    override val ViewModel: DownloadSeasonModel by viewModels()

    companion object {
        fun startActivity(origin: Context) {
            origin.startActivity(Intent(origin, DownloadSeasonList::class.java))
        }
    }
}