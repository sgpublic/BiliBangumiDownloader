package io.github.sgpublic.bilidownload.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.zhpan.bannerview.holder.ViewHolder
import io.github.sgpublic.bilidownload.R
import java.util.*

class SeasonBannerAdapter(private val context: AppCompatActivity) : ViewHolder<SeasonBannerAdapter.BannerItem> {
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
            Glide.with(context)
                .load(data.seasonCover)
                .apply(requestOptions)
                .addOnReadyListener {
                    bannerImagePlaceholder.animate().alpha(0f).setDuration(400).setListener(null)
                    Timer().schedule(object : TimerTask(){
                        override fun run() {
                            context.runOnUiThread {
                                bannerImagePlaceholder.visibility = View.GONE
                                bannerImageForeground.visibility = View.VISIBLE
                                bannerImageForeground.animate().alpha(1f).setDuration(400).setListener(null)
                            }
                        }
                    }, 400)
                }
                .into(bannerImageForeground)
            Glide.with(context)
                .load(data.seasonCover)
                .apply(requestOptions)
                .apply(RequestOptions
                    .bitmapTransform(BlurHelper())
                )
                .addOnReadyListener {
                    bannerImagePlaceholder.animate().alpha(0f).setDuration(400).setListener(null)
                    Timer().schedule(object : TimerTask(){
                        override fun run() {
                            context.runOnUiThread {
                                bannerImage.visibility = View.VISIBLE
                                bannerImage.animate().alpha(1f).setDuration(400).setListener(null)
                            }
                        }
                    }, 400)
                }
                .into(bannerImage)
        } else {
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.pic_doing_h)
                .error(R.drawable.pic_load_failed)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            Glide.with(context)
                .load(data.bannerPath)
                .apply(requestOptions)
                .addOnReadyListener {
                    bannerImagePlaceholder.animate().alpha(0f).setDuration(400).setListener(null)
                    Timer().schedule(object : TimerTask(){
                        override fun run() {
                            context.runOnUiThread {
                                bannerImage.visibility = View.VISIBLE
                                bannerImage.animate().alpha(1f).setDuration(400).setListener(null)
                            }
                        }
                    }, 400)
                }
                .into(bannerImage)
        }
        itemView.findViewById<TextView>(R.id.banner_content).text = data.indicatorText
    }

    class BannerItem(
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
            return false
        }

        override fun hashCode(): Int {
            return seasonId.hashCode()
        }
    }
}