package io.github.sgpublic.bilidownload.ui.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import io.github.sgpublic.bilidownload.core.data.SeriesData
import io.github.sgpublic.bilidownload.core.data.parcelable.EpisodeData
import io.github.sgpublic.bilidownload.core.util.*
import io.github.sgpublic.bilidownload.databinding.ItemDownloadEpisodeBinding
import io.github.sgpublic.bilidownload.databinding.ItemDownloadSeasonBinding
import java.util.*

class DownloadListAdapter(
    private val context: AppCompatActivity,
    private val compat: ListCompat
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var expand: Int = -1
    override fun getItemViewType(position: Int): Int {
        if (position <= expand) {
            return TYPE_SEASON
        }
        if (position > expand + getExpandEpisode().size) {
            return TYPE_SEASON
        }
        return TYPE_EPISODE
    }

    fun getEpisodeList(position: Int): List<EpisodeData>? {
        return compat.list[getSeason(position)]?.keys?.toList()
    }

    fun getExpandEpisode(): LinkedList<EpisodeData> {
        val result = LinkedList<EpisodeData>()
        if (compat.list.size <= expand || expand < 0) {
            return result
        }
        val season = compat.list[getSeason(expand)] ?: return result
        result.addAll(season.keys)
        return result
    }

    fun getExpandSeason(): SeriesData? {
        return getSeason(expand)
    }

    fun getSeason(position: Int): SeriesData? {
        if (position !in 0 until compat.list.size) return null
        return compat.list.keys.toList()[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_EPISODE -> EpisodeTaskViewHolder(ItemDownloadEpisodeBinding.inflate(
                LayoutInflater.from(context), parent, false
            ))
            else -> SeasonTaskViewHolder(ItemDownloadSeasonBinding.inflate(
                LayoutInflater.from(context), parent, false
            ))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val layoutPosition = getSeasonLayoutPosition(position)
        if (layoutPosition >= 0) {
            compat.onBindSeasonItem(layoutPosition, holder as SeasonTaskViewHolder)
        } else {
            compat.onBindExpandEpisodeItem(position - expand - 1, holder as EpisodeTaskViewHolder)
        }
    }

    private fun getSeasonLayoutPosition(position: Int): Int {
        if (position <= expand) {
            return position
        }
        val size = getExpandEpisode().size
        return (position > expand + size).take(position - size, -1)
    }

    override fun getItemCount(): Int = compat.list.size + getExpandEpisode().size

    private var selected: LinkedList<EpisodeData>? = null

    fun isSelectMode() = selected != null

    fun isEpisodeSelected(episode: EpisodeData): Boolean {
        return selected.contains(episode)
    }

    fun isSeasonSelected(position: Int): Boolean {
        return selected.containsAll(getEpisodeList(position))
    }

    fun exchangeAllExpandEpisode() {
        selected.check {
            it.exchangeAll(getExpandEpisode())
            notifySeasonChange(expand)
            notifyExpandEpisodeChange()
        }
    }

    fun exchangeEpisode(episode: EpisodeData, position: Int) {
        selected.check {
            it.exchange(episode)
            notifyEpisodeChange(position)
        }
    }

    fun enterSelectMode() {
        selected.check({
            selected = LinkedList()
        }, {
            it.clear()
        })
    }

    fun exitSelectMode(): LinkedList<EpisodeData> {
        val tmp = LinkedList<EpisodeData>()
        tmp.addAll(selected)
        selected?.clear()
        selected = null
        notifyItemRangeChanged(0, itemCount)
        return tmp
    }

    private fun notifySeasonChange(position: Int) {
        notifyItemChanged((position <= expand).take(position, position + getExpandEpisode().size))
    }

    private fun notifyExpandEpisodeChange() {
        notifyItemRangeChanged(expand + 1, getExpandEpisode().size)
    }

    fun notifyEpisodeFold() {
        notifyItemRangeRemoved(expand + 1, getExpandEpisode().size)
        notifyItemRangeChanged(expand + 1, itemCount - expand - 1)
    }

    fun notifyEpisodeExpand() {
        notifyItemRangeInserted(expand + 1, getExpandEpisode().size)
        notifyItemRangeChanged(expand + 1, itemCount - expand - 1)
    }

    fun notifyEpisodeDeleted(position: Int) {
        notifyItemRemoved(expand + position + 1)
    }

    private fun notifyExpandEpisodeDeleted() {
        notifyItemRangeRemoved(expand, expand + getExpandEpisode().size)
    }

    fun notifySeasonDeleted(vararg positions: Int) {
        for (position in positions) {
            notifyItemRemoved(getSeasonLayoutPosition(position))
            if (position == expand) {
                notifyExpandEpisodeDeleted()
                expand = -1
            }
        }
    }

    private fun notifyEpisodeChange(position: Int) {
        notifySeasonChange(expand)
        notifyItemChanged(expand + 1 + position)
    }

    class SeasonTaskViewHolder(val binding: ItemDownloadSeasonBinding)
        : RecyclerView.ViewHolder(binding.root)
    class EpisodeTaskViewHolder(val binding: ItemDownloadEpisodeBinding)
        : RecyclerView.ViewHolder(binding.root)

    data class TaskInfo(val percent: Int, val speed: Long)

    companion object {
        private const val TYPE_SEASON = 1
        private const val TYPE_EPISODE = 2

        interface ListCompat {
            fun onBindExpandEpisodeItem(position: Int, holder: EpisodeTaskViewHolder)
            fun onBindSeasonItem(position: Int, holder: SeasonTaskViewHolder)

            val list: LinkedHashMap<SeriesData, LinkedHashMap<EpisodeData, MutableLiveData<TaskInfo>>>
        }
    }
}