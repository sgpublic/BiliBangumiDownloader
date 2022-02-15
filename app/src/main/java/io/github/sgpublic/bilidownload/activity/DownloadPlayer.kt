package io.github.sgpublic.bilidownload.activity

import androidx.activity.viewModels
import io.github.sgpublic.bilidownload.base.BaseViewModelActivity
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding
import io.github.sgpublic.bilidownload.fragment.factory.PlayerFragmentFactory
import io.github.sgpublic.bilidownload.fragment.player.LocalPlayer
import io.github.sgpublic.bilidownload.fragment.season.DownloadList
import io.github.sgpublic.bilidownload.viewmodel.LocalPlayerViewModel

class DownloadPlayer: BaseViewModelActivity<ActivityPlayerBinding, LocalPlayerViewModel>() {
    override val ViewModel: LocalPlayerViewModel by viewModels()

    override fun beforeCreate() {
        supportFragmentManager.fragmentFactory = PlayerFragmentFactory(this)
        super.beforeCreate()
    }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        super.onViewSetup()
        supportFragmentManager.beginTransaction().apply {
            replace(ViewBinding.playerOrigin.id, LocalPlayer::class.java, null, "OnlinePlayer")
            ViewBinding.playerContent?.let {
                replace(it.id, DownloadList::class.java, null, "DownloadList")
            }
        }.commit()
    }

    override fun onCreateViweBinding(): ActivityPlayerBinding =
        ActivityPlayerBinding.inflate(layoutInflater)
}