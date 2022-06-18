package io.github.sgpublic.bilidownload.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.data.SeasonData
import io.github.sgpublic.bilidownload.databinding.ItemSearchSeasonBinding
import io.github.sgpublic.bilidownload.ui.customLoad
import io.github.sgpublic.bilidownload.ui.withCrossFade
import io.github.sgpublic.bilidownload.ui.withVerticalPlaceholder
import kotlin.math.roundToInt

class RecommendSeasonListAdapter(private val context: AppCompatActivity, list: List<SeasonData>)
    : ArrayAdapter<SeasonData>(context, R.layout.item_search_season, list) {

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
        Glide.with(context)
            .customLoad(data.info.cover)
            .withVerticalPlaceholder()
            .withCrossFade()
            .into(binding.itemSearchSeasonCover)
        return binding.root
    }
}