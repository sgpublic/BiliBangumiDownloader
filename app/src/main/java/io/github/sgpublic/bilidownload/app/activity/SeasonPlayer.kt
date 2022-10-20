package io.github.sgpublic.bilidownload.app.activity

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.fragment.factory.PlayerFragmentFactory
import io.github.sgpublic.bilidownload.app.viewmodel.OnlinePlayerViewModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelActivity
import io.github.sgpublic.bilidownload.core.exsp.UserPreference
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding
import io.github.sgpublic.exsp.ExPreference

class SeasonPlayer: BaseViewModelActivity<ActivityPlayerBinding, OnlinePlayerViewModel>() {
    override val ViewModel: OnlinePlayerViewModel by viewModels()
    private val mid = ExPreference.get(UserPreference::class.java).mid

    override fun beforeCreate() {
        supportFragmentManager.fragmentFactory = PlayerFragmentFactory(this@SeasonPlayer)
        super.beforeCreate()
    }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        if ((ViewModel.SID.value ?: -1) < 0) {
            Application.onToast(this@SeasonPlayer, R.string.title_season_unknown)
            finish()
            return
        }
        ViewModel.SID.postValue(intent.getLongExtra(KEY_SEASON_ID, -1))
    }

    override fun onViewModelSetup() {

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
        const val KEY_EPISODE_INDEX = "ep_index"

        fun startActivity(context: Context, sid: Long, target: Int? = null) {
            val intent = Intent(context, SeasonPlayer::class.java)
            intent.putExtra(KEY_SEASON_ID, sid)
            target?.let { intent.putExtra(KEY_EPISODE_INDEX, it) }
            context.startActivity(intent)
        }
    }
}