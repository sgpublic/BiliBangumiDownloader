package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arialyy.annotations.DownloadGroup
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.task.DownloadGroupTask
import io.github.sgpublic.bilidownload.app.ui.recycler.DownloadEpisodeAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.DownloadEpisodeModel
import io.github.sgpublic.bilidownload.base.ui.BaseRecyclerActivity
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity
import io.github.sgpublic.bilidownload.databinding.ActivityRecyclerBinding
import io.github.sgpublic.bilidownload.databinding.ItemDownloadEpisodeBinding

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 10:53
 */
class DownloadEpisodeList: BaseRecyclerActivity<DownloadTaskEntity, ItemDownloadEpisodeBinding, DownloadEpisodeModel>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        Aria.download(this).register()
    }

    override fun onViewSetup() {
        super.onViewSetup()
        setSupportActionBar(ViewBinding.recyclerToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra(KEY_SEASON_TITLE) ?: ""
        }

        Adapter.setOnItemClickListener {
            when (it.status) {
                DownloadTaskEntity.Status.Error -> {
                    it.status = DownloadTaskEntity.Status.Waiting
                    it.isRetry = true
                    ViewModel.saveTask(it)
                }
                DownloadTaskEntity.Status.Processing,
                DownloadTaskEntity.Status.Waiting -> {
                    Aria.download(this)
                        .loadGroup(it.taskId)
                        .save()
                }
                DownloadTaskEntity.Status.Paused -> {
                    Aria.download(this)
                        .loadGroup(it.taskId)
                        .resume()
                }
                DownloadTaskEntity.Status.Finished -> {
                    DownloadPlayer.startActivity(this, it.sid, it.epid)
                }
                else -> { }
            }
        }
    }

    override fun onViewModelSetup() {
        ViewModel.EpisodeTasks.observe(this) {
            Adapter.setData(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            @Suppress("DEPRECATION")
            onBackPressed()
            return true
        }
        return false
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (Adapter.isSelectMode()) {
            Adapter.cancelSelectMode()
            return
        }
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }

    @DownloadGroup.onTaskRunning
    fun onTaskRunning(task: DownloadGroupTask) {
        Adapter.setRunningTask(task)
    }

    override fun onDestroy() {
        Aria.download(this).unRegister()
        super.onDestroy()
    }

    override val Adapter by lazy { DownloadEpisodeAdapter() }

    override val ViewBinding: ActivityRecyclerBinding by viewBinding()

    override val ViewModel: DownloadEpisodeModel by viewModels {
        ViewModelFactory(intent.getLongExtra(KEY_SEASON_ID, -1))
    }

    private class ViewModelFactory(
        private val sid: Long,
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Long::class.java)
                .newInstance(sid)
        }
    }

    companion object {
        const val KEY_SEASON_ID = "season_id"
        const val KEY_SEASON_TITLE = "season_title"

        fun startActivity(origin: Context, sid: Long, title: String) {
            origin.startActivity(Intent(origin, DownloadEpisodeList::class.java).also {
                it.putExtra(KEY_SEASON_ID, sid)
                it.putExtra(KEY_SEASON_TITLE, title)
            })
        }
    }
}