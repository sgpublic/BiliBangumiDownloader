package io.github.sgpublic.bilidownload.fragment.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Parcelable
import android.view.*
import android.widget.SeekBar
import com.google.android.exoplayer2.Player
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseViewModelActivity
import io.github.sgpublic.bilidownload.base.BaseViewModelFragment
import io.github.sgpublic.bilidownload.core.util.newObserve
import io.github.sgpublic.bilidownload.databinding.FragmentPlayerBinding
import io.github.sgpublic.bilidownload.dialog.PlayerPanel
import io.github.sgpublic.bilidownload.ui.PlayerGestureDetector
import io.github.sgpublic.bilidownload.ui.list.EpisodeListAdapter
import io.github.sgpublic.bilidownload.viewmodel.BasePlayerViewModel
import java.util.*

@Suppress("PrivatePropertyName", "PropertyName")
abstract class BasePlayer<T: Parcelable, VM: BasePlayerViewModel<T>>(contest: BaseViewModelActivity<*, *>)
    : BaseViewModelFragment<FragmentPlayerBinding, VM>(contest) {
    abstract fun onPlayEpisode(data: T = ViewModel.getPlayerCurrentEpisode())

    override fun onViewModelSetup() {
        ViewModel.PLAYER.newObserve(viewLifecycleOwner) {
            ViewBinding.playerControllerTimeTotal?.text = getDurationString(
                ViewModel.getPlayerDuration()
            )
            ViewBinding.playerControllerSeek.max = ViewModel.getPlayerIntDuration()
            ViewBinding.playerExoplayer.player = it
        }
        ViewModel.PLAYER_DURATION.newObserve(viewLifecycleOwner) { duration ->
            ViewBinding.playerControllerTimeTotal?.text = getDurationString(duration)
            ViewBinding.playerControllerSeek.max = duration.toInt()
        }
        ViewModel.PLAYER_STATE.newObserve(viewLifecycleOwner) {
            when (it) {
                Player.STATE_READY -> {

                }
                Player.STATE_ENDED -> {

                }
                Player.STATE_BUFFERING -> {

                }
                Player.STATE_IDLE -> {

                }
            }
        }
        ViewModel.PLAYER_LOADING.newObserve(viewLifecycleOwner) { loading ->
            startAnimate(loading, 300, ViewBinding.playerControllerLoading)
        }
        ViewModel.PLAYER_PLAYING.newObserve(viewLifecycleOwner) { playing ->
            ViewBinding.playerControllerPlay.setImageResource(
                if (playing) R.drawable.ic_player_pause else R.drawable.ic_player_play
            )
            if (playing) startListenDuration() else stopListenDuration()
        }
        ViewModel.CONTROLLER_VISIBILITY.newObserve(viewLifecycleOwner) { visible ->
            if (visible) {
                ViewBinding.playerController.visibility = View.VISIBLE
                ViewModel.setControllerDismissWithDelay(5000)
            }
            startAnimate(visible, 300, ViewBinding.playerController) {
                if (!visible) {
                    ViewBinding.playerController.visibility = View.GONE
                }
            }
        }
        ViewModel.PLAYER_CURRENT_DURATION.newObserve(viewLifecycleOwner) {
            if (!CONTROLLER_SEEK_TRACKING) {
                ViewBinding.playerControllerSeek.progress = it.toInt()
            }
            val current = getDurationString(it)
            ViewBinding.playerControllerTimeCurrent?.text = current
            @SuppressLint("SetTextI18n")
            ViewBinding.playerControllerTimeDuration?.text = "${current}/${
                getDurationString(ViewModel.PLAYER_DURATION.value ?: 0)
            }"
        }
        ViewModel.setOnPlayIndexChangeListener {
            onPlayEpisode(ViewModel.getPlayerEpisode(it))
        }
    }

    final override fun onCreateViewBinding(container: ViewGroup?): FragmentPlayerBinding =
        FragmentPlayerBinding.inflate(layoutInflater, container, false)

    protected var CONTROLLER_SEEK_TRACKING: Boolean = false
    override fun onViewSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (context.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                context.window.insetsController?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                context.window.insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                context.window.insetsController?.show(WindowInsets.Type.statusBars())
            }
        } else {
            // TODO sdk30 以下隐藏状态栏
            if (context.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                @Suppress("DEPRECATION")
                context.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else {
                @Suppress("DEPRECATION")
                context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }

        ViewBinding.playerExoplayer.setOnTouchListener(object : PlayerGestureDetector(context) {
            override fun onDoubleTap(e: MotionEvent) {
                ViewModel.changePlayingState()
            }

            override fun onSingleTap(e: MotionEvent) {
                ViewModel.changeControllerVisibility()
            }
        })

        ViewBinding.playerControllerFullscreen?.setOnClickListener {
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        ViewBinding.playerControllerBack?.setOnClickListener {
            onBackPressed()
        }
        ViewBinding.playerControllerPlay.setOnClickListener {
            ViewModel.changePlayingState()
        }
        ViewBinding.playerControllerEpisode?.setOnClickListener {
            openEpisodeListPanel()
        }
        ViewBinding.playerControllerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(v: SeekBar, progress: Int, fromUser: Boolean) { }
            override fun onStartTrackingTouch(v: SeekBar) {
                CONTROLLER_SEEK_TRACKING = true
                ViewModel.cancelControllerDismissWithDelay()
            }
            override fun onStopTrackingTouch(v: SeekBar) {
                CONTROLLER_SEEK_TRACKING = false
                ViewModel.getPlayer()?.seekTo(v.progress.toLong())
                ViewModel.setControllerDismissWithDelay()
            }
        })
    }

    private fun openEpisodeListPanel() {
        val adapter = EpisodeListAdapter(context)
        val popup = PlayerPanel(context)
        val panel = XPopup.Builder(context)
            .popupPosition(PopupPosition.Right)
            .asCustom(popup)
        popup.setEpisodeAdapter(adapter)
        adapter.setSelection(ViewModel.getPlayerCurrentEpisodeIndex())
        adapter.setEpisodeList(ViewModel.getPlayerTitleList())
        panel.show()
        adapter.setOnItemClickListener { pos, title ->
            panel.dismissWith {
                panel.destroy()
            }
            if (ViewModel.getPlayerCurrentEpisodeIndex() != pos) {
                ViewBinding.playerControllerTitle?.text = title
                ViewModel.requestPlayEpisode(pos)
            }
        }
    }

    private var onPlayerEndPlay: () -> Unit = { }
    fun setOnPlayerEndPlayListener(onPlayerEndPlay: () -> Unit) {
        this.onPlayerEndPlay = onPlayerEndPlay
    }
    private var durationListener: Timer? = null
    private fun stopListenDuration() {
        durationListener?.cancel()
        durationListener = null
    }
    private fun startListenDuration() {
        durationListener?.let { return }
        durationListener = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        val pre = ViewModel.PLAYER_CURRENT_DURATION.value ?: 0
                        val current = ViewModel.getPlayer()?.currentPosition ?: 0
                        ViewModel.PLAYER_CURRENT_DURATION.postValue(current)
                        ViewModel.PLAYER_LOADING.postValue(current - pre < 400)
                    }
                }
            }, 0, 1000)
        }
    }

    private fun getDurationString(duration: Long): String {
        val d = duration / 1000
        val h = "0${d / 60}".run {
            substring(length - 2)
        }
        val s = "0${d % 60}".run {
            substring(length - 2)
        }
        return "$h:$s"
    }

    override fun onBackPressed(): Boolean {
        if (context.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            @SuppressLint("SourceLockedOrientationActivity")
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            return true
        }
        return super.onBackPressed()
    }

    override fun onDestroy() {
        animate.clear()
        stopListenDuration()
        super.onDestroy()
    }
}