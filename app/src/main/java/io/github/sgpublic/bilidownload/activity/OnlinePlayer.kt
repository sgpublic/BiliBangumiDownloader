package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BasePlayer
import io.github.sgpublic.bilidownload.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.module.PlayModule
import io.github.sgpublic.bilidownload.ui.list.QualityListAdapter

@Suppress("PrivatePropertyName")
class OnlinePlayer: BasePlayer<EpisodeData>(), Player.Listener {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.playerControllerQuality.text =
            ConfigManager.QUALITIES[ConfigManager.DEFAULT_QUALITY]
    }

    override fun onMapEpisodeList(data: EpisodeData): String =
        "${data.index} ${data.title}"

    override fun onPlayEpisode(data: EpisodeData) {
        CONTROLLER_VISIBLE = true
        PLAYER_LOADING = true
        binding.playerControllerTitle.text = data.title
        binding.playerControllerQuality.visibility = View.VISIBLE

        val module = PlayModule(this@OnlinePlayer, ConfigManager.ACCESS_TOKEN)
        module.getPlayUrl(data.cid, data.payment, ConfigManager.QUALITY, object : PlayModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_bangumi_play, message, code)
                finish()
            }

            override fun onResolveAvailableQuality(qualities: Map<Int, String>) {
                runOnUiThread {
                    qualityAdapter.setQualityList(qualities)
                }
            }

            override fun onResolvePlayUrl(url: PlayModule.PlayUrl, qn: Int) {
                val dataSourceFactory: DataSource.Factory =
                    DefaultHttpDataSource.Factory()
                val factory = ProgressiveMediaSource.Factory(dataSourceFactory)
                val media = MergingMediaSource(true,
                    factory.createMediaSource(MediaItem.fromUri(url.video)),
                    factory.createMediaSource(MediaItem.fromUri(url.audio))
                )
                runOnUiThread {
                    qualityAdapter.setCurrentQuality(qn)
                    player.setMediaSource(media)
                    player.prepare()
                }
            }
        })
    }

    override fun onViewSetup() {
        super.onViewSetup()
        qualityAdapter = QualityListAdapter(this@OnlinePlayer)
        qualityAdapter.setOnItemClickListener {
            ConfigManager.QUALITY = it
            PANEL_OPEN = false
            onPlayEpisode()
        }
        qualityAdapter.setOnQualityChangeListener {
            binding.playerControllerQuality.text = it
        }
        binding.playerControllerQuality.setOnClickListener {
            PANEL_QUALITY = !PANEL_QUALITY
        }
    }

    private lateinit var qualityAdapter: QualityListAdapter
    private var PANEL_QUALITY get() = PANEL_OPEN
        set(value) {
            if (value) {
                val lp = binding.playerPanelList.layoutParams as ConstraintLayout.LayoutParams
                lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                binding.playerEpisodeListTitle.visibility = View.GONE
                binding.playerPanelCover.layoutParams.width = Application.dip2px(180f)
                binding.playerPanelList.adapter = qualityAdapter
                qualityAdapter.notifyDataSetChanged()
            }
            PANEL_OPEN = value
        }

    companion object {
        fun startActivity(context: Context, epList: Array<EpisodeData>, pos: Int) {
            val intent = Intent(context, OnlinePlayer::class.java)
            intent.putExtra(KEY_LIST, epList)
            intent.putExtra(KEY_POSITION, pos)
            context.startActivity(intent)
        }
    }
}