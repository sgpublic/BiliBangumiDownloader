package io.github.sgpublic.bilidownload.fragment.season

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.BaseFragment
import io.github.sgpublic.bilidownload.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.databinding.FragmentSeasonDownloadBinding
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager

class SeasonEpisode(private val contest: AppCompatActivity, private val episodeData: List<EpisodeData>)
    : BaseFragment<FragmentSeasonDownloadBinding>(contest) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        if (episodeData.isNotEmpty()) {
            ViewBinding.seasonEpisodeList.visibility = View.VISIBLE
            ViewBinding.seasonNoEpisode.visibility = View.GONE
        } else {
            ViewBinding.seasonEpisodeList.visibility = View.GONE
            ViewBinding.seasonNoEpisode.visibility = View.VISIBLE
        }
    }

    override fun onCreateViweBinding(container: ViewGroup?): FragmentSeasonDownloadBinding =
        FragmentSeasonDownloadBinding.inflate(layoutInflater, container, false)
    
    override fun onViewSetup() {
        if (episodeData.isEmpty()) {
            return
        }
        val viewWidth = (resources.displayMetrics.widthPixels - Application.dip2px(36f)) / 2
        val imageHeight = (viewWidth - Application.dip2px(12f)) / 8 * 5
        var rowCount = episodeData.size / 2
        if (episodeData.size % 2 != 0) {
            rowCount += 1
        }
        ViewBinding.seasonGrid.columnCount = 2
        ViewBinding.seasonGrid.rowCount = rowCount
        val nightMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        for (index in episodeData.indices) {
            val data = episodeData[index]
            val itemSeasonEpisode = ItemSeasonEpisodeBinding.inflate(layoutInflater, ViewBinding.seasonGrid, false)
            if (data.title == ""){
                itemSeasonEpisode.episodeTitle.visibility = View.GONE
            } else {
                itemSeasonEpisode.episodeTitle.text = data.title
            }
            itemSeasonEpisode.episodePublicTime.text = String.format(
                getString(R.string.text_episode_public_time),
                data.pubRealTime
            )
            var indexTitle = data.index
            try {
                indexTitle.toFloat()
                indexTitle = String.format(
                    getString(R.string.text_episode_index), indexTitle
                )
            } catch (ignore: NumberFormatException) {
            }
            itemSeasonEpisode.episodeIndexTitle.text = indexTitle
            if (data.badge == "") {
                itemSeasonEpisode.episodeVipBackground.visibility = View.GONE
            } else {
                itemSeasonEpisode.episodeVipBackground.visibility = View.VISIBLE
                if (nightMode) {
                    itemSeasonEpisode.episodeVipBackground.setCardBackgroundColor(data.badgeColorNight)
                } else {
                    itemSeasonEpisode.episodeVipBackground.setCardBackgroundColor(data.badgeColor)
                }
                itemSeasonEpisode.episodeVip.text = data.badge
            }
            itemSeasonEpisode.root.setOnClickListener {
                if (data.payment == EpisodeData.PAYMENT_VIP
                    && ConfigManager.VIP_STATE == 0) {
                    Application.onToast(context, R.string.text_episode_vip_needed)
                } else {
                    onSetupDownload(index)
                }
            }
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.pic_doing_h)
                .error(R.drawable.pic_load_failed)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            Glide.with(contest)
                .load(data.cover)
                .apply(requestOptions)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        startAnimate(true, 400, itemSeasonEpisode.episodeImage)
                        return false
                    }
                })
                .into(itemSeasonEpisode.episodeImage)
            itemSeasonEpisode.episodeImage.layoutParams.height = imageHeight
            val params = GridLayout.LayoutParams()
            params.rowSpec = GridLayout.spec(index / 2)
            params.columnSpec = GridLayout.spec(index % 2)
            params.width = viewWidth
            ViewBinding.seasonGrid.addView(itemSeasonEpisode.root, params)
        }
    }

    private fun onSetupDownload(pos: Int) {
        // TODO 添加下载任务
    }
}