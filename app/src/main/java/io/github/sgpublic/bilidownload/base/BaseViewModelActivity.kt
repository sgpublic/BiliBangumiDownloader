package io.github.sgpublic.bilidownload.base

import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding

abstract class BaseViewModelActivity<VB: ViewBinding, VM: ViewModel>: BaseActivity<VB>() {
    @Suppress("PropertyName")
    protected abstract val ViewModel: VM

    override fun beforeCreate() {
        onViewModelSetup()
    }

    protected open fun onViewModelSetup() { }
}