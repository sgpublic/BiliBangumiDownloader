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
import io.github.sgpublic.bilidownload.data.ComicData
import io.github.sgpublic.bilidownload.databinding.ItemSearchEpisodeBinding

class RecommendComicListAdapter(private val context: AppCompatActivity, list: List<ComicData>)
    : ArrayAdapter<ComicData>(context, R.layout.item_search_episode, list) {
    private val nightMode: Boolean = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private var onClick: (Long) -> Unit = { }
    fun setOnItemClickListener(onClick: (Long) -> Unit) {
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
            onClick(data.item_id)
        }
        if (convertView != null) {
            binding.itemSearchEpisodeCover.visibility = View.VISIBLE
            return binding.root
        }
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.pic_doing_v)
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(context)
            .load(data.pic)
            .apply(requestOptions)
            .into(binding.itemSearchEpisodeCover)
        return binding.root
    }
}