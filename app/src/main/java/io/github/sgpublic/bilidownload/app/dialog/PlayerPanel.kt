package io.github.sgpublic.bilidownload.app.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.lxj.xpopup.core.DrawerPopupView
import io.github.sgpublic.bilidownload.app.ui.list.EpisodeListAdapter
import io.github.sgpublic.bilidownload.app.ui.list.QualityListAdapter
import io.github.sgpublic.bilidownload.core.util.dp
import io.github.sgpublic.bilidownload.databinding.DialogPlayerPanelBinding

class PlayerPanel(context: Context) : DrawerPopupView(context) {
    private var panelWidth: Int = 0
    override fun getPopupWidth(): Int {
        return panelWidth
    }

    private val binding: DialogPlayerPanelBinding by lazy {
        DialogPlayerPanelBinding.inflate(
            LayoutInflater.from(context), drawerContentContainer, false
        )
    }

    fun setQualityAdapter(adapter: QualityListAdapter) {
        panelWidth = 180.dp
        val params = binding.dialogPanelList.layoutParams as ConstraintLayout.LayoutParams
        params.width = panelWidth
        params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        binding.dialogPanelList.layoutParams = params
        binding.dialogPanelList.adapter = adapter
        binding.dialogEpisodeListTitle.visibility = View.GONE
    }

    fun setEpisodeAdapter(adapter: EpisodeListAdapter) {
        panelWidth = 320.dp
        val params = binding.dialogPanelList.layoutParams as ConstraintLayout.LayoutParams
        params.width = panelWidth
        params.height = 0
        binding.dialogPanelList.layoutParams = params
        binding.dialogPanelList.adapter = adapter
        binding.dialogEpisodeListTitle.visibility = View.VISIBLE
    }

    override fun addInnerContent() {
        drawerContentContainer.addView(binding.root)
    }

    override fun getPopupImplView() = binding.root
}