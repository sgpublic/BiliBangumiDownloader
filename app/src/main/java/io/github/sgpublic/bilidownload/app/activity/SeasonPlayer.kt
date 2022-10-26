package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.fragment.factory.PlayerFragmentFactory
import io.github.sgpublic.bilidownload.app.viewmodel.OnlinePlayerViewModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelActivity
import io.github.sgpublic.bilidownload.core.exsp.UserPreference
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding
import io.github.sgpublic.exsp.ExPreference

class SeasonPlayer: BaseViewModelActivity<ActivityPlayerBinding, OnlinePlayerViewModel>() {
    override val ViewModel: OnlinePlayerViewModel by viewModels {
        ViewModelFactory(intent.getLongExtra(KEY_SEASON_ID, -1))
    }

    override fun beforeCreate() {
        supportFragmentManager.fragmentFactory = PlayerFragmentFactory(this@SeasonPlayer)
        super.beforeCreate()
    }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewModelSetup() {
        ViewModel.SID.observe(this) {
            if (it < 0) {
                Application.onToast(this@SeasonPlayer, R.string.title_season_unknown)
                finish()
                return@observe
            }
        }
    }

    override fun onViewSetup() {
        supportFragmentManager.beginTransaction().apply {
//            replace(ViewBinding.playerOrigin.id, OnlinePlayer::class.java, null, "OnlinePlayer")
//            ViewBinding.playerContent?.let {
//                replace(it.id, SeasonOnlinePage::class.java, null, "SeasonPage")
//            }
        }.commit()


        ViewBinding.playerLoading?.startLoad()
        ViewBinding.playerToolbar?.let {
            initViewAtTop(it)
        }
    }

    override val ViewBinding: ActivityPlayerBinding by viewBinding()
    companion object {
        const val KEY_SEASON_ID = "season_id"
        const val KEY_EPISODE_ID = "ep_id"

        fun startActivity(context: Context, sid: Long, epid: Long? = null) {
            val intent = Intent(context, SeasonPlayer::class.java)
            intent.putExtra(KEY_SEASON_ID, sid)
            epid?.let { intent.putExtra(KEY_EPISODE_ID, it) }
            context.startActivity(intent)
        }
    }

    private class ViewModelFactory(
        private val sid: Long
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Long::class.java).newInstance(sid)
        }
    }
}