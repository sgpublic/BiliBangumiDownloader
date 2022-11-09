package io.github.sgpublic.bilidownload.app.ui.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import io.github.sgpublic.bilidownload.base.ui.MultiSelectable
import io.github.sgpublic.bilidownload.base.ui.SelectableArrayAdapter
import io.github.sgpublic.bilidownload.core.util.customLoad
import io.github.sgpublic.bilidownload.core.util.withCrossFade
import io.github.sgpublic.bilidownload.core.util.withVerticalPlaceholder
import io.github.sgpublic.bilidownload.databinding.ItemDownloadSeasonBinding
import java.io.File
import java.util.HashMap

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 11:18
 */
class DownloadSeasonAdapter: SelectableArrayAdapter<ItemDownloadSeasonBinding, DownloadSeasonAdapter.SeasonTaskGroup>(),
    MultiSelectable<DownloadSeasonAdapter.SeasonTaskGroup> {
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
        val runningCount: Int,
        val totalCount: Int,
    )

    private val sidTmp: HashMap<Long, Int> = HashMap()
    override fun getItemPosition(id: Long): Int = sidTmp[id] ?: 0

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
        ViewBinding.itemDownloadSeasonSelected
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