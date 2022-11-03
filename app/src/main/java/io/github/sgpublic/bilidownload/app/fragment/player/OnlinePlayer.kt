package io.github.sgpublic.bilidownload.app.fragment.player

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import bilibili.pgc.gateway.player.v2.Playurl.PlayViewReply
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.dialog.PlayerPanel
import io.github.sgpublic.bilidownload.app.ui.list.EpisodeListAdapter
import io.github.sgpublic.bilidownload.app.ui.list.QualityListAdapter
import io.github.sgpublic.bilidownload.app.ui.smartDestroy
import io.github.sgpublic.bilidownload.app.viewmodel.OnlinePlayerModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.exsp.BangumiPreference
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp.SeasonInfoData
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.exsp.ExPreference

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 14:29
 */
class OnlinePlayer(activity: AppCompatActivity): BasePlayer<OnlinePlayerModel>(activity) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        super.onViewSetup()
        ViewBinding.playerControllerQuality?.visibility = View.VISIBLE
        ViewBinding.playerControllerQuality?.setOnClickListener {
            openQualityListPanel()
        }
        ViewModel.isCoverVisible.take(View.VISIBLE, View.GONE).let { visibility ->
            ViewBinding.playerCover?.visibility = visibility
            ViewBinding.playerPlayerCover?.visibility = visibility
        }
    }

    override fun onViewModelSetup() {
        super.onViewModelSetup()
        ViewModel.setOnResolvePlayDataListener { play ->
            if (ViewBinding.playerCover?.visibility != View.VISIBLE) {
                onPlay(play)
            } else {
                ViewBinding.playerPlayerCover?.visibility = View.VISIBLE
                ViewBinding.playerCover?.setOnClickListener {
                    log.debug("playerCover onClick")
                    ViewBinding.playerCover?.visibility = View.GONE
                    ViewBinding.playerPlayerCover?.visibility = View.GONE
                    onPlay(play)
                }
            }
        }
        ViewModel.SeasonData.observe(this) {
            ViewBinding.playerCover?.let { playerCover ->
                Glide.with(context)
                    .customLoad(it.refineCover)
                    .withCrossFade()
                    .withBlur()
                    .into(playerCover)
            }
            ViewBinding.playerControllerEpisode?.setOnClickListener {
                openEpisodeListPanel(ViewModel.EpisodeList.values)
            }
        }
        ViewModel.PlayerPlaying.observe(this) {
            if (it) {
                ViewBinding.playerCover?.visibility = View.GONE
                ViewBinding.playerPlayerCover?.visibility = View.GONE
            }
        }
        ViewModel.QualityDesc.observe(this) {
            ViewBinding.playerControllerQuality?.text = it
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
        ViewModel.isCoverVisible = false
        ViewModel.QualityDesc.postValue(video.info.newDescription)
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
        runOnUiThread {
            ViewModel.Player.setMediaSource(media)
            ViewModel.Player.prepare()
        }
    }

    private val BangumiPreference: BangumiPreference by lazy { ExPreference.get() }
    private fun openQualityListPanel() {
        val panel = PlayerPanel(context)
        val popup = XPopup.Builder(context)
            .popupPosition(PopupPosition.Right)
            .asCustom(panel)
        val adapter = QualityListAdapter()
        adapter.setData(ViewModel.QualityData.entries)
        adapter.setSelection(ViewModel.FittedQuality)
        adapter.setOnItemClickListener { (qn, name) ->
            popup.dismiss()
            BangumiPreference.quality = qn
            ViewBinding.playerControllerQuality?.text = name
            val epid = ViewModel.EpisodeId.value ?: return@setOnItemClickListener
            ViewModel.Player.stop()
            ViewModel.getPlayUrl(epid.first, epid.second)
        }
        panel.setQualityAdapter(adapter)
        popup.show()
    }

    private fun openEpisodeListPanel(list: Collection<SeasonInfoData.Episodes.EpisodesData.EpisodesItem>) {
        val panel = PlayerPanel(context)
        val popup = XPopup.Builder(context)
            .popupPosition(PopupPosition.Right)
            .asCustom(panel)
        val adapter = EpisodeListAdapter()
        adapter.setData(list)
        adapter.setSelectedEpid(ViewModel.EpisodeId.value?.first ?: 0)
        panel.setEpisodeAdapter(adapter)
        popup.show()
        adapter.setOnItemClickListener {
            ViewModel.Player.stop()
            ViewModel.getPlayUrl(it.id, it.cid!!)
            popup.dismiss()
        }
    }

    override val ViewModel: OnlinePlayerModel by activityViewModels()
}