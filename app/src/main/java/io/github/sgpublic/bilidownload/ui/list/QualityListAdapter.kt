package io.github.sgpublic.bilidownload.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.databinding.ItemQualityListBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import java.util.*

class QualityListAdapter(private val context: Context) : SelectableBaseAdapter() {
    private val qnMap: MutableMap<Int, String> = linkedMapOf()
    private val qnList: LinkedList<Map.Entry<Int, String>> = LinkedList()
    override fun getCount(): Int = qnMap.size
    override fun getItem(pos: Int): Map.Entry<Int, String> = qnList[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()
    fun setQualityList(qnMap: Map<Int, String>) {
        this.qnMap.clear()
        this.qnMap.putAll(qnMap)
        this.qnList.clear()
        this.qnList.addAll(qnMap.entries.toList())
        notifyDataSetChanged()
    }

    override var position: Int = ConfigManager.QUALITY

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val binding: ItemQualityListBinding = if (convertView != null) {
            ItemQualityListBinding.bind(convertView)
        } else {
            ItemQualityListBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        }
        binding.root.layoutParams.width = parent.width
        return binding.root.apply {
            if (item.key == getSelection()) {
                setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.color_player_controller))
            }
            text = item.value
            setOnClickListener {
                setSelection(position)
                onItemClick(item.key, item.value)
            }
        }
    }
}