package io.github.sgpublic.bilidownload.app.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import io.github.sgpublic.bilidownload.databinding.ItemQualityListBinding

class QualitySpinnerAdapter(
    private val qnList: ArrayList<Map.Entry<Int, String>>
): BaseAdapter() {
    override fun getCount(): Int = qnList.size
    override fun getItem(pos: Int): Map.Entry<Int, String> = qnList[pos]
    override fun getItemId(pos: Int): Long = getItem(pos).key.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)
        val binding: ItemQualityListBinding = if (convertView != null) {
            ItemQualityListBinding.bind(convertView)
        } else {
            ItemQualityListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        }
        binding.root.text = item.value
        return binding.root
    }
}