package io.github.sgpublic.bilidownload.fragment.player

import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseViewModelActivity
import io.github.sgpublic.bilidownload.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.databinding.ActivityPlayerBinding
import io.github.sgpublic.bilidownload.dialog.PlayerPanel
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.ui.list.QualityListAdapter
import io.github.sgpublic.bilidownload.util.MyLog
import io.github.sgpublic.bilidownload.util.newObserve
import io.github.sgpublic.bilidownload.viewmodel.OnlinePlayerViewModel


@Suppress("PrivatePropertyName")
class OnlinePlayer(contest: BaseViewModelActivity<ActivityPlayerBinding, *>)
    : BasePlayer<EpisodeData, OnlinePlayerViewModel>(contest), Player.Listener {
    override val ViewModel: OnlinePlayerViewModel by activityViewModels()

    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        ViewBinding.playerControllerQuality?.text = ConfigManager.QUALITIES[ConfigManager.QUALITY]
        ViewBinding.playerControllerTitle?.text = ViewModel.getEntryJson()?.title ?: ""
    }

    override fun onPlayEpisode(data: EpisodeData) {
        ViewModel.CONTROLLER_VISIBILITY.postValue(true)
        ViewModel.PLAYER_LOADING.postValue(true)
        ViewBinding.playerControllerTitle?.text = data.title
        ViewBinding.playerControllerQuality?.visibility = View.VISIBLE
        ViewModel.EXCEPTION.newObserve(viewLifecycleOwner) {
            Application.onToast(context, R.string.error_bangumi_play, it.message, it.code)
        }
        ViewModel.ENTRY_JSON.newObserve(viewLifecycleOwner) {
            ViewBinding.playerControllerQuality?.text = ConfigManager.QUALITIES[it.video_quality]
            ViewBinding.playerControllerTitle?.text = it.title
        }
        ViewModel.DASH_INDEX_JSON.newObserve(viewLifecycleOwner) { index ->
            val factory = ProgressiveMediaSource.Factory(
                DefaultHttpDataSource.Factory()
            )
            val mediaSource = arrayListOf<MediaSource>(
                factory.createMediaSource(MediaItem.fromUri(index.video.base_url)),
                factory.createMediaSource(MediaItem.fromUri(index.audio.base_url))
            )
            var subtitleUrl = "(no subtitle)"
            if (index.subtitles.isNotEmpty()) {
                val subtitle = index.subtitles[0]
                subtitleUrl = subtitle.subtitle_url
                // TODO 显示字幕
//                mediaSource.add(factory.createMediaSource(
//                    MediaItem.Builder().setUri(subtitle.subtitle_url)
//                        .setMimeType(MimeTypes.TEXT_VTT).build()
//                ))
            }
            val media = MergingMediaSource(true, *mediaSource.toArray(arrayOf()))
            MyLog.d("onResolvePlayData: " +
                    "\n  - video: ${index.video.base_url}" +
                    "\n  - audio: ${index.audio.base_url}" +
                    if (index.subtitles.isEmpty()) "" else
                        "\n  - subtitle: $subtitleUrl")
            ViewModel.getPlayer()?.let { player ->
                player.setMediaSource(media)
                player.prepare()
            }
        }
        var seek = false
        ViewModel.PLAYER_DURATION.newObserve(viewLifecycleOwner) {
            if (seek) {
                return@newObserve
            }
            seek = true
            val duration = Application.DATABASE.WatchHistoryDao()
                .getDurationByCid(data.cid) ?: 0
            ViewModel.getPlayer()?.seekTo(duration.coerceAtLeast(0))
        }
        ViewModel.getPlayData(data)
    }

    override fun onViewSetup() {
        super.onViewSetup()
        ViewBinding.playerControllerQuality?.let {
            it.visibility = View.VISIBLE
            it.setOnClickListener {
                openQualityListPanel()
            }
        }
        ViewModel.PLAYER_PLAYING.newObserve(viewLifecycleOwner) {
            if (!it) {
                return@newObserve
            }
            ViewModel.PLAYER_CURRENT_DURATION.newObserve(viewLifecycleOwner) {
                Thread {
                    Application.DATABASE.WatchHistoryDao()
                        .updateDurationByCid(ViewModel.getPlayerCurrentEpisode().cid, it)
                }.run()
            }
        }
    }

    private fun openQualityListPanel() {
        val adapter = QualityListAdapter(context)
        val popup = PlayerPanel(context)
        val panel = XPopup.Builder(context)
            .popupPosition(PopupPosition.Right)
            .asCustom(popup)
        popup.setQualityAdapter(adapter)
        adapter.setSelection(ViewModel.getCurrentQuality())
        adapter.setQualityList(ViewModel.getQualityList())
        panel.show()
        adapter.setOnItemClickListener { pos, title ->
            panel.dismiss()
            if (ConfigManager.QUALITY != pos) {
                ConfigManager.QUALITY = pos
                ViewBinding.playerControllerQuality?.text = title
                onPlayEpisode()
            }
        }
    }
}