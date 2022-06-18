package io.github.sgpublic.bilidownload.ui.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.Application
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.core.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.core.util.contains
import io.github.sgpublic.bilidownload.core.util.exchange
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.databinding.ItemSeasonEpisodeBinding
import io.github.sgpublic.bilidownload.room.entity.TaskEntity
import io.github.sgpublic.bilidownload.ui.customLoad
import io.github.sgpublic.bilidownload.ui.withCrossFade
import io.github.sgpublic.bilidownload.ui.withHorizontalPlaceholder

class EpisodeRecyclerAdapter(private val context: AppCompatActivity, private val list: List<EpisodeData>,
                             private val currentIndex: LiveData<Int>, private val selectable: Boolean = false)
    : RecyclerView.Adapter<EpisodeRecyclerAdapter.EpisodeViewHolder>() {
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
    private var listener: Listener? = null
    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun setItemClickListener(listener: (Int) -> Unit): EpisodeRecyclerAdapter {
        this.listener = object : Listener {
            override fun onItemClick(position: Int) {
                listener(position)
            }
        }
        return this
    }

    fun enterSelectMode() {
        selected = HashSet()
        listener?.onEnterSelectMode()
    }

    fun isSelectMode() = selected != null

    fun getSelectedData(): List<EpisodeData> {
        val result = arrayListOf<EpisodeData>()
        selected?.let {
            it.forEach { index ->
                result.add(list[index])
            }
        }
        return result
    }

    fun exitSelectedMode() {
        selected?.clear()
        selected = null
        notifyItemRangeChanged(0, itemCount)
        listener?.onExitSelectMode()
    }

    private val dao = Application.DATABASE.TasksDao()
    private var selected: HashSet<Int>? = null
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
        holder.binding.episodeSelected.visibility = selected.contains(position)
            .take(View.VISIBLE, View.GONE)
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
            if (Application.IS_NIGHT_MODE) {
                holder.binding.episodeVipBackground.setCardBackgroundColor(data.badgeColorNight)
            } else {
                holder.binding.episodeVipBackground.setCardBackgroundColor(data.badgeColor)
            }
            holder.binding.episodeVip.text = data.badge
        }
        Glide.with(context)
            .customLoad(data.cover)
            .withHorizontalPlaceholder()
            .withCrossFade()
            .into(holder.binding.episodeImage)

        val task = dao.getByCid(data.cid)
        if (selected == null) {
            if (selectable) {
                holder.binding.root.setOnLongClickListener {
                    if (listener?.onEnterSelectMode() == true) {
                        return@setOnLongClickListener true
                    }
                    selected = HashSet()
                    if (task == null) {
                        selected!!.add(position)
                    }
                    notifyItemRangeChanged(0, itemCount)
                    return@setOnLongClickListener true
                }
            }
            if (current == position) {
                holder.binding.episodeState.visibility = View.VISIBLE
                holder.binding.episodeState.setImageResource(R.drawable.ic_episode_playing)
                holder.binding.root.setOnClickListener { }
            } else {
                holder.binding.episodeState.visibility = View.GONE
                holder.binding.root.setOnClickListener {
                    listener?.onItemClick(position)
                }
            }
            return
        }
        if (task == null) {
            holder.binding.episodeState.visibility = View.GONE
            holder.binding.root.setOnClickListener {
                selected.exchange(position)
                notifyItemChanged(position)
            }
        } else {
            holder.binding.episodeState.visibility = View.VISIBLE
            if (task.status == TaskEntity.STATUS_FINISHED) {
                holder.binding.episodeState.setImageResource(R.drawable.ic_episode_finish)
            } else {
                holder.binding.episodeState.setImageResource(R.drawable.ic_episode_downloading)
            }
            holder.binding.root.setOnClickListener { }
        }
    }

    private var orientation: Int = -1
    private var current = -1
    private val observer = Observer<Int> {
        val tmp = current
        current = it
        if (tmp >= 0) notifyItemChanged(tmp)
        notifyItemChanged(current)
    }
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        when(recyclerView.layoutManager?.javaClass) {
            GridLayoutManager::class.java -> {
                orientation = LinearLayout.VERTICAL
            }
            LinearLayoutManager::class.java -> {
                orientation = LinearLayout.HORIZONTAL
            }
        }
        currentIndex.observeForever(observer)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        selected?.clear()
        selected = null
        currentIndex.removeObserver(observer)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int = list.size

    class EpisodeViewHolder(val binding: ItemSeasonEpisodeBinding)
        : RecyclerView.ViewHolder(binding.root)

    interface Listener {
        fun onItemClick(position: Int)
        fun onEnterSelectMode(): Boolean = false
        fun onExitSelectMode() { }
    }
}