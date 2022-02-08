package io.github.sgpublic.bilidownload.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.databinding.ItemEpisodeListBinding
import java.util.*

class EpisodeListAdapter(private val context: Context, private val origin: Int) : BaseAdapter()  {
    private var onChange: (Int) -> Unit = { _ -> }
    fun setOnEpisodeChangeListener(onChange: (Int) -> Unit) {
        this.onChange = onChange
        notifyDataSetChanged()
    }

    private val list: LinkedList<String> = LinkedList()
    override fun getCount(): Int = list.size
    override fun getItem(pos: Int): String = list[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()
    fun setEpisodeList(list: List<String>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    private var position: Int = 0
    fun next() {
        onChange(++position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ItemEpisodeListBinding = if (convertView != null) {
            ItemEpisodeListBinding.bind(convertView)
        } else {
            ItemEpisodeListBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        }
        binding.itemEpisodeTitle.apply {
            if (position == this@EpisodeListAdapter.position) {
                setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                setBackgroundResource(R.drawable.shape_episode_list_border_current)
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.color_player_controller))
                setBackgroundResource(R.drawable.shape_episode_list_border_list)
            }
            text = getItem(position)
            setOnClickListener {
                this@EpisodeListAdapter.position = position
                onChange(position)
            }
        }
        return binding.root
    }
}