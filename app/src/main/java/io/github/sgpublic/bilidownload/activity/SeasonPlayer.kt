package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseViewModelActivity
import io.github.sgpublic.bilidownload.core.module.ApiModule
import io.github.sgpublic.bilidownload.core.util.newObserve
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding
import io.github.sgpublic.bilidownload.fragment.factory.PlayerFragmentFactory
import io.github.sgpublic.bilidownload.fragment.player.OnlinePlayer
import io.github.sgpublic.bilidownload.fragment.season.SeasonOnlinePage
import io.github.sgpublic.bilidownload.room.entity.WatchHistoryEntity
import io.github.sgpublic.bilidownload.ui.customLoad
import io.github.sgpublic.bilidownload.ui.withBlur
import io.github.sgpublic.bilidownload.ui.withCrossFade
import io.github.sgpublic.bilidownload.ui.withError
import io.github.sgpublic.bilidownload.viewmodel.OnlinePlayerViewModel

class SeasonPlayer: BaseViewModelActivity<ActivityPlayerBinding, OnlinePlayerViewModel>() {
    override val ViewModel: OnlinePlayerViewModel by viewModels {
        OnlinePlayerViewModel.Factory(intent.getLongExtra(KEY_SEASON_ID, -1))
    }

    override fun beforeCreate() {
        supportFragmentManager.fragmentFactory = PlayerFragmentFactory(this@SeasonPlayer)
        super.beforeCreate()
    }

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        ViewModel.getEpisodeList().let {
            if (it.isEmpty()) {
                return@let
            }
            ViewBinding.playerCover?.visibility = View.GONE
            ViewBinding.playerOrigin.visibility = View.VISIBLE
        }
    }

    override fun onCreateViewBinding(): ActivityPlayerBinding =
        ActivityPlayerBinding.inflate(layoutInflater)

    override fun onViewModelSetup() {
        if (ViewModel.getSeasonId() < 0) {
            Application.onToast(this@SeasonPlayer, R.string.title_season_unknown)
            finish()
            return
        }
        ViewModel.EPISODE_LIST.newObserve(this) { list ->
            if (list.isNotEmpty()) {
                ViewBinding.playerPlayerCover?.visibility = View.VISIBLE
            }
            ViewBinding.playerCover?.setOnClickListener {
                it.visibility = View.GONE
                ViewBinding.playerOrigin.visibility = View.VISIBLE
                var target = intent.getIntExtra(KEY_EPISODE_INDEX, -1)
                if (target < 0) {
                    target = Application.DATABASE.WatchHistoryDao()
                        .getLatestBySeasonId(ViewModel.getSeasonId()) ?: 0
                }
                ViewModel.requestPlayEpisode(target)
            }
            ViewModel.PLAYER_EPISODE_INDEX.newObserve(this) { index ->
                ViewBinding.playerCover?.visibility = View.GONE
                ViewBinding.playerOrigin.visibility = View.VISIBLE
                Thread {
                    val dao = Application.DATABASE.WatchHistoryDao()
                    val data = list[index]
                    val history = dao.getByCid(data.cid)?.also {
                        it.watch_time = ApiModule.TS
                    } ?: WatchHistoryEntity(
                        data.cid, index, ViewModel.getSeasonId()
                    )
                    dao.save(history)
                }.run()
            }
        }
        ViewModel.SEASON_DATA.newObserve(this) {
            setAnimateState(false, 300, ViewBinding.playerLoading) {
                ViewBinding.playerLoading?.stopLoad()
            }
            setAnimateState(false, 300, ViewBinding.playerContent) {
                setAnimateState(true, 500, ViewBinding.playerContent)
            }
            ViewBinding.playerToolbarTitle?.text = it.info.title
            ViewBinding.playerCover?.let { seasonCover ->
                Glide.with(this@SeasonPlayer)
                    .customLoad(it.info.cover)
                    .withError(R.drawable.pic_load_failed_h)
                    .withBlur()
                    .withCrossFade()
                    .into(seasonCover)
            }
            ViewModel.SEASON_ID.newObserve(this) sid@{ sid ->
                if (sid == it.info.seasonId) {
                    return@sid
                }
                ViewBinding.playerAppbar?.setExpanded(true, true)
                ViewBinding.playerContentBase?.smoothScrollTo(0, 0)
                ViewBinding.playerToolbarTitle?.text = ""
                ViewBinding.playerCover?.let { playerCover ->
                    playerCover.alpha = 0f
                    playerCover.visibility = View.INVISIBLE
                }
                setAnimateState(false, 200, ViewBinding.playerContent) {
                    ViewBinding.playerLoading?.startLoad()
                    setAnimateState(true, 200, ViewBinding.playerLoading) {
                        ViewModel.getInfoBySid()
                        ViewModel.getRecommend()
                    }
                }
            }
        }
        ViewModel.EXCEPTION.newObserve(this) {
            Application.onToast(this@SeasonPlayer, R.string.error_bangumi_load, it.message, it.code)
            ViewBinding.playerLoading?.stopLoad(true)
        }
        ViewModel.PLAYER_PLAYING.newObserve(this) {
            val layout = ViewBinding.playerOrigin.layoutParams
            if (layout !is CollapsingToolbarLayout.LayoutParams) {
                return@newObserve
            }
            layout.collapseMode = if (it) CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN else
                CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
        }
    }

    override fun onViewSetup() {
        supportFragmentManager.beginTransaction().apply {
            replace(ViewBinding.playerOrigin.id, OnlinePlayer::class.java, null, "OnlinePlayer")
            ViewBinding.playerContent?.let {
                replace(it.id, SeasonOnlinePage::class.java, null, "SeasonPage")
            }
        }.commit()


        ViewBinding.playerLoading?.startLoad()
        ViewBinding.playerToolbar?.let {
            initViewAtTop(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

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