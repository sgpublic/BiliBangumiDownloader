package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.ui.MultiSelectable
import io.github.sgpublic.bilidownload.base.ui.SelectableArrayAdapter
import io.github.sgpublic.bilidownload.core.util.customLoad
import io.github.sgpublic.bilidownload.core.util.take
import io.github.sgpublic.bilidownload.core.util.withCrossFade
import io.github.sgpublic.bilidownload.core.util.withVerticalPlaceholder
import io.github.sgpublic.bilidownload.databinding.ItemDownloadSeasonBinding
import java.io.File

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 11:18
 */
class DownloadEpisodeAdapter: SelectableArrayAdapter<ItemDownloadSeasonBinding, DownloadEpisodeAdapter.SeasonTaskGroup>(),
    MultiSelectable<DownloadEpisodeAdapter.SeasonTaskGroup> {
    init {
        setOnItemLongClickListener longClick@{
            if (isSelectMode()) {
                return@longClick false
            }
            entrySelectMode(getItemPosition(it.seasonId))
            return@longClick true
        }
    }

    data class SeasonTaskGroup(
        val seasonTitle: String,
        val seasonCover: String,
        val seasonId: Long,
        var totalCount: Int = 0,
        var runningCount: Int = 0,
        var finishedCount: Int = 0,
    ): Comparable<SeasonTaskGroup> {
        override fun equals(other: Any?): Boolean {
            if (other !is SeasonTaskGroup) {
                return false
            }
            return seasonId == other.seasonId
        }
        override fun hashCode() = seasonId.hashCode()
        override fun compareTo(other: SeasonTaskGroup): Int {
            return (totalCount != other.totalCount || runningCount != other.runningCount || finishedCount != other.finishedCount).take(-1, 0)
        }
    }

    private val sidTmp: HashMap<Long, Int> = HashMap()
    override fun getItemPosition(id: Long): Int = sidTmp[id] ?: 0
    override fun setData(list: Collection<SeasonTaskGroup>) {
        super.setData(list)
        list.forEachIndexed { index, seasonTaskGroup ->
            sidTmp[seasonTaskGroup.seasonId] = index
        }
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup) =
        ItemDownloadSeasonBinding.inflate(inflater, parent, false)

    override fun onBindViewHolder(
        context: Context,
        ViewBinding: ItemDownloadSeasonBinding,
        data: SeasonTaskGroup
    ) {
        val cover = File(context.getExternalFilesDir("Cover")!!, "s_${data.seasonId}/cover.png")
            .takeIf { it.exists() }?.toUri()
        Glide.with(context)
            .let {
                if (cover != null) {
                    it.customLoad(cover)
                } else {
                    it.customLoad(data.seasonCover)
                }
            }
            .withVerticalPlaceholder()
            .withCrossFade()
            .into(ViewBinding.itemDownloadSeasonCover)
        ViewBinding.itemDownloadSeasonTitle.text = data.seasonTitle
        ViewBinding.itemDownloadSeasonSelected.visibility = (selectMode && multiSelection.contains(
            getItemPosition(data.seasonId)
        )).take(View.VISIBLE, View.GONE)
        ViewBinding.itemDownloadSeasonCount.text = when {
            data.runningCount > 0 -> context.getString(R.string.text_download_task_status, data.totalCount, data.runningCount)
            data.finishedCount == data.totalCount -> context.getString(R.string.text_download_task_all_finished, data.totalCount)
            else -> context.getString(R.string.text_download_task_finished, data.totalCount, data.finishedCount)
        }
    }

    override val Adapter: SelectableArrayAdapter<*, SeasonTaskGroup> = this
    override var selectMode: Boolean = false
    override val multiSelection: HashSet<Int> = HashSet()

    private var onChangeSelectMode: (Boolean) -> Unit = { }
    fun setOnChangeSelectModeListener(listener: (Boolean) -> Unit) {
        onChangeSelectMode = listener
    }
    override fun invokeOnChangeSelectMode(mode: Boolean) {
        onChangeSelectMode.invoke(mode)
    }
}