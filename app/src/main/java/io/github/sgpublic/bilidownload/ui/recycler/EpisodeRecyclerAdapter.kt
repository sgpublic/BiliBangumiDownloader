package io.github.sgpublic.bilidownload.ui.recycler

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding

class EpisodeRecyclerAdapter(private val context: AppCompatActivity, private val list: List<EpisodeData>)
    : RecyclerView.Adapter<EpisodeRecyclerAdapter.EpisodeViewHolder>() {
    private val nightMode: Boolean = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        return EpisodeViewHolder(ItemSeasonEpisodeBinding.inflate(
            LayoutInflater.from(context), parent, false
        ))
    }

//    if (data.payment == EpisodeData.PAYMENT_VIP
//    && ConfigManager.VIP_STATE == 0) {
//        Application.onToast(context, R.string.text_episode_vip_needed)
//    } else {
//        onSetupDownload(index)
//    }
    private var onItemClick: (Int) -> Unit = { _ -> }
    fun setOnItemClickListener(onItemClick: (Int) -> Unit){
        this.onItemClick = onItemClick
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val data = list[position]
        when(orientation) {
            LinearLayout.VERTICAL -> {
                holder.binding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            LinearLayout.HORIZONTAL -> {
                holder.binding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
        if (data.title == ""){
            holder.binding.episodeTitle.visibility = View.GONE
        } else {
            holder.binding.episodeTitle.text = data.title
        }
        holder.binding.episodePublicTime.text = Application.getString(
            R.string.text_episode_public_time, data.pubRealTime
        )
        holder.binding.episodeIndexTitle.text = try {
            Application.getString(R.string.text_episode_index, data.index.apply { toFloat() })
        } catch (ignore: NumberFormatException) { data.index }
        if (data.badge == "") {
            holder.binding.episodeVipBackground.visibility = View.GONE
        } else {
            holder.binding.episodeVipBackground.visibility = View.VISIBLE
            if (nightMode) {
                holder.binding.episodeVipBackground.setCardBackgroundColor(data.badgeColorNight)
            } else {
                holder.binding.episodeVipBackground.setCardBackgroundColor(data.badgeColor)
            }
            holder.binding.episodeVip.text = data.badge
        }
        holder.binding.root.setOnClickListener {
            onItemClick(position)
        }
        if (holder.hasLoad) {
            holder.binding.episodeImage.visibility = View.VISIBLE
            return
        }
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.pic_doing_h)
            .error(R.drawable.pic_load_failed)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        Glide.with(context)
            .load(data.cover)
            .apply(requestOptions)
            .into(holder.binding.episodeImage)
        holder.hasLoad = true
    }

    private var orientation: Int = -1
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        when(recyclerView.layoutManager?.javaClass) {
            GridLayoutManager::class.java -> {
                orientation = LinearLayout.VERTICAL
            }
            LinearLayoutManager::class.java -> {
                orientation = LinearLayout.HORIZONTAL
            }
        }
    }

    override fun getItemCount(): Int = list.size

    class EpisodeViewHolder(val binding: ItemSeasonEpisodeBinding, var hasLoad: Boolean = false)
        : RecyclerView.ViewHolder(binding.root)
}