package io.github.sgpublic.bilidownload.fragment.season

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.task.DownloadGroupTask
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseViewModelFragment
import io.github.sgpublic.bilidownload.core.data.SeriesData
import io.github.sgpublic.bilidownload.core.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.core.util.AriaGroupDownloadListener
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.databinding.FragmentDownloadListBinding
import io.github.sgpublic.bilidownload.room.entity.TaskEntity
import io.github.sgpublic.bilidownload.ui.customLoad
import io.github.sgpublic.bilidownload.ui.recycler.DownloadListAdapter
import io.github.sgpublic.bilidownload.ui.withCrossFade
import io.github.sgpublic.bilidownload.ui.withRound
import io.github.sgpublic.bilidownload.ui.withVerticalPlaceholder
import io.github.sgpublic.bilidownload.viewmodel.DownloadListViewModel

class TaskList(contest: AppCompatActivity)
    : BaseViewModelFragment<FragmentDownloadListBinding, DownloadListViewModel>(contest),
    DownloadListAdapter.Companion.ListCompat, AriaGroupDownloadListener {
    override val ViewModel: DownloadListViewModel by activityViewModels()
    private val dao = Application.DATABASE.TasksDao()
    override val list: LinkedHashMap<SeriesData, LinkedHashMap<EpisodeData, MutableLiveData<DownloadListAdapter.TaskInfo>>> = LinkedHashMap()

    private val adapter = DownloadListAdapter(context, this)
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        for (task in dao.getAll()) {
            val series = task.entry.toSeriesData()
            list.putIfAbsent(series, LinkedHashMap())
            val episode = task.entry.toEpisodeData()
            if (!list[series]!!.containsKey(episode)) {
                list[series]!![episode] = MutableLiveData()
            }
        }
        ViewBinding.root.adapter = adapter
    }


    override fun onTaskRunning(task: DownloadGroupTask) {
        Thread {
            val entity = dao.getByTaskId(task.entity.id)
                ?: return@Thread
            if (adapter.getSeason(adapter.expand)?.seasonId != entity.sid) {
                return@Thread
            }

            val info = list[SeriesData(seasonId = entity.sid)]
                ?.get(EpisodeData(cid = entity.cid))
                ?: return@Thread
            info.postValue(DownloadListAdapter.TaskInfo(
                    task.percent, task.speed
            ))
        }.start()
    }

    override fun onBindSeasonItem(position: Int, holder: DownloadListAdapter.SeasonTaskViewHolder) {
        val series = adapter.getSeason(position) ?: return
        if (adapter.isSelectMode()) {
            adapter.getEpisodeList(position)?.let {
                holder.binding.itemDownloadSeasonSelected.visibility =
                    adapter.isSeasonSelected(position).take(View.VISIBLE, View.GONE)
            }
        } else {
            holder.binding.itemDownloadSeasonSelected.visibility = View.GONE
        }
        holder.binding.root.setOnClickListener onClick@{
            onSeasonItemClick(position)
        }
        holder.binding.itemDownloadSeasonTitle.text = series.title
        Glide.with(context)
            .customLoad(series.cover)
            .withRound()
            .withCrossFade()
            .withVerticalPlaceholder()
            .into(holder.binding.itemDownloadSeasonCover)
        dao.listenBySid(series.seasonId).observe(this) {
            var finished = 0
            var processing = 0
            for (taskEntity in it) {
                when(taskEntity.status) {
                    TaskEntity.STATUS_PROCESSING -> processing++
                    TaskEntity.STATUS_FINISHED -> finished++
                }
            }
            when {
                processing > 0 -> {
                    holder.binding.itemDownloadSeasonStatus.setTextColor(context.getColor(R.color.colorPrimary))
                    holder.binding.itemDownloadSeasonStatus.text = Application.getString(
                        R.string.text_download_task_status, it.size, processing
                    )
                }
                finished != it.size -> {
                    holder.binding.itemDownloadSeasonStatus.setTextColor(context.getColor(R.color.color_text_dark))
                    holder.binding.itemDownloadSeasonStatus.text = Application.getString(
                        R.string.text_download_task_finished, it.size, finished
                    )
                }
                else -> {
                    holder.binding.itemDownloadSeasonStatus.setTextColor(context.getColor(R.color.color_text_dark))
                    holder.binding.itemDownloadSeasonStatus.text = Application.getString(
                        R.string.text_download_task_all_finished, it.size
                    )
                }
            }
        }
    }

    private fun onSeasonItemClick(position: Int) {
        if (adapter.isSelectMode() && adapter.expand == position) {
            adapter.exchangeAllExpandEpisode()
            return
        }
        if (adapter.expand >= 0) {
            adapter.notifyEpisodeFold()
        }
        if (position == adapter.expand) {
            adapter.expand = -1
            return
        }
        adapter.expand = position
        adapter.notifyEpisodeExpand()
    }

    override fun onBindExpandEpisodeItem(position: Int, holder: DownloadListAdapter.EpisodeTaskViewHolder) {
        val episode = adapter.getExpandEpisode()[position]
        if (adapter.isSelectMode()) {
            holder.binding.itemDownloadEpisodeSelected.visibility =
                adapter.isEpisodeSelected(episode).take(View.VISIBLE, View.GONE)
        } else {
            holder.binding.itemDownloadEpisodeSelected.visibility = View.GONE
        }
        holder.binding.root.setOnClickListener {
            onEpisodeItemClick(episode, position)
        }
        holder.binding.root.setOnLongClickListener {
            onEpisodeItemLongClick(episode, position)
        }
        holder.binding.itemDownloadEpisodeTitle.text = episode.title
        Glide.with(context)
            .customLoad(episode.cover)
            .withRound()
            .withCrossFade()
            .withVerticalPlaceholder()
            .into(holder.binding.itemDownloadEpisodeCover)
        dao.listenByCid(episode.cid).observe(this) { task ->
            when(task.status) {
                TaskEntity.STATUS_PAUSED -> {
                    holder.binding.itemDownloadEpisodeProgress.visibility = View.VISIBLE
                    holder.binding.itemDownloadEpisodeProgress.isIndeterminate = false
                    holder.binding.itemDownloadEpisodeStatus.text =
                        Application.getString(R.string.text_download_task_paused)
                    holder.binding.itemDownloadEpisodeStatus.setTextColor(context.getColor(R.color.colorPrimary))
                }
                TaskEntity.STATUS_PAUSING -> {
                    holder.binding.itemDownloadEpisodeProgress.visibility = View.VISIBLE
                    holder.binding.itemDownloadEpisodeProgress.isIndeterminate = true
                    holder.binding.itemDownloadEpisodeStatus.text =
                        Application.getString(R.string.text_download_task_pausing)
                    holder.binding.itemDownloadEpisodeStatus.setTextColor(context.getColor(R.color.color_text_dark))
                }
                TaskEntity.STATUS_PROCESSING -> {
                    holder.binding.itemDownloadEpisodeProgress.visibility = View.VISIBLE
                    holder.binding.itemDownloadEpisodeProgress.isIndeterminate = false
                    holder.binding.itemDownloadEpisodeStatus.text = ""
                    list[adapter.getExpandSeason()]?.get(episode)?.observe(this) {
                        holder.binding.itemDownloadEpisodeProgress.progress = it.percent
                        val speed = it.speed / 1024.0
                        holder.binding.itemDownloadEpisodeSpeed.text = String.format("%.2f KB/s", speed)
                    }
                }
                TaskEntity.STATUS_WAITING -> {
                    holder.binding.itemDownloadEpisodeProgress.visibility = View.GONE
                    holder.binding.itemDownloadEpisodeStatus.text =
                        Application.getString(R.string.text_download_task_waiting)
                    holder.binding.itemDownloadEpisodeStatus.setTextColor(context.getColor(R.color.color_text_dark))
                }
                TaskEntity.STATUS_PREPARING -> {
                    holder.binding.itemDownloadEpisodeProgress.visibility = View.VISIBLE
                    holder.binding.itemDownloadEpisodeProgress.isIndeterminate = true
                    holder.binding.itemDownloadEpisodeStatus.text =
                        Application.getString(R.string.text_download_task_preparing)
                    holder.binding.itemDownloadEpisodeStatus.setTextColor(context.getColor(R.color.color_text_dark))
                }
                TaskEntity.STATUS_FINISHED -> {
                    holder.binding.itemDownloadEpisodeProgress.visibility = View.GONE
                    holder.binding.itemDownloadEpisodeStatus.text =
                        Application.getString(R.string.text_download_task_single_finished)
                    holder.binding.itemDownloadEpisodeStatus.setTextColor(context.getColor(R.color.color_text_dark))
                }
                else -> {
                    holder.binding.itemDownloadEpisodeProgress.visibility = View.GONE
                    holder.binding.itemDownloadEpisodeStatus.text = task.message
                    holder.binding.itemDownloadEpisodeStatus.setTextColor(context.getColor(R.color.color_err))
                }
            }
        }
    }

    private fun onEpisodeItemClick(episode: EpisodeData, position: Int) {
        if (adapter.isSelectMode()) {
            adapter.exchangeEpisode(episode, position)
            return
        }
        val task = dao.getByCid(episode.cid) ?: return
        when(task.status) {
            TaskEntity.STATUS_FINISHED -> {

            }
            TaskEntity.STATUS_PREPARING, TaskEntity.STATUS_PAUSING -> {
                // ignore
            }
            TaskEntity.STATUS_PROCESSING -> {
                dao.updateStatusByCid(task.cid, TaskEntity.STATUS_PAUSING)
                Aria.download(this).load(task.taskId).stop()
            }
            TaskEntity.STATUS_PAUSED, TaskEntity.STATUS_WAITING -> {
                Application.startListenTask()
                if (dao.getByTaskStatus(TaskEntity.STATUS_PROCESSING).isNotEmpty()) {
                    Application.onToast(context, R.string.error_download_processing_exist)
                } else {
                    dao.updateStatusByCid(task.cid, TaskEntity.STATUS_WAITING)
                }
            }
        }
    }

    private fun onEpisodeItemLongClick(episode: EpisodeData, position: Int): Boolean {
        if (adapter.isSelectMode()) {
            return false
        }
        adapter.enterSelectMode()
        adapter.exchangeEpisode(episode, position)
        return true
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentDownloadListBinding =
        FragmentDownloadListBinding.inflate(layoutInflater, container, false)

    override fun onDestroy() {
        list.clear()
        super.onDestroy()
    }

    override fun onBackPressed(): Boolean {
        if (!adapter.isSelectMode()) {
            return super.onBackPressed()
        }
        adapter.exitSelectMode()
        return true
    }
}