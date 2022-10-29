package io.github.sgpublic.bilidownload.app.fragment.player

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import bilibili.pgc.gateway.player.v2.Playurl.PlayViewReply
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.viewmodel.OnlinePlayerModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.FragmentPlayerBinding

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 14:29
 */
class OnlinePlayer(activity: AppCompatActivity): BasePlayer<FragmentPlayerBinding, OnlinePlayerModel>(activity) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        ViewBinding.playerCover?.setOnClickListener {
            val player = ViewModel.PlayerData.value ?: return@setOnClickListener
            ViewBinding.playerCover?.visibility = View.GONE
            ViewBinding.playerPlayerCover?.visibility = View.GONE
            onPlay(player)
        }
    }

    override fun onViewModelSetup() {
        ViewModel.PlayerData.observe(this) {
            if (ViewBinding.playerCover?.visibility != View.GONE) {
                return@observe
            }
            onPlay(it)
        }
        ViewModel.SeasonData.observe(this) {
            ViewBinding.playerCover?.let { playerCover ->
                Glide.with(context)
                    .customLoad(it.refineCover)
                    .withCrossFade()
                    .withBlur()
                    .into(playerCover)
            }
        }
    }

    private fun onPlay(data: PlayViewReply) {
        val video = data.videoInfo.streamListList.find {
            it.info.quality == ViewModel.FittedQuality
        }
        val audio = video?.let {
            data.videoInfo.dashAudioList.find {
                it.id == video.dashVideo.audioId
            }
        }
        if (video == null || audio == null) {
            ViewModel.Exception.postValue(
                RequestCallback.CODE_PLAYER_QUALITY,
                Application.getString(R.string.text_player_quality)
            )
            return
        }
        val factory = ProgressiveMediaSource.Factory(
            DefaultHttpDataSource.Factory()
        )
        val mediaSource = arrayListOf<MediaSource>(
            factory.createMediaSource(MediaItem.fromUri(video.dashVideo.baseUrl)),
            factory.createMediaSource(MediaItem.fromUri(audio.baseUrl))
        )
//        var subtitleUrl = "(no subtitle)"
//        if (index.subtitles.isNotEmpty()) {
//            val subtitle = index.subtitles[0]
//            subtitleUrl = subtitle.subtitle_url
//            // TODO 显示字幕
////                mediaSource.add(factory.createMediaSource(
////                    MediaItem.Builder().setUri(subtitle.subtitle_url)
////                        .setMimeType(MimeTypes.TEXT_VTT).build()
////                ))
//        }
        val media = MergingMediaSource(true, *mediaSource.toTypedArray())
        log.info("onResolvePlayData: " +
                "\n  - video: ${video.dashVideo.baseUrl}" +
                "\n  - audio: ${audio.baseUrl}")
        ViewModel.Player.setMediaSource(media)
        ViewModel.Player.prepare()
    }

    override val ViewModel: OnlinePlayerModel by activityViewModels()
    override fun onCreateViewBinding(container: ViewGroup?) =
        FragmentPlayerBinding.inflate(layoutInflater, container, false)
}