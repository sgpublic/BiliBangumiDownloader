package io.github.sgpublic.bilidownload.base.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import java.lang.Integer.max

/**
 *
 * @author Madray Haven
 * @date 2022/10/28 9:44
 */
abstract class ArrayRecyclerAdapter<VB: ViewBinding, ItemT>(list: List<ItemT>? = null):
    RecyclerView.Adapter<ArrayRecyclerAdapter.Holder<VB>>() {

    protected val data: ArrayList<ItemT> = ArrayList<ItemT>().also {
        it.addAll(list ?: return@also)
    }
    fun setData(list: Collection<ItemT>) {
        val size = max(this.data.size, list.size)
        this.data.clear()
        this.data.addAll(list)
        notifyItemRangeChanged(0, size)
    }

    private var onClick: (ItemT) -> Unit = { }
    fun setOnItemClickListener(onClick: (ItemT) -> Unit) {
        this.onClick = onClick
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder<VB> {
        return Holder(onCreateViewBinding(
            LayoutInflater.from(parent.context), parent
        ))
    }

    final override fun onBindViewHolder(holder: Holder<VB>, position: Int) {
        val data = data[position]
        onBindViewHolder(holder.binding.root.context, holder.binding, data)
        if (!clickable) {
            return
        }
        getClickableView(holder.binding).setOnClickListener {
            onClick.invoke(data)
        }
    }

    open val clickable: Boolean = true
    open fun getClickableView(ViewBinding: VB) = ViewBinding.root

    abstract fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup): VB
    abstract fun onBindViewHolder(context: Context, ViewBinding: VB, data: ItemT)
    override fun getItemCount() = data.size

    class Holder<VB: ViewBinding>(val binding: VB, ): ViewHolder(binding.root)
}