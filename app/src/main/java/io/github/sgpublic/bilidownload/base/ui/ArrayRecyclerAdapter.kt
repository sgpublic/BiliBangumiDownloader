package io.github.sgpublic.bilidownload.base.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import io.github.sgpublic.bilidownload.core.util.log
import java.lang.Integer.max

/**
 *
 * @author Madray Haven
 * @date 2022/10/28 9:44
 */
abstract class ArrayRecyclerAdapter<VB: ViewBinding, ItemT>(list: List<ItemT>? = null):
    RecyclerView.Adapter<ArrayRecyclerAdapter.Holder<VB>>() {

    private val data: ArrayList<ItemT> = ArrayList<ItemT>().also {
        it.addAll(list ?: return@also)
    }
    @CallSuper
    open fun setData(list: Collection<ItemT>) {
        val size = max(this.data.size, list.size)
        this.data.clear()
        this.data.addAll(list)
        if (size < list.size && size != 0) {
            notifyItemRangeInserted(size, list.size - size)
        } else {
            notifyItemRangeChanged(0, size)
            if (size > list.size) {
                notifyItemRangeRemoved(size + 1, size - list.size)
            }
        }
    }
    fun isEmpty() = data.isEmpty()

    fun getItem(position: Int): ItemT = data[position]

    private var onClick: (ItemT) -> Unit = { }
    open fun setOnItemClickListener(onClick: (ItemT) -> Unit) {
        this.onClick = onClick
    }

    private var onLongClick: (ItemT) -> Boolean = { false }
    fun setOnItemLongClickListener(onLongClick: (ItemT) -> Boolean) {
        this.onLongClick = onLongClick
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<VB> {
        return Holder(onCreateViewBinding(
            LayoutInflater.from(parent.context), parent
        ))
    }

    final override fun onBindViewHolder(holder: Holder<VB>, position: Int) {
        val data = data[position]
        onBindViewHolder(holder.binding.root.context, holder.binding, data)
        getClickableView(holder.binding)?.setOnClickListener {
            onClick.invoke(data)
        }
        getLongClickableView(holder.binding)?.setOnLongClickListener {
            onLongClick.invoke(data)
        }
    }

    open fun getClickableView(ViewBinding: VB): View? = null
    open fun getLongClickableView(ViewBinding: VB): View? = null

    abstract fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup): VB
    abstract fun onBindViewHolder(context: Context, ViewBinding: VB, data: ItemT)
    final override fun getItemCount() = data.size

    class Holder<VB: ViewBinding>(val binding: VB): ViewHolder(binding.root)
}