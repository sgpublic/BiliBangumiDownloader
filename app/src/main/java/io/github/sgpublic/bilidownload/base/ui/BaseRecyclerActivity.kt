package io.github.sgpublic.bilidownload.base.ui

import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import androidx.viewbinding.ViewBinding
import io.github.sgpublic.bilidownload.base.app.BaseViewModel
import io.github.sgpublic.bilidownload.base.app.BaseViewModelActivity
import io.github.sgpublic.bilidownload.databinding.ActivityRecyclerBinding

/**
 *
 * @author Madray Haven
 * @date 2022/11/9 10:55
 */
abstract class BaseRecyclerActivity<ItemT, ViewT: ViewBinding>: BaseViewModelActivity<ActivityRecyclerBinding, BaseRecyclerActivity.ArrayViewModel<ItemT>>() {
    abstract class ArrayViewModel<ItemT>: BaseViewModel() {
       abstract val Data: MutableLiveData<List<ItemT>>
    }

    @CallSuper
    override fun onViewSetup() {
        ViewBinding.recyclerOrigin.adapter = Adapter
    }

    protected abstract val Adapter: ArrayRecyclerAdapter<ViewT, ItemT>

    override val ViewBinding: ActivityRecyclerBinding = ActivityRecyclerBinding.inflate(layoutInflater)
    override val ViewModel: ArrayViewModel<ItemT> by viewModels()
}