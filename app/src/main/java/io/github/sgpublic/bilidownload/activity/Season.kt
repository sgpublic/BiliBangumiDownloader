package io.github.sgpublic.bilidownload.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayoutMediator
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseActivity
import io.github.sgpublic.bilidownload.base.CrashHandler
import io.github.sgpublic.bilidownload.data.SeasonData
import io.github.sgpublic.bilidownload.data.SeriesData
import io.github.sgpublic.bilidownload.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.databinding.ActivitySeasonBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import io.github.sgpublic.bilidownload.module.PlayModule
import io.github.sgpublic.bilidownload.module.SeasonModule
import io.github.sgpublic.bilidownload.ui.BlurHelper
import io.github.sgpublic.bilidownload.ui.fragment.SeasonFragmentAdapter
import java.util.*

class Season: BaseActivity<ActivitySeasonBinding>() {
    private lateinit var episodeData: List<EpisodeData>
    private lateinit var seasonData: SeasonData
    private var seasonInfo: SeriesData = SeriesData()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val module = SeasonModule(this@Season, seasonInfo.seasonId, ConfigManager.ACCESS_TOKEN)
        module.getInfoBySid(object : SeasonModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_bangumi_load, message, code)
                runOnUiThread {
                    stopOnLoadingState()
                    binding.seasonLoading.setImageResource(R.drawable.pic_load_failed)
                }
                CrashHandler.saveExplosion(e, code)
            }

            override fun onResult(episodeData: List<EpisodeData>, seasonData: SeasonData) {
                this@Season.episodeData = episodeData
                this@Season.seasonInfo = seasonData.baseInfo
                this@Season.seasonData = seasonData
                onSetupSeasonInfo()
            }
        })
    }

    @Deprecated("get when use.")
    private fun getAvailableQuality() {
        val module = PlayModule(this@Season, ConfigManager.ACCESS_TOKEN)
        module.getAvailableQuality(episodeData, object : PlayModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onToast(R.string.error_bangumi_load, message, code)
                runOnUiThread {
                    stopOnLoadingState()
                    binding.seasonLoading.setImageResource(R.drawable.pic_load_failed)
                }
                CrashHandler.saveExplosion(e, code)
            }

            override fun onResolveAvailableQuality(qualities: Map<Int, String>) {
                Thread.sleep(500)
                try {
                    onSetupSeasonInfo()
                } catch (_: NullPointerException) { }
            }
        })
    }

    override fun onViewSetup() {
        startOnLoadingState(binding.seasonLoading)
        setSupportActionBar(binding.seasonToolbar)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
        }

        val requestOptions = RequestOptions()
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

        seasonInfo.seasonId = intent.getLongExtra("season_id", 0)
        seasonInfo.cover = intent.getStringExtra("cover_url").toString()
        seasonInfo.title = intent.getStringExtra("title").toString()
        binding.seasonCollapsingToolbar.title = seasonInfo.title
        Glide.with(this)
            .load(seasonInfo.cover)
            .apply(requestOptions)
            .apply(RequestOptions.bitmapTransform(BlurHelper()))
            .addListener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            setAnimateState(true, 400, binding.seasonCoverBackground)
                        }
                    }, 400)
                    return false
                }
            })
            .into(binding.seasonCoverBackground)

        Glide.with(this)
            .load(seasonInfo.cover)
            .apply(requestOptions.placeholder(R.drawable.pic_doing_v))
            .addListener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    setAnimateState(false, 400, binding.seasonCoverPlaceholder) {
                        setAnimateState(true, 400, binding.seasonCover)
                    }
                    return false
                }
            })
            .into(binding.seasonCover)
    }

    private fun onSetupSeasonInfo() {
        runOnUiThread {
            setAnimateState(false, 300, binding.seasonLoading)
            setAnimateState(false, 300, binding.seasonShow) {
                binding.seasonViewpager.adapter = SeasonFragmentAdapter(
                    this@Season, seasonData, episodeData
                )

                TabLayoutMediator(
                    binding.seasonTab, binding.seasonViewpager
                ) { tab, position ->
                    tab.text = when(position) {
                        0 -> getString(R.string.title_season_info)
                        1 -> getString(R.string.title_season_episode)
                        else -> ""
                    }
                }.attach()
                setAnimateState(true, 500, binding.seasonShow)
                stopOnLoadingState()
            }
        }
    }

    private var timer: Timer? = null
    private var imageIndex = 0
    private fun startOnLoadingState(imageView: ImageView) {
        imageView.visibility = View.VISIBLE
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                imageIndex = if (imageIndex == R.drawable.pic_search_doing_1)
                    R.drawable.pic_search_doing_2 else R.drawable.pic_search_doing_1
                runOnUiThread { imageView.setImageResource(imageIndex) }
            }
        }, 0, 500)
    }

    private fun stopOnLoadingState() {
        timer?.let {
            it.cancel()
            timer = null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    companion object {
        fun startActivity(context: Context, title: String?, sid: Long, cover_url: String?) {
            val intent = Intent(context, Season::class.java)
            intent.putExtra("season_id", sid)
            intent.putExtra("cover_url", cover_url)
            intent.putExtra("title", title)
            context.startActivity(intent)
        }
    }
}