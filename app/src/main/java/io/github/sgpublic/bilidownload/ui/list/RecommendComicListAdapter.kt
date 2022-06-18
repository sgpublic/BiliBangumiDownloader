package io.github.sgpublic.bilidownload.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.data.ComicData
import io.github.sgpublic.bilidownload.databinding.ItemSearchEpisodeBinding
import io.github.sgpublic.bilidownload.ui.customLoad
import io.github.sgpublic.bilidownload.ui.withCrossFade
import io.github.sgpublic.bilidownload.ui.withHorizontalPlaceholder

class RecommendComicListAdapter(private val context: AppCompatActivity, list: List<ComicData>)
    : ArrayAdapter<ComicData>(context, R.layout.item_search_episode, list) {
    private var onClick: (ComicData) -> Unit = { }
    fun setOnItemClickListener(onClick: (ComicData) -> Unit) {
        this.onClick = onClick
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val data = getItem(position)!!
        val binding = if (convertView != null){
            ItemSearchEpisodeBinding.bind(convertView)
        } else {
            ItemSearchEpisodeBinding.inflate(LayoutInflater.from(context), parent, false)
        }
        binding.itemEpisodeBadges.text = "漫画"
        binding.itemSearchEpisodeTitle.text = data.title
        binding.itemSearchEpisodeFrom.text = data.desc2
        binding.root.setOnClickListener {
            onClick(data)
        }
        if (convertView != null) {
            binding.itemSearchEpisodeCover.visibility = View.VISIBLE
            return binding.root
        }
        Glide.with(context)
            .customLoad(data.pic)
            .withHorizontalPlaceholder()
            .withCrossFade()
            .into(binding.itemSearchEpisodeCover)
        return binding.root
    }
}