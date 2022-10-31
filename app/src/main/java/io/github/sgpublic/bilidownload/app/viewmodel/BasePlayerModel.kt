package io.github.sgpublic.bilidownload.app.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.util.log
import java.util.*

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 14:32
 */
abstract class BasePlayerModel: BaseViewModel(), Player.Listener {
    val Player: ExoPlayer by lazy {
        val player = ExoPlayer.Builder(Application.APPLICATION_CONTEXT)
            .build()
        player.addListener(this)
        player.playWhenReady = true
        return@lazy player
    }

    override fun onPlayerError(error: PlaybackException) {
        log.error("player error", error)
        Exception.postValue(-2000, error.message)
    }

    val PlayerPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val PlayerLoading: MutableLiveData<Boolean> = MutableLiveData()

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            PlayerLoading.postValue(false)
        }
        PlayerPlaying.postValue(isPlaying)
    }

    fun changePlayingState() {
        if (Player.isPlaying) {
            Player.pause()
        } else {
            Player.play()
        }
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        if (isLoading) {
            PlayerLoading.postValue(true)
        }
    }

    val PlayerDuration: MutableLiveData<Long> = MutableLiveData()
    val PlayerState: MutableLiveData<Int> = MutableLiveData()
    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == com.google.android.exoplayer2.Player.STATE_READY) {
            PlayerDuration.postValue(Player.duration)
        }
        PlayerState.postValue(playbackState)
    }
    val PlayerCurrentDuration: MutableLiveData<Long> = MutableLiveData()

    fun isControllerVisible(): Boolean = ControllerVisibility.value ?: false
    val ControllerVisibility: MutableLiveData<Boolean> = MutableLiveData()
    fun changeControllerVisibility() {
        ControllerVisibility.postValue(!isControllerVisible())
    }

    private var controllerDismissTimer: Timer? = null
    fun setControllerDismissWithDelay(delay: Long = 5000) {
        controllerDismissTimer?.cancel()
        if (isControllerVisible()) {
            controllerDismissTimer = Timer()
            controllerDismissTimer?.schedule(object : TimerTask() {
                override fun run() {
                    controllerDismissTimer = null
                    ControllerVisibility.postValue(false)
                }
            }, delay)
        }
    }
    fun cancelControllerDismissWithDelay() {
        controllerDismissTimer?.cancel()
    }

    @CallSuper
    override fun onCleared() {
        cancelControllerDismissWithDelay()
        Player.removeListener(this)
        Player.pause()
        Player.release()
    }
}