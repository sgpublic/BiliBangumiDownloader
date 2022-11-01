package io.github.sgpublic.bilidownload.app.dialog

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.lxj.xpopup.core.BottomPopupView
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.ui.recycler.SeasonEpisodeDialogAdapter
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.databinding.DialogEpisodeListBinding

@SuppressLint("ViewConstructor")
class EpisodeListDialog(
    context: AppCompatActivity,
    private val data: Collection<SeasonInfoResp.SeasonInfoData.Episodes.EpisodesData.EpisodesItem>,
    private val title: String,
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
            currentEpid.observe(this) {
                adapter.setSelectedEpid(it.first)
            }
        }
    }

    override fun onCreate() {
        val binding: DialogEpisodeListBinding = DialogEpisodeListBinding.bind(popupImplView)
        binding.dialogEpisodeConfirm.hide()
        binding.dialogEpisodeListTitle.text = title
        binding.dialogEpisodeList.adapter = adapter
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
        adapter.exitSelectMode()
        return true
    }

    override fun onDismiss() {
        adapter.cancelSelectMode()
        super.onDismiss()
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_episode_list
}