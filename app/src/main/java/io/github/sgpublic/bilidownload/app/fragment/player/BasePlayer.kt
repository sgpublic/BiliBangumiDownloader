package io.github.sgpublic.bilidownload.app.fragment.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.*
import android.widget.SeekBar
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.ui.PlayerGestureDetector
import io.github.sgpublic.bilidownload.app.viewmodel.BasePlayerModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelFragment
import io.github.sgpublic.bilidownload.core.util.animate
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.databinding.FragmentPlayerBinding
import java.util.*

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 14:30
 */
abstract class BasePlayer<VM: BasePlayerModel>(activity: AppCompatActivity)
    : BaseViewModelFragment<FragmentPlayerBinding, VM>(activity) {
    protected var ControllerSeekTracking: Boolean = false
    @CallSuper
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
            @Suppress("DEPRECATION")
            if (context.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                context.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else {
                context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
        ViewBinding.playerExoplayer.player = ViewModel.Player

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
        ViewBinding.playerControllerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(v: SeekBar, progress: Int, fromUser: Boolean) { }
            override fun onStartTrackingTouch(v: SeekBar) {
                ControllerSeekTracking = true
                ViewModel.cancelControllerDismissWithDelay()
            }
            override fun onStopTrackingTouch(v: SeekBar) {
                ControllerSeekTracking = false
                ViewModel.Player.seekTo(v.progress.toLong())
                ViewModel.setControllerDismissWithDelay()
            }
        })
    }

    @CallSuper
    override fun onViewModelSetup() {
        ViewModel.Exception.observe(this) {
            Application.onToast(context, R.string.title_season_error, it.message, it.code)
        }
        ViewModel.PlayerLoading.observe(this) {
            ViewBinding.playerControllerLoading.animate(it, 100)
        }
        ViewModel.PlayerPlaying.observe(this) {
            ViewBinding.playerControllerPlay.setImageResource(
                it.take(R.drawable.ic_player_pause, R.drawable.ic_player_play)
            )
            setListenDuration(it)
        }
        ViewModel.ControllerVisibility.observe(this) { visible ->
            if (visible) {
                ViewBinding.playerController.visibility = View.VISIBLE
                ViewModel.setControllerDismissWithDelay(5000)
            }
            ViewBinding.playerController.animate(visible, 300) {
                if (!visible) {
                    ViewBinding.playerController.visibility = View.GONE
                }
            }
        }
        ViewModel.PlayerDuration.observe(this) { duration ->
            val durationText = getDurationString(duration)
            ViewBinding.playerControllerTimeTotal?.text = durationText
            ViewBinding.playerControllerSeek.max = duration.toInt()
            ViewModel.PlayerCurrentDuration.removeObservers(this)
            var durationTmp = -1L
            ViewModel.PlayerCurrentDuration.observe(this) { current ->
                ViewModel.PlayerLoading.postValue(current == durationTmp)
                durationTmp = current
                val currentTime = getDurationString(current)
                @SuppressLint("SetTextI18n")
                ViewBinding.playerControllerTimeDuration?.text = "${durationText}/${currentTime}"
                ViewBinding.playerControllerTimeCurrent?.text = currentTime
                if (!ControllerSeekTracking) {
                    ViewBinding.playerControllerSeek.progress = current.toInt()
                }
            }
        }
    }

    private var durationListener: Timer? = null
    private fun setListenDuration(isListen: Boolean) {
        if (isListen && durationListener == null) {
            durationListener = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            ViewModel.PlayerCurrentDuration.postValue(ViewModel.Player.currentPosition)
                        }
                    }
                }, 100, 1000)
            }
        } else if (!isListen && durationListener != null) {
            durationListener?.cancel()
            durationListener = null
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

    override fun onDestroy() {
        setListenDuration(false)
        super.onDestroy()
    }

    final override fun onCreateViewBinding(container: ViewGroup?) =
        FragmentPlayerBinding.inflate(layoutInflater, container, false)
}