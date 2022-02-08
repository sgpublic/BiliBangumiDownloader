package io.github.sgpublic.bilidownload.base

import android.animation.Animator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.ui.PlayerGestureDetector
import io.github.sgpublic.bilidownload.ui.list.EpisodeListAdapter
import java.util.*

@Suppress("PrivatePropertyName", "PropertyName")
abstract class BasePlayer<T: Parcelable> : BaseActivity<ActivityPlayerBinding>(),
    Player.Listener {
    private val list: Array<out Parcelable> get() =
        intent.getParcelableArrayExtra(KEY_LIST)
            ?: arrayOf()
    protected val position: Int get() = intent.getIntExtra(KEY_POSITION, 0)

    private lateinit var episodeAdapter: EpisodeListAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        onPlayEpisode()
    }

    abstract fun onMapEpisodeList(data: T): String

    @Suppress("UNCHECKED_CAST")
    abstract fun onPlayEpisode(data: T = list[position] as T)

    protected lateinit var player: ExoPlayer
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewSetup() {
        episodeAdapter = EpisodeListAdapter(this, position)
        @Suppress("UNCHECKED_CAST")
        episodeAdapter.setEpisodeList(list.map { onMapEpisodeList(it as T) })
        binding.playerPanelList.setSelection(position)
        episodeAdapter.setOnEpisodeChangeListener {
            PANEL_OPEN = false
            binding.playerPanelList.setSelection(it)
            @Suppress("UNCHECKED_CAST")
            onPlayEpisode(list[it] as T)
        }

        setSwipeBackEnable(false)
        player = ExoPlayer.Builder(this)
            .build()
        player.addListener(this)
        binding.playerExoplayer.player = player
        binding.playerExoplayer.setOnTouchListener(object : PlayerGestureDetector(this) {
            override fun onDoubleTap(e: MotionEvent) {
                PLAYER_PLAYING = !PLAYER_PLAYING
            }

            override fun onSingleTap(e: MotionEvent) {
                CONTROLLER_VISIBLE = !CONTROLLER_VISIBLE
                setControllerDismissWithDelay()
            }
        })

        binding.playerControllerBack.setOnClickListener {
            finish()
        }
        binding.playerControllerPlay.setOnClickListener {
            PLAYER_PLAYING = !PLAYER_PLAYING
        }
        binding.playerControllerEpisode.setOnClickListener {
            PANEL_EPISODE = !PANEL_EPISODE
        }
        binding.playerPanelBase.setOnClickListener {
            PANEL_OPEN = false
        }
        binding.playerControllerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(v: SeekBar, progress: Int, fromUser: Boolean) { }
            override fun onStartTrackingTouch(v: SeekBar) {
                CONTROLLER_SEEK_TRACKING = true
                cancelControllerDismissWithDelay()
            }
            override fun onStopTrackingTouch(v: SeekBar) {
                CONTROLLER_SEEK_TRACKING = false
                CONTROLLER_SEEK = v.progress.toLong()
                setControllerDismissWithDelay()
            }
        })
    }

    protected var PLAYER_PLAYING
        get() = player.isPlaying
        set(value) {
            if (!value) {
                player.pause()
                binding.playerControllerPlay.setImageResource(R.drawable.ic_player_play)
            } else {
                player.play()
                binding.playerControllerPlay.setImageResource(R.drawable.ic_player_pause)
                CONTROLLER_VISIBLE = false
            }
        }

    protected var PLAYER_LOADING
        get() = getAnimateState(binding.playerControllerLoading)
        set(value) {
            setAnimateState(value, binding.playerControllerLoading)
        }

    protected var CONTROLLER_VISIBLE
        get() = getAnimateState(binding.playerController)
        set(value) {
            if (value) {
                binding.playerController.visibility = View.VISIBLE
            }
            setAnimateState(value, binding.playerController) {
                if (!value) {
                    binding.playerController.visibility = View.GONE
                }
            }
        }

    protected val PANEL_GRAVITY = GravityCompat.END
    protected var PANEL_OPEN
        get() = binding.playerPanel.isDrawerOpen(PANEL_GRAVITY)
        set(value) {
            if (value) {
                binding.playerPanel.openDrawer(PANEL_GRAVITY)
            } else {
                binding.playerPanel.closeDrawer(PANEL_GRAVITY)
            }
        }

    protected var CONTROLLER_SEEK_TRACKING: Boolean = false
    protected var CONTROLLER_SEEK
        get() = player.currentPosition / 1000
        set(value) {
            PLAYER_LOADING = true
            player.seekTo(value * 1000)
            binding.playerControllerTimeCurrent.text =
                getDurationString(value)
        }

    private var PANEL_EPISODE
        get() = PANEL_OPEN
        set(value) {
            if (value) {
                val lp = binding.playerPanelList.layoutParams as ConstraintLayout.LayoutParams
                lp.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                binding.playerEpisodeListTitle.visibility = View.VISIBLE
                binding.playerPanelCover.layoutParams.width = Application.dip2px(320f)
                binding.playerPanelList.adapter = episodeAdapter
                episodeAdapter.notifyDataSetChanged()
            }
            PANEL_OPEN = value
        }

    private var durationListener: Timer? = null
    private var listenerDuration: Boolean = false
    private var LISTEN_DURATION
        get() = listenerDuration
        set(value) {
            if (value == listenerDuration) {
                return
            }
            listenerDuration = value
            if (!value) {
                durationListener?.cancel()
                durationListener = null
                return
            }
            if (durationListener == null) {
                durationListener = Timer()
            }
            durationListener?.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        binding.playerControllerTimeCurrent.text =
                            getDurationString(CONTROLLER_SEEK)
                        if (!CONTROLLER_SEEK_TRACKING) {
                            binding.playerControllerSeek.progress =
                                CONTROLLER_SEEK.toInt()
                        }
                    }
                }
            }, 0, 500)
        }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                PLAYER_PLAYING = true
                binding.playerControllerTimeTotal.text = getDurationString(
                    player.duration / 1000
                )
                binding.playerControllerSeek.max = (player.duration / 1000).toInt()
            }
            Player.STATE_ENDED -> {
                if (ConfigManager.PLAYER_AUTO_NEXT) {
                    episodeAdapter.next()
                }
            }
            Player.STATE_BUFFERING -> {

            }
            Player.STATE_IDLE -> {

            }
        }
    }

    fun getDurationString(duration: Long): String {
        val h = "0${duration / 60}".run {
            substring(length - 2)
        }
        val s = "0${duration % 60}".run {
            substring(length - 2)
        }
        return "$h:$s"
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        if (!isLoading) {
            PLAYER_LOADING = false
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        LISTEN_DURATION = isPlaying
        if (isPlaying) {
            PLAYER_LOADING = false
        }
    }

    private val animate = mutableMapOf<View, ViewState>()
    private fun setAnimateState(isVisible: Boolean, view: View, callback: () -> Unit = { }) {
        if (!animate.containsKey(view)) {
            animate[view] = ViewState()
        }
        val state = animate[view]!!
        if (state.visible == isVisible) {
            return
        }
        state.visible = isVisible
        state.animate?.cancel()
        state.animate = view.animate().apply {
            if (isVisible) {
                alphaBy(0f).alpha(1f)
            } else {
                alphaBy(1f).alpha(0f)
            }
        }.setDuration(300).setListener(object : AnimateListener {
            override fun onAnimationEnd(p0: Animator?) {
                state.animate = null
                callback()
            }
        })
    }
    private fun getAnimateState(view: View): Boolean =
        animate[view]?.visible ?: false
    private data class ViewState(
        var visible: Boolean = false,
        var animate: ViewPropertyAnimator? = null
    )

    private var controllerDismissTimer: Timer? = null
    fun setControllerDismissWithDelay(delay: Long = 5000) {
        controllerDismissTimer?.cancel()
        if (CONTROLLER_VISIBLE) {
            controllerDismissTimer = Timer()
            controllerDismissTimer?.schedule(object : TimerTask() {
                override fun run() {
                    controllerDismissTimer = null
                    runOnUiThread {
                        CONTROLLER_VISIBLE = false
                    }
                }
            }, delay)
        }
    }
    fun cancelControllerDismissWithDelay() {
        controllerDismissTimer?.cancel()
    }

    override fun onBackPressed() {
        if (PANEL_OPEN) {
            PANEL_OPEN = false
        } else {
            PLAYER_PLAYING = false
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        player.release()
        LISTEN_DURATION = false
        animate.clear()
        super.onDestroy()
    }

    private interface AnimateListener : Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator?) {}
        override fun onAnimationCancel(p0: Animator?) {}
        override fun onAnimationRepeat(p0: Animator?) {}
        override fun onAnimationEnd(p0: Animator?)
    }

    companion object {
        const val KEY_POSITION = "pos"
        const val KEY_LIST = "list"
    }
}