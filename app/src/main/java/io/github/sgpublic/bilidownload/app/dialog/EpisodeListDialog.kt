package io.github.sgpublic.bilidownload.app.dialog

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lxj.xpopup.core.BottomPopupView
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.service.DownloadService
import io.github.sgpublic.bilidownload.app.ui.recycler.SeasonEpisodeDialogAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity
import io.github.sgpublic.bilidownload.databinding.DialogEpisodeListBinding

@SuppressLint("ViewConstructor")
class EpisodeListDialog(
    context: AppCompatActivity, private val seasonCover: String,
    private val data: Collection<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>,
    private val tasks: LiveData<List<DownloadTaskEntity>>,
    private val title: String, private val sid: Long,
    private val currentEpid: MutableLiveData<Pair<Long, Long>>,
    private val qn: Map<Int, String>
) : BottomPopupView(context) {
    private val adapter: SeasonEpisodeDialogAdapter by lazy {
        SeasonEpisodeDialogAdapter().also { adapter ->
            adapter.setData(data)
            adapter.setSelectable(qn.isNotEmpty())
            adapter.setOnItemClickListener {
                if (adapter.isSelectMode()) {
                    adapter.toggleSelection(adapter.getPosition(it.id))
                } else {
                    onItemClick.invoke(it.id, it.cid!!)
                }
            }
            tasks.observe(this) {
                adapter.setDownloadTasks(it)
            }
            currentEpid.observe(this) {
                adapter.setSelectedEpid(it.first)
            }
            adapter.setOnChangeSelectModeListener {
                if (it) {
                    ViewBinding.dialogEpisodeDownload.hide()
                    ViewBinding.dialogEpisodeConfirm.show()
                } else {
                    ViewBinding.dialogEpisodeDownload.show()
                    ViewBinding.dialogEpisodeConfirm.hide()
                }
            }
        }
    }

    private val ViewBinding: DialogEpisodeListBinding by lazy { DialogEpisodeListBinding.bind(popupImplView) }
    override fun onCreate() {
        ViewBinding.dialogEpisodeConfirm.hide()
        ViewBinding.dialogEpisodeListTitle.text = title
        ViewBinding.dialogEpisodeList.adapter = adapter
        ViewBinding.dialogEpisodeDownload.setOnClickListener {
            adapter.entrySelectMode()
        }
        ViewBinding.dialogEpisodeConfirm.setOnClickListener {
            val list = adapter.exitSelectMode()
            if (list.isEmpty()) {
                return@setOnClickListener
            }
            val tasks = ArrayList<DownloadTaskEntity>(list.size)
            for (item in list) {
                DownloadTaskEntity().let { entity ->
                    entity.epid = item.id
                    entity.cid = item.cid!!
                    entity.sid = sid
                    entity.seasonCover = seasonCover
                    entity.episodeCover = item.cover
                    tasks.add(entity)
                }
            }
            Application.Database.DownloadTaskDao().add(tasks)
            DownloadService.startService(context)
        }
    }

    private var onItemClick: (Long, Long) -> Unit = { _, _ -> }
    fun setOnItemClickListener(onItemClick: (Long, Long) -> Unit): EpisodeListDialog {
        this.onItemClick = onItemClick
        return this
    }

    override fun onBackPressed(): Boolean {
        if (!adapter.isSelectMode()) {
            return super.onBackPressed()
        }
        adapter.cancelSelectMode()
        return true
    }

    override fun onDismiss() {
        adapter.cancelSelectMode()
        super.onDismiss()
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_episode_list
}