package io.github.sgpublic.bilidownload.app.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.lxj.xpopup.core.DrawerPopupView
import io.github.sgpublic.bilidownload.app.ui.list.EpisodeListAdapter
import io.github.sgpublic.bilidownload.core.util.dp
import io.github.sgpublic.bilidownload.databinding.DialogPlayerPanelBinding

class PlayerPanel(context: Context) : DrawerPopupView(context) {
    private var panelWidth: Int = 0
    override fun getPopupWidth(): Int {
        return panelWidth
    }
    private var binding: DialogPlayerPanelBinding? = null
    init {
        binding = DialogPlayerPanelBinding.inflate(
            LayoutInflater.from(context), drawerContentContainer, false
        )
    }


//    fun setQualityAdapter(adapter: QualityListAdapter) {
//        binding?.let {
//            (it.dialogPanelList.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
//                ConstraintLayout.LayoutParams.PARENT_ID
//            panelWidth = 180.dp
//            it.dialogPanelList.layoutParams.width = panelWidth
//            it.dialogEpisodeListTitle.visibility = View.GONE
//            it.dialogPanelList.adapter = adapter
//        }
//    }

    fun setEpisodeAdapter(adapter: EpisodeListAdapter) {
        binding?.let {
            (it.dialogPanelList.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                ConstraintLayout.LayoutParams.UNSET
            panelWidth = 320.dp
            it.dialogPanelList.layoutParams.width = panelWidth
            it.dialogEpisodeListTitle.visibility = View.VISIBLE
//            it.dialogPanelList.adapter = adapter
        }
    }

    override fun addInnerContent() {
        drawerContentContainer.addView(binding?.root)
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}