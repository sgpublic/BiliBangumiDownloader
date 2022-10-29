package io.github.sgpublic.bilidownload.base.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.viewbinding.ViewBinding


/**
 *
 * @author Madray Haven
 * @date 2022/10/29 10:17
 */
abstract class ArrayListAdapter<ItemT, VB: ViewBinding>: BaseAdapter() {
    private val data: ArrayList<ItemT> = ArrayList()
    fun setData(list: Collection<ItemT>) {
        this.data.clear()
        this.data.addAll(list)
        notifyDataSetChanged()
    }
    fun getData(): List<ItemT> {
        return ArrayList(data)
    }

    private var onClick: (ItemT) -> Unit = { }
    fun setOnItemClickListener(onClick: (ItemT) -> Unit) {
        this.onClick = onClick
    }

    final override fun getCount() = data.size
    final override fun getItem(position: Int) = data[position]
    final override fun getItemId(position: Int) = position.toLong()

    final override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val vb = onCreateViewBinding(LayoutInflater.from(parent.context), parent)
        val data = getItem(position)
        onBindView(vb.root.context, vb, data)
        getClickableView(vb)?.setOnClickListener {
            onClick.invoke(data)
        }
        return vb.root
    }

    abstract fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup): VB
    abstract fun onBindView(context: Context, ViewBinding: VB, data: ItemT)
    open fun getClickableView(ViewBinding: VB): View? = null
}