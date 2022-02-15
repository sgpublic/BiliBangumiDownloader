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
import io.github.sgpublic.bilidownload.data.SeasonData
import io.github.sgpublic.bilidownload.databinding.ItemSearchSeasonBinding
import kotlin.math.roundToInt

class RecommendSeasonListAdapter(private val context: AppCompatActivity, list: List<SeasonData>)
    : ArrayAdapter<SeasonData>(context, R.layout.item_search_season, list) {
    private val nightMode: Boolean = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private var onClick: (Long) -> Unit = { }
    fun setOnItemClickListener(onClick: (Long) -> Unit) {
        this.onClick = onClick
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val data = getItem(position)!!
        val binding = if (convertView != null){
            ItemSearchSeasonBinding.bind(convertView)
        } else {
            ItemSearchSeasonBinding.inflate(LayoutInflater.from(context), parent, false)
        }
        binding.itemSearchSeasonTitle.text = data.info.title
        if (data.info.badge == "") {
            binding.itemSeasonBadges.visibility = View.GONE
        } else {
            binding.itemSeasonBadges.visibility = View.VISIBLE
            binding.itemSeasonBadges.text = data.info.badge
        }
        if (data.rating == 0.0) {
            binding.itemSearchRatingNull.visibility = View.VISIBLE
            binding.itemSearchRatingString.visibility = View.INVISIBLE
        } else {
            binding.itemSearchRatingNull.visibility = View.INVISIBLE
            binding.itemSearchRatingString.visibility = View.VISIBLE
        }
        binding.itemSearchSeasonContent.text = data.styles
        binding.itemSearchRatingString.text = data.rating.toString()
        binding.itemSearchRatingStart.progress = data.rating.roundToInt()
        binding.root.setOnClickListener {
            onClick(data.info.seasonId)
        }
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.pic_doing_v)
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(context)
            .load(data.info.cover)
            .apply(requestOptions)
            .into(binding.itemSearchSeasonCover)
        return binding.root
    }
}