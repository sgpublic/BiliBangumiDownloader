package io.github.sgpublic.bilidownload.viewmodel

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.base.BaseViewModel
import java.util.*

@Suppress("PropertyName")
open class BasePlayerViewModel<T: Parcelable> : BaseViewModel(), Player.Listener {
    fun getPlayer() = PLAYER.value
    val PLAYER: MutableLiveData<ExoPlayer> by lazy {
        MutableLiveData<ExoPlayer>().also {
            val player: ExoPlayer =
                ExoPlayer.Builder(Application.APPLICATION)
                    .build()
            player.addListener(this)
            player.playWhenReady = true
            it.postValue(player)
        }
    }

    fun getEpisodeList(): List<T> = EPISODE_LIST.value ?: listOf()
    val EPISODE_LIST: MutableLiveData<List<T>> = MutableLiveData()

    fun getPlayerCurrentEpisodeIndex(): Int = PLAYER_EPISODE_INDEX.value ?: 0
    val PLAYER_EPISODE_INDEX: MutableLiveData<Int> = MutableLiveData()

    protected var onPlayIndexChange: (Int) -> Unit = { }
    fun setOnPlayIndexChangeListener(listener: (Int) -> Unit) {
        this.onPlayIndexChange = listener
    }
    fun requestPlayEpisode(index: Int) {
        PLAYER_EPISODE_INDEX.postValue(index)
        onPlayIndexChange(index)
    }

    fun getPlayerCurrentEpisode() = getEpisodeList()[getPlayerCurrentEpisodeIndex()]
    fun getPlayerEpisode(index: Int) = getEpisodeList()[index]

    fun getPlayerTitleList() = getEpisodeList().map { it.toString() }

    fun isPlayerPlaying(): Boolean = PLAYER_PLAYING.value ?: false
    val PLAYER_PLAYING: MutableLiveData<Boolean> = MutableLiveData()
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            PLAYER_LOADING.postValue(false)
        }
        PLAYER_PLAYING.postValue(isPlaying)
    }
    fun changePlayingState() {
        if (isPlayerPlaying()) {
            getPlayer()?.pause()
        } else {
            getPlayer()?.play()
        }
        PLAYER_PLAYING.postValue(!isPlayerPlaying())
    }

    fun isPlayerLoading(): Boolean = PLAYER_LOADING.value ?: false
    val PLAYER_LOADING: MutableLiveData<Boolean> = MutableLiveData()
    override fun onIsLoadingChanged(isLoading: Boolean) {
        if (isLoading) {
            PLAYER_LOADING.postValue(false)
        }
    }

    fun getPlayerState(): Int = PLAYER_STATE.value ?: 0
    val PLAYER_STATE: MutableLiveData<Int> = MutableLiveData()
    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            PLAYER_DURATION.postValue(getPlayer()?.duration)
        }
        PLAYER_STATE.postValue(playbackState)
    }

    fun isControllerVisible(): Boolean = CONTROLLER_VISIBILITY.value ?: false
    val CONTROLLER_VISIBILITY: MutableLiveData<Boolean> = MutableLiveData()
    fun changeControllerVisibility() {
        CONTROLLER_VISIBILITY.postValue(!isControllerVisible())
    }
    private var controllerDismissTimer: Timer? = null
    fun setControllerDismissWithDelay(delay: Long = 5000) {
        controllerDismissTimer?.cancel()
        if (isControllerVisible()) {
            controllerDismissTimer = Timer()
            controllerDismissTimer?.schedule(object : TimerTask() {
                override fun run() {
                    controllerDismissTimer = null
                    CONTROLLER_VISIBILITY.postValue(false)
                }
            }, delay)
        }
    }
    fun cancelControllerDismissWithDelay() {
        controllerDismissTimer?.cancel()
    }

    fun getPlayerDuration(): Long = PLAYER_DURATION.value ?: 0
    val PLAYER_DURATION: MutableLiveData<Long> = MutableLiveData()
    fun getPlayerIntDuration(): Int = getPlayerDuration().toInt()

    fun getPlayerCurrentDuration(): Long = PLAYER_CURRENT_DURATION.value ?: 0
    val PLAYER_CURRENT_DURATION: MutableLiveData<Long> = MutableLiveData()

    override fun onCleared() {
        cancelControllerDismissWithDelay()
        getPlayer()?.let {
            it.removeListener(this@BasePlayerViewModel)
            it.pause()
            it.release()
        }
        super.onCleared()
    }
}