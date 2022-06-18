package io.github.sgpublic.bilidownload.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.databinding.ItemEpisodeListBinding
import java.util.*

class EpisodeListAdapter(private val context: Context) : SelectableBaseAdapter(){
    private val list: LinkedList<String> = LinkedList()
    override fun getCount(): Int = list.size
    override fun getItem(pos: Int): String = list[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()
    fun setEpisodeList(list: List<String>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ItemEpisodeListBinding = if (convertView != null) {
            ItemEpisodeListBinding.bind(convertView)
        } else {
            ItemEpisodeListBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        }
        binding.root.layoutParams.width = parent.width
        binding.itemEpisodeTitle.apply {
            if (position == this@EpisodeListAdapter.getSelection()) {
                setTextColor(ContextCompat.getColor(context, selectedColor))
                setBackgroundResource(R.drawable.shape_episode_list_border_current)
            } else {
                setTextColor(ContextCompat.getColor(context, normalColor))
                setBackgroundResource(R.drawable.shape_episode_list_border_list)
            }
            text = getItem(position)
            setOnClickListener {
                setSelection(position)

                onItemClick(position, getItem(position))
            }
        }
        return binding.root
    }
}