package io.github.sgpublic.bilidownload.dialog

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.lxj.xpopup.core.BottomPopupView
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.databinding.DialogEpisodeListBinding
import io.github.sgpublic.bilidownload.ui.recycler.EpisodeRecyclerAdapter

@SuppressLint("ViewConstructor")
class EpisodeListDialog(context: AppCompatActivity, private val list: List<EpisodeData>)
    : BottomPopupView(context) {
    private var adapter: EpisodeRecyclerAdapter? = EpisodeRecyclerAdapter(context, list)
    override fun onCreate() {
        val binding: DialogEpisodeListBinding = DialogEpisodeListBinding.bind(popupImplView)
        binding.dialogEpisodeListTitle.text = Application.getString(
            R.string.text_player_dialog_episode, list.size
        )
        binding.dialogEpisodeList.adapter = adapter
    }

    fun setOnItemClickLayout(onItemClick: (Int) -> Unit){
        adapter?.setOnItemClickListener(onItemClick)
    }

    override fun onDestroy() {
        adapter = null
        super.onDestroy()
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_episode_list
}