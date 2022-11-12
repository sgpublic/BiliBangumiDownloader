package io.github.sgpublic.bilidownload.app.fragment.player

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupPosition
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.app.dialog.PlayerPanel
import io.github.sgpublic.bilidownload.app.ui.list.LocalEpisodeListAdapter
import io.github.sgpublic.bilidownload.app.viewmodel.LocalPlayerModel
import io.github.sgpublic.bilidownload.base.app.postValue
import io.github.sgpublic.bilidownload.core.room.entity.DownloadTaskEntity
import io.github.sgpublic.bilidownload.core.util.RequestCallback
import io.github.sgpublic.bilidownload.core.util.child
import io.github.sgpublic.bilidownload.core.util.log
import java.io.File

/**
 *
 * @author Madray Haven
 * @date 2022/10/27 14:29
 */
class LocalPlayer(
    private val sid: Long,
    private val epid: Long,
    activity: AppCompatActivity,
): BasePlayer<LocalPlayerModel>(activity) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        super.onViewSetup()
        ViewBinding.playerControllerQuality?.visibility = View.VISIBLE
        ViewBinding.playerControllerQuality?.setOnClickListener { }
        ViewBinding.playerControllerEpisode?.setOnClickListener {
            openEpisodeListPanel()
        }
    }

    override fun onViewModelSetup() {
        super.onViewModelSetup()
        ViewModel.PlayerData.observe(this, ::onPlay)
        ViewModel.PlayerPlaying.observe(this) {
            if (it) {
                ViewBinding.playerCover?.visibility = View.GONE
                ViewBinding.playerPlayerCover?.visibility = View.GONE
            }
        }
    }

    private val BasePath: File by lazy {
        context.getExternalFilesDir("Download")!!.child("s_${ViewModel.SID}")
    }

    private fun onPlay(data: DownloadTaskEntity) {
        val factory = ProgressiveMediaSource.Factory(FileDataSource.Factory())
        val EpisodeBasePath = BasePath.child("ep${data.epid}")
        // TODO 显示字幕
        val video = EpisodeBasePath.child("video.m4s")
        val audio = EpisodeBasePath.child("audio.m4s")
        val mediaSource = arrayListOf<MediaSource>(
            factory.createMediaSource(MediaItem.fromUri(video.toUri())),
            factory.createMediaSource(MediaItem.fromUri(audio.toUri()))
        )
        if (!video.exists() || !audio.exists()) {
            ViewModel.Exception.postValue(
                RequestCallback.CODE_PLAYER_QUALITY,
                Application.getString(R.string.text_player_quality)
            )
            return
        }
        ViewModel.isCoverVisible = false
        ViewModel.QualityDesc.postValue(data.qnDesc)
        ViewModel.EpisodeId.postValue(data.epid)
        val media = MergingMediaSource(true, *mediaSource.toTypedArray())
        log.info("onResolvePlayData: " +
                "\n  - video: $video" +
                "\n  - audio: $audio")
        runOnUiThread {
            ViewModel.Player.setMediaSource(media)
            ViewModel.Player.prepare()
        }
    }

    private fun openEpisodeListPanel() {
        val panel = PlayerPanel(context)
        val popup = XPopup.Builder(context)
            .popupPosition(PopupPosition.Right)
            .asCustom(panel)
        val adapter = LocalEpisodeListAdapter()
        ViewModel.EpisodeList.observe(popup) {
            adapter.setData(it)
        }
        ViewModel.EpisodeId.observe(popup) {
            adapter.setSelectedEpid(it)
        }
        panel.setEpisodeAdapter(adapter)
        adapter.setOnItemClickListener {
            ViewModel.Player.stop()
            ViewModel.PlayerData.postValue(it)
            popup.dismiss()
        }
        popup.show()
    }

    override val ViewModel: LocalPlayerModel by activityViewModels {
        ViewModelFactory(sid, epid)
    }

    private class ViewModelFactory(
        private val sid: Long,
        private val epid: Long,
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Long::class.java, Long::class.java)
                .newInstance(sid, epid)
        }
    }
}