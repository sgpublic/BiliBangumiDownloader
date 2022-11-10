package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.aria.core.task.DownloadGroupTask
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.ui.MultiSelectable
import io.github.sgpublic.bilidownload.base.ui.SelectableArrayAdapter
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity
import io.github.sgpublic.bilidownload.core.util.customLoad
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.core.util.withCrossFade
import io.github.sgpublic.bilidownload.core.util.withHorizontalPlaceholder
import io.github.sgpublic.bilidownload.databinding.ItemDownloadEpisodeBinding
import java.io.File

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 11:18
 */
class DownloadEpisodeAdapter: SelectableArrayAdapter<ItemDownloadEpisodeBinding, DownloadTaskEntity>(),
    MultiSelectable<DownloadTaskEntity> {
    init {
        setOnItemLongClickListener longClick@{
            if (isSelectMode()) {
                return@longClick false
            }
            entrySelectMode(getItemPosition(it.epid))
            return@longClick true
        }
    }

    override fun setOnItemClickListener(onClick: (DownloadTaskEntity) -> Unit) {
        super.setOnItemClickListener {
            if (isSelectMode()) {
                toggleSelection(getItemPosition(it.taskId))
            } else {
                onClick.invoke(it)
            }
        }
    }

    private val taskIdTmp: HashMap<Long, Int> = HashMap()
    override fun getItemPosition(id: Long): Int = taskIdTmp[id] ?: 0
    override fun setData(list: Collection<DownloadTaskEntity>) {
        super.setData(list)
        list.forEachIndexed { index, seasonTaskGroup ->
            taskIdTmp[seasonTaskGroup.taskId] = index
        }
    }

    private var runningTask: DownloadGroupTask? = null
    fun setRunningTask(runningTask: DownloadGroupTask?) {
        this.runningTask = runningTask
        notifyItemChanged(getItemPosition(
            (runningTask ?: return).entity.id
        ))
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup) =
        ItemDownloadEpisodeBinding.inflate(inflater, parent, false)

    override fun onBindViewHolder(
        context: Context,
        ViewBinding: ItemDownloadEpisodeBinding,
        data: DownloadTaskEntity
    ) {
        val cover = File(context.getExternalFilesDir("Cover")!!, "s_${data.sid}/ep${data.epid}/cover.png")
            .takeIf { it.exists() }?.toUri()
        Glide.with(context)
            .let {
                if (cover != null) {
                    it.customLoad(cover)
                } else {
                    it.customLoad(data.episodeCover)
                }
            }
            .withHorizontalPlaceholder()
            .withCrossFade()
            .into(ViewBinding.itemDownloadEpisodeCover)
        ViewBinding.itemDownloadEpisodeTitle.text = data.episodeTitle
        if (data.status == DownloadTaskEntity.Status.Processing) {
            runningTask?.let { runningTask ->
                ViewBinding.itemDownloadEpisodeStatus.visibility = View.GONE
                ViewBinding.itemDownloadEpisodeSpeed.visibility = View.VISIBLE
                ViewBinding.itemDownloadEpisodeProgress.visibility = View.VISIBLE
                ViewBinding.itemDownloadEpisodeSpeed.text = runningTask.convertSpeed
                ViewBinding.itemDownloadEpisodeProgress.progress = runningTask.percent
            }
        } else {
            ViewBinding.itemDownloadEpisodeStatus.visibility = View.VISIBLE
            ViewBinding.itemDownloadEpisodeSpeed.visibility = View.GONE
            ViewBinding.itemDownloadEpisodeProgress.visibility = View.GONE
            ViewBinding.itemDownloadEpisodeStatus.text = when (data.status) {
                DownloadTaskEntity.Status.Finished -> context.getString(R.string.text_download_task_single_finished)
                DownloadTaskEntity.Status.Prepare -> context.getString(R.string.text_download_task_preparing)
                DownloadTaskEntity.Status.Paused -> context.getString(R.string.text_download_task_paused)
                DownloadTaskEntity.Status.Waiting -> context.getString(R.string.text_download_task_waiting)
                DownloadTaskEntity.Status.Error -> context.getString(R.string.text_download_task_error, data.statusMessage)
                else -> ""
            }
            ViewBinding.itemDownloadEpisodeStatus.setTextColor(context.getColor(
                (data.status == DownloadTaskEntity.Status.Error).take(R.color.color_err, R.color.color_text_dark)
            ))
        }
        ViewBinding.itemDownloadEpisodeSelected.visibility = (selectMode && multiSelection.contains(
            getItemPosition(data.taskId)
        )).take(View.VISIBLE, View.GONE)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {

    }

    override fun getClickableView(ViewBinding: ItemDownloadEpisodeBinding) = ViewBinding.root
    override fun getLongClickableView(ViewBinding: ItemDownloadEpisodeBinding) = ViewBinding.root

    override val Adapter: SelectableArrayAdapter<*, DownloadTaskEntity> = this
    override var selectMode: Boolean = false
    override val multiSelection: HashSet<Int> = HashSet()

    private var onChangeSelectMode: (Boolean) -> Unit = { }
    fun setOnChangeSelectModeListener(listener: (Boolean) -> Unit) {
        onChangeSelectMode = listener
    }
    override fun invokeOnChangeSelectMode(mode: Boolean) {
        onChangeSelectMode.invoke(mode)
    }
}