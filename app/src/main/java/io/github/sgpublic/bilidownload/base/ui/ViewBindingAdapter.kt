package io.github.sgpublic.bilidownload.base.ui

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 *
 * @author Madray Haven
 * @date 2022/10/29 10:47
 */
abstract class ViewBindingRecyclerAdapter: RecyclerView.Adapter<ViewBindingHolder<*>>() {

}
abstract class ViewBindingHolder<VB: ViewBinding>(val ViewBinding: VB): RecyclerView.ViewHolder(ViewBinding.root) {
    val context: Context get() = ViewBinding.root.context
}