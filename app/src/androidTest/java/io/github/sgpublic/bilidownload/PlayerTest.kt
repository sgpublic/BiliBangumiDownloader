package io.github.sgpublic.bilidownload

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import io.github.sgpublic.bilidownload.util.MyLog
import io.github.sgpublic.bilidownload.viewmodel.OnlinePlayerViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerTest: ApplicationText() {
    private lateinit var accessKey: String
    private lateinit var player: ExoPlayer

    @Before
    fun createPlayer() {
        MyLog.i("onCreatePlayer")
        player = ExoPlayer.Builder(applicationContext)
            .build()
        val sp = applicationContext.getSharedPreferences("user", Context.MODE_PRIVATE)
        accessKey = sp.getString("access_token", null) ?: throw IllegalStateException("empty access_token!")
        MyLog.d("accessKey: $accessKey")
    }

//    @After
//    fun releasePlayer() {
//        MyLog.i("onReleasePlayer")
//        runOnUiThread {
//            player.release()
//        }
//    }

    @Test
    fun startPlay() {
        val obj = Object()
        runOnUiThread {
            MyLog.w("startPlay")
            @Suppress("LocalVariableName")
            val ViewModel = OnlinePlayerViewModel(40165)
            ViewModel.EXCEPTION.observeForever {
                throw IllegalStateException("code: ${it.code}, message: ${it.message}")
            }
            ViewModel.EPISODE_LIST.observeForever {
                MyLog.d("EPISODE_LIST: ${it.size}")
                ViewModel.requestPlayEpisode(0)
            }
            ViewModel.setOnPlayIndexChangeListener {
                ViewModel.getPlayData(ViewModel.getPlayerCurrentEpisode())
                ViewModel.DASH_INDEX_JSON.observeForever { index ->
                    val factory = ProgressiveMediaSource.Factory(
                        DefaultHttpDataSource.Factory()
                    )
//                    val media = MergingMediaSource(true,
//                        factory.createMediaSource(MediaItem.fromUri(index.video.base_url)),
//                        factory.createMediaSource(MediaItem.fromUri(index.audio.base_url))
//                    )
                    val media = factory.createMediaSource(MediaItem.fromUri("http://oi.sgpublic.xyz/?/Bangumi/%E7%81%B0%E8%89%B2%E4%B8%89%E9%83%A8%E6%9B%B2/" +
                            "%E7%81%B0%E8%89%B2%E7%9A%84%E6%9E%9C%E5%AE%9E/" +
                            "%5BKissSub%26FZSD%5D%5BGrisaia_no_Kajitsu%5D%5BBDRip%5D%5B01%5D%5BCHS%5D%5B720P%5D%5BAVC_AAC%5D%28E141FF2A%29.mp4"))
//                    MyLog.d("onResolvePlayData: " +
//                            "\n  - video: ${index.video.base_url}" +
//                            "\n  - audio: ${index.audio.base_url}")
                    ViewModel.PLAYER.observeForever { player ->
                        player.setMediaSource(media)
                        player.prepare()
                    }
                }
            }
            ViewModel.PLAYER_STATE.observeForever {
                if (it == Player.STATE_READY) {
                    Thread {
                        Thread.sleep(1000)
                        synchronized(obj) {
                            obj.notify()
                        }
                    }.run()
                }
            }
            ViewModel.SEASON_DATA.observeForever { }
        }
        synchronized(obj) {
            obj.wait()
        }
    }
}