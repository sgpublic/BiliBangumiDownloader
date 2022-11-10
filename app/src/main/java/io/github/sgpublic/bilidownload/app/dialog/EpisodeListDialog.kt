package io.github.sgpublic.bilidownload.app.dialog

import android.annotation.SuppressLint
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lxj.xpopup.core.BottomPopupView
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.service.DownloadService
import io.github.sgpublic.bilidownload.app.ui.list.QualitySpinnerAdapter
import io.github.sgpublic.bilidownload.app.ui.recycler.SeasonEpisodeDialogAdapter
import io.github.sgpublic.bilidownload.core.exsp.BangumiPreference
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity
import io.github.sgpublic.bilidownload.databinding.DialogEpisodeListBinding
import io.github.sgpublic.exsp.ExPreference
import java.util.*

@SuppressLint("ViewConstructor")
class EpisodeListDialog(
    context: AppCompatActivity, private val seasonCover: String,
    private val data: Collection<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>,
    private val tasks: LiveData<List<DownloadTaskEntity>>,
    private val title: String, private val sid: Long,
    private val seasonTitle: String,
    private val currentEpid: MutableLiveData<Pair<Long, Long>>,
    private val qn: LinkedHashMap<Int, String>,
    private val fittedQn: Int
) : BottomPopupView(context) {
    private val adapter: SeasonEpisodeDialogAdapter by lazy {
        SeasonEpisodeDialogAdapter().also { adapter ->
            adapter.setData(data)
            adapter.setIsSelectable(qn.isNotEmpty())
            adapter.setOnItemClickListener {
                onItemClick.invoke(it.id, it.cid!!)
            }
            tasks.observe(this) {
                adapter.setDownloadTasks(it)
            }
            currentEpid.observe(this) {
                adapter.setSelectedEpid(it.first)
            }
            adapter.setOnChangeSelectModeListener { mode ->
                if (mode) {
                    ViewBinding.dialogEpisodeDownload.hide()
                    ViewBinding.dialogEpisodeConfirm.show()
                    ViewBinding.dialogEpisodeQuality.visibility = View.VISIBLE
                } else {
                    ViewBinding.dialogEpisodeDownload.show()
                    ViewBinding.dialogEpisodeConfirm.hide()
                    ViewBinding.dialogEpisodeQuality.visibility = View.GONE
                }
            }
        }
    }

    private val ViewBinding: DialogEpisodeListBinding by lazy { DialogEpisodeListBinding.bind(popupImplView) }
    override fun onCreate() {
        val bangumi = ExPreference.get<BangumiPreference>()
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
                    entity.episodeTitle = try {
                        Application.getString(R.string.text_episode_index, item.title.apply { toFloat() })
                    } catch (_: NumberFormatException) {
                        item.title
                    }.takeIf {
                        item.longTitle.isBlank()
                    } ?: item.longTitle
                    entity.sid = sid
                    entity.seasonTitle = seasonTitle
                    entity.seasonCover = seasonCover
                    entity.episodeCover = item.cover
                    entity.qn = bangumi.quality
                    tasks.add(entity)
                }
            }
            Application.Database.DownloadTaskDao().add(tasks)
            DownloadService.startService(context)
        }
        val data = LinkedList(qn.keys)
        ViewBinding.dialogEpisodeQuality.adapter = QualitySpinnerAdapter(ArrayList(qn.entries))
        ViewBinding.dialogEpisodeQuality.setSelection(
            data.indexOf(fittedQn).takeIf { it >= 0 && it < data.size } ?: 0, true
        )
        ViewBinding.dialogEpisodeQuality.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                bangumi.quality = id.toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                bangumi.quality = 80
            }
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