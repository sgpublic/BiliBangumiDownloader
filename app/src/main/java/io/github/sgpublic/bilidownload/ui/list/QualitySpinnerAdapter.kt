package io.github.sgpublic.bilidownload.ui.list

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import io.github.sgpublic.bilidownload.R
import io.github.sgpublic.bilidownload.databinding.ItemQualityListBinding
import java.util.*

class QualitySpinnerAdapter(private val context: Context) : BaseAdapter() {
    private val qnList: LinkedList<Map.Entry<Int, String>> = LinkedList()
    override fun getCount(): Int = qnList.size
    override fun getItem(pos: Int): Map.Entry<Int, String> = qnList[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()
    fun setQualityMap(qnMap: Map<Int, String>) {
        this.qnList.clear()
        this.qnList.addAll(qnMap.entries.toList())
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
            layoutParams.width = parent.width
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setTextColor(context.getColor(R.color.color_text_normal))
            text = item.value
        }
    }
}