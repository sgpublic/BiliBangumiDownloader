package io.github.sgpublic.bilidownload.ui

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.zhpan.bannerview.holder.ViewHolder
import io.github.sgpublic.bilidownload.R
import java.util.*

class SeasonBannerAdapter : ViewHolder<SeasonBannerAdapter.BannerItem> {
    override fun getLayoutId(): Int {
        return R.layout.item_bangumi_banner
    }

    override fun onBind(itemView: View, data: BannerItem, position: Int, size: Int) {
        val bannerImagePlaceholder = itemView.findViewById<ImageView>(R.id.banner_image_placeholder)
        val bannerImage = itemView.findViewById<ImageView>(R.id.banner_image)
        val bannerImageForeground = itemView.findViewById<ImageView>(R.id.banner_image_foreground)
        val itemBannerBadgeBackground: CardView = itemView.findViewById(R.id.item_banner_badge_background)
        if (data.badge == "") {
            itemBannerBadgeBackground.visibility = View.GONE
        } else {
            itemBannerBadgeBackground.visibility = View.VISIBLE
            itemBannerBadgeBackground.setCardBackgroundColor(data.badgeColor)
            itemView.findViewById<TextView>(R.id.item_banner_badge).text = data.badge
        }
        if (data.bannerPath == "") {
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.pic_doing_v)
                .error(R.drawable.pic_load_failed)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            Glide.with(data.context)
                .load(data.seasonCover)
                .apply(requestOptions)
                .addListener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        bannerImagePlaceholder.animate().alpha(0f).setDuration(400).setListener(null)
                        Timer().schedule(object : TimerTask(){
                            override fun run() {
                                data.context.runOnUiThread {
                                    bannerImagePlaceholder.visibility = View.GONE
                                    bannerImageForeground.visibility = View.VISIBLE
                                    bannerImageForeground.animate().alpha(1f).setDuration(400).setListener(null)
                                }
                            }
                        }, 400)
                        return false
                    }
                })
                .into(bannerImageForeground)
            Glide.with(data.context)
                .load(data.seasonCover)
                .apply(requestOptions)
                .apply(RequestOptions
                    .bitmapTransform(BlurHelper())
                )
                .addListener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        bannerImagePlaceholder.animate().alpha(0f).setDuration(400).setListener(null)
                        Timer().schedule(object : TimerTask(){
                            override fun run() {
                                data.context.runOnUiThread {
                                    bannerImage.visibility = View.VISIBLE
                                    bannerImage.animate().alpha(1f).setDuration(400).setListener(null)
                                }
                            }
                        }, 400)
                        return false
                    }
                })
                .into(bannerImage)
        } else {
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.pic_doing_h)
                .error(R.drawable.pic_load_failed)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            Glide.with(data.context)
                .load(data.bannerPath)
                .apply(requestOptions)
                .addListener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        bannerImagePlaceholder.animate().alpha(0f).setDuration(400).setListener(null)
                        Timer().schedule(object : TimerTask(){
                            override fun run() {
                                data.context.runOnUiThread {
                                    bannerImage.visibility = View.VISIBLE
                                    bannerImage.animate().alpha(1f).setDuration(400).setListener(null)
                                }
                            }
                        }, 400)
                        return false
                    }
                })
                .into(bannerImage)
        }
        itemView.findViewById<TextView>(R.id.banner_content).text = data.indicatorText
    }

    class BannerItem(
        val context: AppCompatActivity,
        val bannerPath: String,
        val seasonCover: String,
        val seasonId: Long,
        val title: String,
        private val indicator: String,
        val badge: String,
        val badgeColor: Int
    ) {
        val indicatorText: String get() = "$title：$indicator"

        override fun equals(other: Any?): Boolean {
            if (other is BannerItem) {
                return other.seasonId == seasonId
            }
            if (other is FollowItem) {
                return other.sid == seasonId
            }
            return false
        }

        override fun hashCode(): Int {
            return seasonId.hashCode()
        }
    }
}