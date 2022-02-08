package io.github.sgpublic.bilidownload.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.databinding.ItemQualityListBinding
import io.github.sgpublic.bilidownload.manager.ConfigManager
import java.util.*

class QualityListAdapter(private val context: Context) : BaseAdapter() {
    private var onItemClick: (Int) -> Unit = { _ -> }
    fun setOnItemClickListener(onClick: (Int) -> Unit) {
        onItemClick = onClick
        notifyDataSetChanged()
    }

    private var onQualityChange: (String) -> Unit = { _ -> }
    fun setOnQualityChangeListener(onChange: (String) -> Unit) {
        onQualityChange = onChange
    }

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

    private var qn = ConfigManager.QUALITY
    fun setCurrentQuality(qn: Int) {
        this.qn = qn
        onQualityChange(qnMap[qn]!!)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val binding: ItemQualityListBinding = if (convertView != null) {
            ItemQualityListBinding.bind(convertView)
        } else {
            ItemQualityListBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        }
        return binding.root.apply {
            if (item.key == qn) {
                setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.color_player_controller))
            }
            text = item.value
            setOnClickListener {
                onItemClick(item.key)
            }
        }
    }

    data class QualityItem(
        val qn: Int,
        val name: String
    )
}