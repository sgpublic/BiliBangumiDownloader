package io.github.sgpublic.bilidownload.fragment.player

import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.android.exoplayer2.Player
import io.github.sgpublic.bilidownload.base.BaseViewModelActivity
import io.github.sgpublic.bilidownload.data.parcelable.EntryJson
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding
import io.github.sgpublic.bilidownload.viewmodel.BasePlayerViewModel

@Suppress("PrivatePropertyName")
class LocalPlayer(contest: BaseViewModelActivity<ActivityPlayerBinding, *>)
    : BasePlayer<EntryJson, BasePlayerViewModel<EntryJson>>(contest), Player.Listener {
    override val ViewModel: BasePlayerViewModel<EntryJson> by activityViewModels()

    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        ViewBinding.playerControllerQuality?.visibility = View.GONE
    }

    override fun onPlayEpisode(data: EntryJson) {
        // TODO
    }
}