package io.github.sgpublic.bilidownload.fragment.factory

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import io.github.sgpublic.bilidownload.base.BaseViewModelActivity
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding
import io.github.sgpublic.bilidownload.fragment.player.LocalPlayer
import io.github.sgpublic.bilidownload.fragment.player.OnlinePlayer
import io.github.sgpublic.bilidownload.fragment.season.SeasonOnlinePage

class PlayerFragmentFactory(
    private val context: BaseViewModelActivity<ActivityPlayerBinding, *>
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(loadFragmentClass(classLoader, className)) {
            SeasonOnlinePage::class.java -> SeasonOnlinePage(context)
            OnlinePlayer::class.java -> OnlinePlayer(context)
            LocalPlayer::class.java -> LocalPlayer(context)
            else -> super.instantiate(classLoader, className)
        }
    }
}