package io.github.sgpublic.bilidownload.ui.list

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.github.sgpublic.bilidownload.databinding.ItemQualityListBinding
import io.github.sgpublic.bilidownload.core.manager.ConfigManager
import java.util.*

class QualityListAdapter(private val context: Context) : SelectableBaseAdapter() {
    private val qnList: LinkedList<Map.Entry<Int, String>> = LinkedList()
    override fun getCount(): Int = qnList.size
    override fun getItem(pos: Int): Map.Entry<Int, String> = qnList[pos]
    override fun getItemId(pos: Int): Long = pos.toLong()
    fun setQualityMap(qnMap: Map<Int, String>) {
        this.qnList.clear()
        this.qnList.addAll(qnMap.entries.toList())
        notifyDataSetChanged()
    }

    private var gravity = Gravity.CENTER
    fun setGravity(gravity: Int) {
        this.gravity = gravity
    }

    override var position: Int = ConfigManager.QUALITY
    val currentIndex: Int get() {
        for (i in 0 until count) {
            if (getItem(i).key == position) {
                return i
            }
        }
        return 0
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
            gravity = this@QualityListAdapter.gravity
            if (item.key == getSelection()) {
                setTextColor(ContextCompat.getColor(context, selectedColor))
            } else {
                setTextColor(ContextCompat.getColor(context, normalColor))
            }
            text = item.value
            setOnClickListener {
                setSelection(position)
                onItemClick(item.key, item.value)
            }
        }
    }
}