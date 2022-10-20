package io.github.sgpublic.bilidownload.base.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.sgpublic.bilidownload.core.forest.core.BiliApiException

abstract class BaseViewModel: ViewModel() {
    val EXCEPTION: MutableLiveData<ExceptionData> = MutableLiveData()
    fun getExceptionData() = EXCEPTION.value
    data class ExceptionData(var code: Int, var message: String?)

    open val LOADING: MutableLiveData<Boolean> = MutableLiveData()
}

fun MutableLiveData<BaseViewModel.ExceptionData>.postValue(
    code: Int, message: String?
) {
    postValue(BaseViewModel.ExceptionData(code, message))
}

fun MutableLiveData<BaseViewModel.ExceptionData>.postValue(e: BiliApiException) {
    postValue(BaseViewModel.ExceptionData(e.code, e.msg))
}