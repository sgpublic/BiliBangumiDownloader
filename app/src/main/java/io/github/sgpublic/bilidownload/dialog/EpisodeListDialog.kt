package io.github.sgpublic.bilidownload.dialog

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.lxj.xpopup.core.BottomPopupView
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import io.github.sgpublic.bilidownload.databinding.DialogEpisodeListBinding
import io.github.sgpublic.bilidownload.ui.list.QualitySpinnerAdapter
import io.github.sgpublic.bilidownload.ui.recycler.EpisodeRecyclerAdapter

@SuppressLint("ViewConstructor")
class EpisodeListDialog(
    private val context: AppCompatActivity, private val list: List<EpisodeData>,
    currentIndex: LiveData<Int>, private val title: String, private val qn: Map<Int, String>
) : BottomPopupView(context) {
    private val episodeAdapter = EpisodeRecyclerAdapter(context, list, currentIndex, true)
    override fun onCreate() {
        val binding: DialogEpisodeListBinding = DialogEpisodeListBinding.bind(popupImplView)
        binding.dialogEpisodeConfirm.hide()
        binding.dialogEpisodeListTitle.text = title
        binding.dialogEpisodeList.adapter = episodeAdapter
        episodeAdapter.setListener(object : EpisodeRecyclerAdapter.Listener {
            override fun onItemClick(position: Int) {
                this@EpisodeListDialog.onItemClick(position)
            }

            override fun onEnterSelectMode(): Boolean {
                if (beforeEnterToSelectMod()) {
                    return true
                }
                binding.dialogEpisodeDownload.hide()
                binding.dialogEpisodeConfirm.show()
                binding.dialogEpisodeQuality.visibility = View.VISIBLE
                return false
            }

            override fun onExitSelectMode() {
                binding.dialogEpisodeDownload.show()
                binding.dialogEpisodeConfirm.hide()
                binding.dialogEpisodeQuality.visibility = View.GONE
            }
        })
        binding.dialogEpisodeConfirm.setOnClickListener {
            val selected = episodeAdapter.getSelectedData()
            if (selected.isNotEmpty()) {
                onItemSelect(selected, binding.dialogEpisodeQuality.selectedItemPosition)
            }
            episodeAdapter.exitSelectedMode()
        }
        binding.dialogEpisodeDownload.setOnClickListener {
            episodeAdapter.enterSelectMode()
        }
        val dao = Application.DATABASE.TasksDao()
        for (i in list.indices) {
            dao.listenStatusByCid(list[i].cid).observe(this) {
                if (episodeAdapter.isSelectMode()) {
                    episodeAdapter.notifyItemChanged(i)
                }
            }
        }
        QualitySpinnerAdapter(context).also { adapter ->
            adapter.setQualityMap(qn)
            binding.dialogEpisodeQuality.adapter = adapter
            adapter.notifyDataSetChanged()
            binding.dialogEpisodeQuality.setSelection(
                qn.keys.indexOf(ConfigManager.QUALITY).takeIf {
                    it >= 0
                } ?: qn.keys.indexOf(ConfigManager.DEFAULT_QUALITY).takeIf {
                    it >= 0
                } ?: 0
            )
        }
    }

    private var beforeEnterToSelectMod: () -> Boolean = { false }
    fun setBeforeEnterToSelectMod(beforeEnterToSelectMod: () -> Boolean) {
        this.beforeEnterToSelectMod = beforeEnterToSelectMod
    }

    private var onItemClick: (Int) -> Unit = { }
    fun setOnItemClickListener(onItemClick: (Int) -> Unit): EpisodeListDialog {
        this.onItemClick = onItemClick
        return this
    }

    private var onItemSelect: (List<EpisodeData>, Int) -> Unit = { _, _ -> }
    fun setOnItemSelectListener(onItemSelect: (List<EpisodeData>, Int) -> Unit): EpisodeListDialog {
        this.onItemSelect = onItemSelect
        return this
    }

    override fun onBackPressed(): Boolean {
        if (!episodeAdapter.isSelectMode()) {
            return super.onBackPressed()
        }
        episodeAdapter.exitSelectedMode()
        return true
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_episode_list
}