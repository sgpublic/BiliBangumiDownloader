package io.github.sgpublic.bilidownload.ui.list

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.data.SeriesData
import io.github.sgpublic.bilidownload.databinding.ItemBangumiFollowBinding
import io.github.sgpublic.bilidownload.ui.addOnReadyListener

class SeriesListAdapter(private val context: AppCompatActivity, list: List<SeriesData>)
    : ArrayAdapter<SeriesData>(context, R.layout.item_bangumi_follow, list) {
    private val nightMode: Boolean = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private var onClick: (Long) -> Unit = { }
    fun setOnItemClickListener(onClick: (Long) -> Unit) {
        this.onClick = onClick
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val data = getItem(position)!!
        val binding = if (convertView != null){
            ItemBangumiFollowBinding.bind(convertView)
        } else {
            ItemBangumiFollowBinding.inflate(LayoutInflater.from(context), parent, false)
        }
        binding.followContent.text = data.title
        if (data.badge == "") {
            binding.itemFollowBadgesBackground.visibility = View.GONE
        } else {
            binding.itemFollowBadgesBackground.visibility = View.VISIBLE
            if (nightMode) {
                binding.itemFollowBadgesBackground.setCardBackgroundColor(
                    data.badgeColorNight
                )
            } else {
                binding.itemFollowBadgesBackground.setCardBackgroundColor(data.badgeColor)
            }
            binding.itemFollowBadges.text = data.badge
        }
        binding.root.setOnClickListener {
            onClick(data.seasonId)
        }
        if (convertView != null) {
            binding.followImage.visibility = View.VISIBLE
            return binding.root
        }
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.pic_doing_v)
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(context)
            .load(data.cover)
            .apply(requestOptions)
            .addOnReadyListener {
                binding.followImage.visibility = View.VISIBLE
                binding.followImage.animate().alpha(1f)
                    .setDuration(400).setListener(null)
            }
            .into(binding.followImage)
        return binding.root
    }
}