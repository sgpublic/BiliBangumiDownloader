package io.github.sgpublic.bilidownload.app.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.base.ui.SelectableArrayAdapter
import io.github.sgpublic.bilidownload.base.ui.SingleSelection
import io.github.sgpublic.bilidownload.databinding.ItemEpisodeListBinding

abstract class PlayerEpisodeListAdapter<T: Any> :
    SelectableArrayAdapter<ItemEpisodeListBinding, T>(),
    SingleSelection<T> {
    private val idTmp = HashMap<Long, Int>()
    override fun setData(list: Collection<T>) {
        super.setData(list)
        list.forEachIndexed { index, item ->
            idTmp[getItemId(index)] = index
        }
    }

    fun setSelectedEpid(epid: Long) {
        setSelection(idTmp[epid] ?: 0)
    }
    override fun getItemPosition(id: Long) = idTmp[id] ?: 0

    override fun setOnItemClickListener(onClick: (T) -> Unit) {
        super.setOnItemClickListener {
            if (!isSelected(it)) {
                onClick.invoke(it)
            }
        }
    }

    override fun onBindViewHolder(context: Context, ViewBinding: ItemEpisodeListBinding, data: T) {
        ViewBinding.itemEpisodeTitle.apply {
            if (isSelected(data)) {
                setTextColor(ContextCompat.getColor(context, getSelectedColor()))
                setBackgroundResource(R.drawable.shape_episode_list_border_current)
            } else {
                setTextColor(ContextCompat.getColor(context, getNormalColor()))
                setBackgroundResource(R.drawable.shape_episode_list_border_list)
            }
            text = getItemTitle(data)
        }
    }

    abstract fun getItemTitle(data: T): String
    abstract fun isSelected(data: T): Boolean

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) = ItemEpisodeListBinding.inflate(inflater, parent, false)

    override fun getClickableView(ViewBinding: ItemEpisodeListBinding) = ViewBinding.itemEpisodeTitle

    override val Adapter: SelectableArrayAdapter<*, T> = this
    override var position: Int = 0
}