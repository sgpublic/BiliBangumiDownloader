package io.github.sgpublic.bilidownload.app.ui.list

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.forest.data.SeasonInfoResp
import io.github.sgpublic.bilidownload.core.util.customLoad
import io.github.sgpublic.bilidownload.core.util.withCrossFade
import io.github.sgpublic.bilidownload.core.util.withVerticalPlaceholder
import io.github.sgpublic.bilidownload.databinding.ItemBangumiFollowBinding

class SeriesListAdapter(private val context: AppCompatActivity)
    : ArrayAdapter<SeasonInfoResp.SeasonInfoData.Seasons.SeasonData.SeasonItem>(
    context, R.layout.item_bangumi_follow, ArrayList<SeasonInfoResp.SeasonInfoData.Seasons.SeasonData.SeasonItem>()
) {
    private var onClick: (Long) -> Unit = { }
    fun setOnItemClickListener(onClick: (Long) -> Unit) {
        this.onClick = onClick
    }

    private val list: ArrayList<SeasonInfoResp.SeasonInfoData.Seasons.SeasonData.SeasonItem> = ArrayList()
    override fun getItem(position: Int): SeasonInfoResp.SeasonInfoData.Seasons.SeasonData.SeasonItem {
        return list[position]
    }

    override fun getCount(): Int {
        return list.size
    }

    fun setData(list: Collection<SeasonInfoResp.SeasonInfoData.Seasons.SeasonData.SeasonItem>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val data = getItem(position)
        val binding = if (convertView != null){
            ItemBangumiFollowBinding.bind(convertView)
        } else {
            ItemBangumiFollowBinding.inflate(LayoutInflater.from(context), parent, false)
        }
        binding.followContent.text = data.seasonTitle
        if (data.badgeInfo.img != null) {
            Glide.with(context)
        }
        binding.root.setOnClickListener {
            onClick(data.seasonId)
        }
        if (convertView != null) {
            binding.followImage.visibility = View.VISIBLE
            return binding.root
        }
        Glide.with(context)
            .customLoad(data.cover)
            .withVerticalPlaceholder()
            .withCrossFade()
            .into(binding.followImage)
        return binding.root
    }
}