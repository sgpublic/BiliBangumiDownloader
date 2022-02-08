package io.github.sgpublic.bilidownload.fragment.season

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
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
import io.github.sgpublic.bilidownload.activity.Season
import io.github.sgpublic.bilidownload.base.BaseFragment
import io.github.sgpublic.bilidownload.data.SeasonData
import io.github.sgpublic.bilidownload.databinding.FragmentSeasonInfoBinding
import io.github.sgpublic.bilidownload.databinding.ItemBangumiFollowBinding
import kotlin.math.roundToInt

class SeasonInfo(private val contest: AppCompatActivity, private val seasonData: SeasonData) : BaseFragment<FragmentSeasonInfoBinding>(contest) {
    override fun onFragmentCreated(savedInstanceState: Bundle?) {

    }

    override fun onViewSetup() {
        if (seasonData.rating == 0.0) {
            binding.seasonRatingString.visibility = View.INVISIBLE
            binding.seasonRatingNull.visibility = View.VISIBLE
            binding.seasonRatingStar.progress = 0
        } else {
            binding.seasonRatingNull.visibility = View.INVISIBLE
            binding.seasonRatingString.visibility = View.VISIBLE
            binding.seasonRatingString.text = seasonData.rating.toString()
            binding.seasonRatingStar.progress = seasonData.rating.roundToInt()
        }
        binding.seasonStuff.setOnClickListener {
            binding.seasonStuff.maxLines =
                if (seasonData.staffLines == binding.seasonStuff.maxLines) 3 else seasonData.staffLines
        }
        binding.seasonActors.setOnClickListener {
            binding.seasonActors.maxLines =
                if (seasonData.actorsLines == binding.seasonActors.maxLines) 3 else seasonData.actorsLines
        }
        binding.seasonContent.text = seasonData.description
        if (seasonData.alias != "") {
            binding.seasonAliasBase.visibility = View.VISIBLE
            binding.seasonAlias.text = seasonData.alias
        }
        if (seasonData.styles != "") {
            binding.seasonStylesBase.visibility = View.VISIBLE
            binding.seasonStyles.text = seasonData.styles
        }
        if (seasonData.styles != "" || seasonData.alias != "") {
            binding.seasonAliasStylesBase.visibility = View.VISIBLE
        }
        if (seasonData.actors != "") {
            binding.seasonActorsBase.visibility = View.VISIBLE
            binding.seasonActors.text = seasonData.actors
        }
        if (seasonData.staff != "") {
            binding.seasonStuffBase.visibility = View.VISIBLE
            binding.seasonStuff.text = seasonData.staff
        }
        if (seasonData.evaluate != "") {
            binding.seasonEvaluateBase.visibility = View.VISIBLE
            binding.seasonEvaluate.text = seasonData.evaluate
        }
        if (seasonData.series.size == 0) {
            return
        }
        binding.seasonSeriesBase.visibility = View.VISIBLE
        var rowCount = seasonData.series.size / 3
        if (seasonData.series.size % 3 != 0) {
            rowCount += 1
        }
        binding.seasonSeries.rowCount = rowCount
        binding.seasonSeries.columnCount = 3

        val viewWidth = (resources.displayMetrics.widthPixels - Application.dip2px(56f)) / 3
        val imageHeight = (viewWidth - Application.dip2px(12f)) / 3 * 4
        val viewHeight = imageHeight + Application.dip2px(38f)

        var dataInfoIndex = 0

        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        for ((_, _, badge, badge_color, badge_color_night, cover, title, season_id) in seasonData.series) {
            val itemBangumeFollow = ItemBangumiFollowBinding.inflate(
                layoutInflater,
                binding.seasonSeries,
                false
            )
            itemBangumeFollow.followContent.text = title
            if (badge == "") {
                itemBangumeFollow.itemFollowBadgesBackground.visibility = View.GONE
            } else {
                itemBangumeFollow.itemFollowBadgesBackground.visibility = View.VISIBLE
                if (nightMode) {
                    itemBangumeFollow.itemFollowBadgesBackground.setCardBackgroundColor(
                        badge_color_night
                    )
                } else {
                    itemBangumeFollow.itemFollowBadgesBackground.setCardBackgroundColor(
                        badge_color
                    )
                }
                itemBangumeFollow.itemFollowBadges.text = badge
            }
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.pic_doing_v)
                .error(R.drawable.pic_load_failed)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            Glide.with(contest)
                .load(cover)
                .apply(requestOptions)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>,
                                              isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>,
                                                 dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        setAnimateState(false, 400, itemBangumeFollow.followImagePlaceholder) {
                            setAnimateState(true, 400, itemBangumeFollow.followImage)
                        }
                        return false
                    }
                })
                .into(itemBangumeFollow.followImage)
            itemBangumeFollow.followImage.layoutParams.height = imageHeight
            val params = GridLayout.LayoutParams()
            params.rowSpec = GridLayout.spec(dataInfoIndex / 3)
            params.columnSpec = GridLayout.spec(dataInfoIndex % 3)
            params.width = viewWidth
            params.height = viewHeight
            itemBangumeFollow.root.setOnClickListener {
                Season.startActivity(contest, title, season_id, cover)
            }
            binding.seasonSeries.addView(itemBangumeFollow.root, params)
            dataInfoIndex += 1
        }
    }
}